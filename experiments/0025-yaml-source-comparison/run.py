#!/usr/bin/env python3
"""Bounded, replayable comparison. --record deliberately updates captured evidence."""
from pathlib import Path
import argparse, copy, difflib, hashlib, json, os, re, subprocess, tempfile
import profile

HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[1]
BUILD=ROOT/'build/yaml-comparison'
CP=str(BUILD/'classes')+os.pathsep+str(BUILD/'snakeyaml-engine-3.1.1.jar')
JAVA=os.environ.get('COMPARISON_JAVA','java')

def custom(paths):
    return json.loads(subprocess.check_output([JAVA,'-cp',CP,'CustomProbe','inspect',*map(str,paths)],text=True))

def snake(mode,path):
    output=subprocess.check_output([JAVA,'-cp',CP,'YamlProbe',mode,str(path)],text=True)
    return json.loads(output) if mode=='load' else output

def comments(text):
    return [line.strip() for line in text.splitlines() if line.lstrip().startswith('#')]

def plain(value):
    if isinstance(value,dict):return {str(k):plain(v) for k,v in value.items()}
    if isinstance(value,list):return [plain(v) for v in value]
    if isinstance(value,str):return str(value)
    return value

def write_json(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2,sort_keys=True)+'\n')

def compare_files(actual_dir,expected_dir):
    for actual in sorted(actual_dir.rglob('*')):
        if not actual.is_file():continue
        expected=expected_dir/actual.relative_to(actual_dir)
        if not expected.exists() or actual.read_bytes()!=expected.read_bytes():
            raise AssertionError(f'Recorded evidence differs: {actual.relative_to(actual_dir)}; inspect {actual}')

def run(record):
    outputs=BUILD/'results';outputs.mkdir(exist_ok=True)
    cp=sorted((HERE/'fixtures/custom').glob('*.fixture'));yp=sorted((HERE/'fixtures/yaml').glob('*.yaml'))
    c=custom(cp);y=profile.inspect(yp)
    assert c['valid'] and y['valid'],(c['diagnostics'],y['diagnostics'])
    assert c['requirements']==y['requirements']
    assert custom(list(reversed(cp)))['requirements']==c['requirements']
    assert profile.inspect(list(reversed(yp)))['requirements']==y['requirements']
    write_json(outputs/'semantics.json',c['requirements'])
    summary={'requirements':len(c['requirements']),'source_files_per_format':len(cp),
             'semantic_equivalence':True,'input_order_independence':True,'formatting':[], 'pressure_cases':[], 'workflows':[]}
    with tempfile.TemporaryDirectory(prefix='comparison-',dir=BUILD) as d:
        work=Path(d)
        for file in yp:
            pydata,diag=profile.parse(file);assert not diag
            j=snake('load',file);assert j['valid'] and j['data']==plain(pydata),(file,j)
            customfile=HERE/'fixtures/custom'/file.name.replace('.yaml','.mreq.fixture')
            original_custom=customfile.read_text()
            co=subprocess.check_output([JAVA,'-cp',CP,'CustomProbe','format',str(customfile)],text=True)
            cf=work/'formatted.mreq.fixture';cf.write_text(co)
            assert subprocess.check_output([JAVA,'-cp',CP,'CustomProbe','format',str(cf)],text=True)==co
            assert comments(co)==comments(original_custom)
            assert custom([cf,*[p for p in cp if p!=customfile]])['requirements']==c['requirements']
            fmt={'file':file.name,'custom_comments_preserved':True,'custom_idempotent':True,'custom_semantics_preserved':True}
            for name,formatted in [('python-roundtrip',profile.roundtrip(file.read_text())),
                                   ('java-node-roundtrip',snake('roundtrip',file)),('java-object-dump',snake('dump',file)),
                                   ('java-conservative',snake('conservative',file))]:
                path=work/(name+'.yaml');path.write_text(formatted)
                data,diag=profile.parse(path);assert not diag,(name,file,diag)
                assert profile.normalize(data)==profile.normalize(pydata)
                second=profile.roundtrip(formatted) if name=='python-roundtrip' else snake({'java-node-roundtrip':'roundtrip','java-object-dump':'dump','java-conservative':'conservative'}[name],path)
                originals=comments(file.read_text());preserved=comments(formatted)
                fmt[name]={'semantic_equivalence':True,'idempotent':second==formatted,
                           'comments_before':len(originals),'comments_after':len(preserved),
                           'comment_sequence_preserved':originals==preserved,
                           'missing_comments':[s for s in originals if s not in preserved]}
                if name in ['python-roundtrip','java-conservative']:assert originals==preserved and second==formatted
                if name=='java-conservative':
                    path.write_bytes(file.read_text().replace('\n','\r\n').encode())
                    assert snake('conservative',path)==file.read_text()
                dest=outputs/'formatting'/name/file.name;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(formatted)
            summary['formatting'].append(fmt)
        for case in json.loads((HERE/'invalid/cases.json').read_text()):
            folder=HERE/'invalid'/case['name']; cr=custom([folder/'input.mreq.fixture']);yr=profile.inspect([folder/'input.yaml'])
            assert cr['valid']==case['custom_valid'] and yr['valid']==case['yaml_valid'],case
            if case['yaml_layer']:assert yr['diagnostics'][0]['layer']==case['yaml_layer'],case
            if case['equal'] is not None:assert (cr['requirements']==yr['requirements'])==case['equal'],case
            observed={'name':case['name'],'custom_valid':cr['valid'],'yaml_valid':yr['valid'],
                      'custom_diagnostics':cr['diagnostics'],'yaml_diagnostics':yr['diagnostics']}
            if case['equal'] is not None:observed['semantic_equivalence']=case['equal']
            # Parser interoperability is measured separately from candidate profile checks.
            if case['name']!='invalid-utf8':observed['java_parser']=snake('load',folder/'input.yaml')
            summary['pressure_cases'].append(observed)
        # Both formats undergo the same source-edit intent against the complete baseline.
        source=(HERE/'fixtures/custom/edge.mreq.fixture').read_text()
        yaml_source=(HERE/'fixtures/yaml/edge.yaml').read_text()
        def edit(name,cs,ys,unchanged=False):
            f=work/'edit.mreq.fixture';g=work/'edit.yaml';f.write_text(cs);g.write_text(ys)
            cr=custom([f,*[p for p in cp if p.name!='edge.mreq.fixture']])
            yr=profile.inspect([g,*[p for p in yp if p.name!='edge.yaml']])
            assert cr['valid'] and yr['valid'],(name,cr,yr)
            assert cr['requirements']==yr['requirements'],name
            assert (cr['requirements']==c['requirements'])==unchanged,name
            for ext,old,new in [('mreq',source,cs),('yaml',yaml_source,ys)]:
                path=outputs/'edits'/f'{name}.{ext}.diff';path.parent.mkdir(exist_ok=True)
                path.write_text(''.join(difflib.unified_diff(old.splitlines(True),new.splitlines(True),fromfile='before.'+ext,tofile='after.'+ext)))
            summary['workflows'].append({'name':name,'equal_semantics':True,'baseline_unchanged':unchanged})
        edit('normative-edit',source.replace('preserve Unicode','retain Unicode'),yaml_source.replace('preserve Unicode','retain Unicode'))
        # Wrapping replacements are chosen at an existing word boundary in each format.
        edit('wrap-prose',source.replace('001, and','001,\n  and'),yaml_source.replace('001, and','001,\n      and'),True)
        edit('retarget',source.replace('decomposes: TOP','decomposes: CHILD'),yaml_source.replace('- "TOP"','- "CHILD"'))
        edit('comment-only',source.replace('Synthetic lexical pressure','Revised lexical pressure'),yaml_source.replace('Synthetic lexical pressure','Revised lexical pressure'),True)
        # A separate simpler record supplies a paired add/split without context-dependent IDs.
        addc='\nrequirement 002\ntitle: Separate log behavior\nstatement:\n  The display shall record its label.\nend requirement\n'
        addy='\n  - id: "002"\n    title: "Separate log behavior"\n    statement: >-\n      The display shall record its label.\n'
        edit('add-record',source+addc,yaml_source+addy)
        edit('split-record',source.replace('  It shall preserve Unicode Δ and 😀 in the log.\n','')+addc.replace('The display shall record its label.','It shall preserve Unicode Δ and 😀 in the log.'),
             yaml_source.replace(' It shall preserve Unicode Δ and 😀 in the log.','')+addy.replace('The display shall record its label.','It shall preserve Unicode Δ and 😀 in the log.'))
        # File movement changes locator only; record order changes are nonsemantic too.
        f=work/'moved.mreq.fixture';g=work/'moved.yaml';f.write_text(source);g.write_text(yaml_source)
        assert custom([f,*[p for p in cp if p.name!='edge.mreq.fixture']])['requirements']==c['requirements']
        assert profile.inspect([g,*[p for p in yp if p.name!='edge.yaml']])['requirements']==y['requirements']
        summary['workflows'].append({'name':'move-file','equal_semantics':True,'baseline_unchanged':True})
        # Git's actual three-way text merge, both disjoint and conflicting edits.
        merged={}
        for kind in ['disjoint','conflicting']:
            for ext,base in [('mreq',source),('yaml',yaml_source)]:
                ours=base.replace('true: status','active: status')
                theirs=base.replace('true: status','inactive: status') if kind=='conflicting' else base.replace('section:4','section:5')
                files=[work/f'{label}.{ext}' for label in ['ours','base','theirs']]
                for path,text in zip(files,[ours,base,theirs]):path.write_text(text)
                proc=subprocess.run(['git','merge-file','-p','-L','ours','-L','base','-L','theirs',*map(str,files)],capture_output=True,text=True)
                assert proc.returncode==(1 if kind=='conflicting' else 0),(kind,ext,proc)
                path=outputs/'merges'/f'{kind}.{ext}.txt';path.parent.mkdir(exist_ok=True);path.write_text(proc.stdout)
                if kind=='disjoint':merged[ext]=proc.stdout
                summary['workflows'].append({'name':f'git-{kind}-{ext}','exit':proc.returncode,'conflict':proc.returncode==1})
        f=work/'merged.mreq.fixture';g=work/'merged.yaml';f.write_text(merged['mreq']);g.write_text(merged['yaml'])
        cm=custom([f,*[p for p in cp if p.name!='edge.mreq.fixture']]);ym=profile.inspect([g,*[p for p in yp if p.name!='edge.yaml']])
        assert cm['valid'] and ym['valid'] and cm['requirements']==ym['requirements']
        summary['merged_disjoint_semantics_equal']=True
        # Moving records into a single file in reverse order must also preserve semantics.
        f=work/'reordered.mreq.fixture';g=work/'reordered.yaml'
        allc='\n'.join(p.read_text() for p in cp)
        records=re.findall(r'^requirement [^\n]+\n.*?^end requirement\n',allc,re.M|re.S)
        f.write_text('\n'.join(reversed(records)))
        docs=[profile.yaml().load(p.read_text()) for p in yp]
        import io
        stream=io.StringIO();profile.yaml().dump({'requirements':list(reversed([item for doc in docs for item in doc['requirements']]))},stream);g.write_text(stream.getvalue())
        assert custom([f])['requirements']==c['requirements'] and profile.inspect([g])['requirements']==y['requirements']
        summary['workflows'].append({'name':'reorder-and-combine-records','equal_semantics':True,'baseline_unchanged':True})
        # Duplicate IDs across files are domain errors in both representations.
        f=work/'duplicate.mreq.fixture';g=work/'duplicate.yaml';f.write_text(source);g.write_text(yaml_source)
        assert not custom([*cp,f])['valid'] and not profile.inspect([*yp,g])['valid']
        summary['cross_file_duplicate_rejected']=True
    # Recorded descriptive size only; these are not usability scores or runtime benchmarks.
    summary['source_size']={}
    for name,files in [('custom',cp),('yaml',yp)]:
        summary['source_size'][name]={'lines':sum(len(p.read_text().splitlines()) for p in files),'bytes':sum(p.stat().st_size for p in files)}
    write_json(outputs/'summary.json',summary)
    if record:
        import shutil
        for p in outputs.rglob('*'):
            if p.is_file():
                dest=HERE/'results'/p.relative_to(outputs);dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,dest)
    else:compare_files(outputs,HERE/'results')
    print(f"PASS {summary['requirements']} paired requirements; {len(summary['pressure_cases'])} pressure cases; {len(summary['workflows'])} workflows; formatting evidence and golden results")

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--record',action='store_true');args=parser.parse_args();run(args.record)
