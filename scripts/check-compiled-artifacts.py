"""Behavioral JVM/native artifact checks; independent JSON consumer and source slices."""
import hashlib
import json
import os
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FIXTURES = ROOT/'specification/examples/requirements-artifact-0.1'
NATIVE = Path(sys.argv[1]).resolve()
CP = str(ROOT/'build/maintained/classes')+':'+str(ROOT/'build/dependencies/snakeyaml-engine-3.1.1.jar')
COMMANDS = [['java','-cp',CP,'mundanereq.cli.CompileMain'], [str(NATIVE)]]


def invoke(command, root, inputs=None, source='yaml-0.3', expected=0):
    args = ['--source='+source, '--root', str(root), '--'] + [str(p) for p in (inputs if inputs is not None else [root])]
    result = subprocess.run(command+args, capture_output=True, timeout=45)
    assert result.returncode == expected, (args, result.returncode, result.stderr)
    assert not result.stderr, result.stderr
    artifact = json.loads(result.stdout)
    assert artifact['complete'] == (expected == 0)
    assert (not artifact['requirements']) if expected else (not artifact['diagnostics'])
    return artifact, result.stdout


def values(artifact):
    return [r['values'] for r in artifact['requirements']]


def slice_span(root, span):
    lines = (root/span['path']).read_text().splitlines(keepends=True)
    start, end = span['start'], span['end']
    assert end is not None
    for point in [start,end]:
        assert 1 <= point['line'] <= len(lines)+1
        content = lines[point['line']-1].rstrip('\r\n') if point['line'] <= len(lines) else ''
        assert 1 <= point['column'] <= len(content)+1, (span, content)
    a = sum(len(s) for s in lines[:start['line']-1])+start['column']-1
    b = sum(len(s) for s in lines[:end['line']-1])+end['column']-1
    assert b >= a
    return ''.join(lines)[a:b]


def check_spans(root, artifact):
    for source in artifact['sources']:
        assert source['sha256'] == hashlib.sha256((root/source['path']).read_bytes()).hexdigest()
    for record in artifact['requirements']:
        v, loc = record['values'], record['locations']
        assert v['id'] in slice_span(root,loc['record'])
        assert v['id'] in slice_span(root,loc['fields']['id'][0])
        for spans in loc['fields'].values():
            for span in spans: slice_span(root,span)
        for target,span in loc['references'].items():
            assert target in slice_span(root,span)


for case, status in [('valid',0),('invalid',1),('duplicate',1),('dangling',1)]:
    for command in COMMANDS:
        artifact, raw = invoke(command,FIXTURES/case,expected=status)
        assert raw == (FIXTURES/(case+'.json')).read_bytes(), ('golden',case,command)
        if not status: check_spans(FIXTURES/case,artifact)
print('PASS compiled contract golden bytes: valid/multifile/Unicode/math, invalid, duplicate and dangling; JVM/native')

# Existing language corpora are semantic compatibility evidence, not just parser internals.
corpora = [(ROOT/'conformance/0.1/valid','custom-0.2')]
for line in (ROOT/'conformance/0.3/migration-corpus.tsv').read_text().splitlines():
    old,new = line.split('\t')
    corpora += [(ROOT/old,'custom-0.2'),(ROOT/new,'yaml-0.3')]
corpora += [(ROOT/'conformance/0.3/authoring','yaml-0.3')]
artifacts = {}
for root, source in corpora:
    results = [invoke(command,root,source=source) for command in COMMANDS]
    assert results[0][1] == results[1][1], ('native parity',root)
    check_spans(root, results[0][0])
    suffix = '.mreq.yaml' if source == 'yaml-0.3' else '.mreq'
    inputs = sorted(root.glob('*'+suffix), reverse=True)
    for command in COMMANDS:
        assert invoke(command,root,inputs+inputs,source)[1] == results[0][1], 'argument reorder/deduplication'
    artifacts[root] = results[0][0]
for line in (ROOT/'conformance/0.3/migration-corpus.tsv').read_text().splitlines():
    old,new = line.split('\t')
    assert values(artifacts[ROOT/old]) == values(artifacts[ROOT/new])
# A committed corpus golden explicitly constrains every semantic field independent of source layout.
semantic = {str(root.relative_to(ROOT)): values(artifact) for root,artifact in artifacts.items()}
assert semantic == json.loads((FIXTURES/'corpus-values.json').read_text()), 'semantic corpus golden'
print('PASS 8 source corpora: source spans/checksums, deterministic reordered selection, 120 equivalent migrated requirements')

for version in ['0.1','0.2']:
    directory=ROOT/'conformance'/version/'invalid'
    for path in sorted(directory.iterdir()):
        if path.suffix == '.mreq' or path.is_dir():
            a,raw=invoke(COMMANDS[0],directory,[path],'custom-0.2',1)
            assert raw==invoke(COMMANDS[1],directory,[path],'custom-0.2',1)[1]
            assert a['diagnostics'] and all(d['location']['end'] is None for d in a['diagnostics'])
for path in sorted((ROOT/'conformance/0.3/invalid').glob('*.yaml')):
    a,raw=invoke(COMMANDS[0],path.parent,[path],expected=1)
    assert raw==invoke(COMMANDS[1],path.parent,[path],expected=1)[1]
print('PASS legacy and YAML adversarial corpora: no partial requirement publication; JVM/native diagnostic parity')

with tempfile.TemporaryDirectory(prefix='compiled-artifacts-') as directory:
    root=Path(directory)
    source=root/'source';shutil.copytree(FIXTURES/'valid',source)
    baseline,_=invoke(COMMANDS[1],source)
    (source/'a.mreq.yaml').write_text('# retained review comment\n'+(source/'a.mreq.yaml').read_text())
    comment,_=invoke(COMMANDS[1],source)
    assert values(comment)==values(baseline) and comment['sources']!=baseline['sources']
    (source/'a.mreq.yaml').rename(source/'moved.mreq.yaml')
    moved,_=invoke(COMMANDS[1],source)
    assert values(moved)==values(baseline) and moved['sources']!=comment['sources']
    p=source/'b.mreq.yaml';p.write_text(p.read_text().replace('The unit shall measure.','The unit shall record.'))
    changed,raw=invoke(COMMANDS[1],source)
    assert values(changed)!=values(baseline)
    # Cross-component boundary: only a JSON file is provided in an isolated cwd.
    serialized=root/'requirements.json';serialized.write_bytes(raw)
    consumer=FIXTURES/'consume.py'
    result=subprocess.run(['python3',str(consumer),str(serialized)],cwd=root,capture_output=True,text=True)
    assert result.returncode==0 and result.stdout=='A\nB\n'
    for case in ['invalid','duplicate','dangling','unknown']:
        result=subprocess.run(['python3',str(consumer),str(FIXTURES/(case+'.json'))],cwd=root,capture_output=True)
        assert result.returncode==1 and not result.stdout
    # Changes to root/argument ordering affect neither artifact paths nor values.
    copy=root/'other-checkout';shutil.copytree(source,copy)
    assert invoke(COMMANDS[1],copy)[1]==raw
    # Actual malformed bytes and bounded reads do not acquire misleading hashes/models.
    bad=root/'bad';bad.mkdir();f=bad/'input.mreq.yaml'
    for content,rule in [(b'\xff\n','invalid-utf8'),(b'x'*(8*1024*1024+2),'yaml-limit')]:
        f.write_bytes(content)
        for command in COMMANDS:
            artifact,_=invoke(command,bad,expected=1)
            assert rule in [d['ruleId'] for d in artifact['diagnostics']]
            if rule=='yaml-limit':assert artifact['sources'][0]['sha256'] is None
    f.write_bytes(b'x\nA'+ '😀'.encode()+b'B\xc3(')
    for command in COMMANDS:
        artifact,_=invoke(command,bad,expected=1)
        diagnostic=next(d for d in artifact['diagnostics'] if d['ruleId']=='invalid-utf8')
        assert diagnostic['location']['start']=={'line':2,'column':4}, 'UTF-8 byte-to-codepoint diagnostic conversion'
    f.unlink()
    for command in COMMANDS:
        empty,_=invoke(command,bad,expected=2)
        assert empty['diagnostics'][0]['ruleId']=='no-source-files'
        missing,_=invoke(command,bad,[bad/'missing.yaml'],expected=2)
        assert missing['diagnostics'][0]['phase']=='input'
        for args in [[], ['--source=unknown'], ['--root',str(bad),str(ROOT/'README.md')], ['--output-format=unknown']]:
            result=subprocess.run(command+args,capture_output=True)
            assert result.returncode==2 and not result.stdout and result.stderr
    # Broken pipes and closed descriptors must override successful source interpretation.
    f.write_text('format: "mundanereq-yaml-0.3"\nrequirements:\n  - id: "BIG"\n    title: "Large"\n    statement: "'+('x'*1024*1024)+'"\n')
    for command in COMMANDS:
        args=command+['--source=yaml-0.3','--root',str(bad),str(bad)]
        process=subprocess.Popen(args,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
        process.stdout.read(64);process.stdout.close()
        assert process.wait(timeout=45)==2
        assert process.stderr.read()
        # bash receives argv through positional parameters, never shell interpolation.
        result=subprocess.run(['bash','-c','exec "$@" 1>&-','closed-stdout']+args,capture_output=True,timeout=45)
        assert result.returncode==2
print('PASS isolated consumer, exact source versus semantic changes, independent checkout, resource/input/format failures and real JVM/native broken output')
