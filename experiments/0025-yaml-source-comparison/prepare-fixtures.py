#!/usr/bin/env python3
"""Recreate paired experimental copies from immutable, recorded source selections."""
from pathlib import Path
import hashlib, json, re, subprocess, textwrap
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[1]
CP=ROOT/'build/yaml-comparison/classes'
SELECTIONS={
 'comments':('conformance/0.2/valid/requirements.mreq',None),
 'uas':('experiments/0001-source-representations/candidate-b-one-per-file/requirements', ['OPS-004','SYS-006']),
 'pilot':('experiments/0024-vaccine-monitoring-pilot/product/requirements', ['NEED-001','NEED-004','NEED-005','SYS-001','SYS-009'])}
def frozen(p):
    return subprocess.check_output(["git", "show", "ea463f8:"+str(p.relative_to(ROOT))])

provenance=[]
for name,(source,ids) in SELECTIONS.items():
    p=ROOT/source
    if source.endswith(".mreq"):text=frozen(p).decode();paths=[p]
    else:
        records={};paths=[]
        for name_at_commit in subprocess.check_output(['git','ls-tree','-r','--name-only','ea463f8','--',source],text=True).splitlines():
            f=ROOT/name_at_commit
            if not f.name.endswith('.mreq'):continue
            for m in re.finditer(r'^requirement ([^\n]+)\n.*?^end requirement\n',frozen(f).decode(),re.M|re.S):
                if m[1] in ids:records[m[1]]=m[0];paths.append(f)
        text='\n'.join(records[id] for id in ids)
    target=HERE/'fixtures/custom'/f'{name}.mreq.fixture';target.write_text(text)
    provenance.append(dict(fixture=str(target.relative_to(HERE)),ids=ids or ['TOP','CHILD','LEAF'],
        sources=[dict(path=str(f.relative_to(ROOT)),sha256=hashlib.sha256(frozen(f)).hexdigest()) for f in sorted(set(paths))],
        operation='Exact full-file copy' if source.endswith('.mreq') else 'Exact complete-record extraction in listed order'))
edge='''# Synthetic lexical pressure; this is not an engineering obligation from the pilot.
requirement 001
title: true: status # Δ 😀
allocation: 2026-09-04
statement:
  The display shall show true, 001, and the label status: ready # without changing their text.
  It shall preserve Unicode Δ and 😀 in the log.
rationale:
  A colon: and a number sign # are prose, not structural metadata.

  math latex is ordinary rationale prose here.
source: https://example.invalid/spec#section:4
decomposes: TOP
end requirement
'''
(HERE/'fixtures/custom/edge.mreq.fixture').write_text(edge)
provenance.append(dict(fixture='fixtures/custom/edge.mreq.fixture',ids=['001'],operation='Synthetic lexical-pressure example, authored for this experiment'))

def scalar(value):
    # Quote all non-prose scalars for an intentionally predictable authoring convention.
    return json.dumps(value,ensure_ascii=False)

def field(lines,key,value,indent=4):
    prefix=' '*indent
    if key in ['statement','rationale']:
        if len(value)==1 and value[0][0]=='prose':
            lines.append(prefix+key+': >-')
            lines.extend(' '*(indent+2)+s for s in textwrap.wrap(value[0][1],76,break_long_words=False,break_on_hyphens=False))
        else:
            lines.append(prefix+key+':')
            for b in value:
                if key=='rationale':
                    lines.append(prefix+'  - >-')
                    lines.extend(prefix+'      '+s for s in textwrap.wrap(b[1],72,break_long_words=False,break_on_hyphens=False))
                elif b[0]=='prose':
                    lines.append(prefix+'  - prose: >-')
                    lines.extend(prefix+'      '+s for s in textwrap.wrap(b[1],72,break_long_words=False,break_on_hyphens=False))
                else:
                    lines.extend([prefix+'  - math:',prefix+'      language: latex',prefix+'      payload: |-'])
                    lines.extend(prefix+'        '+s if s else '' for s in b[2].split('\n'))
    elif key=='decomposes':
        lines.append(prefix+'decomposes:')
        lines.extend(prefix+'  - '+scalar(s) for s in value)
    else:lines.append(prefix+key+': '+scalar(value))
for f in sorted((HERE/'fixtures/custom').glob('*.fixture')):
    # Whole-set oracle first; individual files intentionally may contain external references.
    result=json.loads(subprocess.check_output(['java','-cp',str(CP),'CustomProbe','inspect',str(f)],text=True))
    records={r['id']:r for r in result['requirements']}
    lines=['requirements:'];current=None;pending=[];emitted=set()
    for line in f.read_text().splitlines():
        if line.startswith('#'):pending.append(line)
        elif line.startswith('requirement '):
            lines.extend('  '+c for c in pending);pending=[]
            current=records[line.split(' ',1)[1]];emitted=set()
            lines.append('  - id: '+scalar(current['id']))
        elif line=='end requirement':
            lines.extend('    '+c for c in pending);pending=[];lines.append('')
        elif re.match(r'^(title|allocation|statement|rationale|source|decomposes):',line):
            key=line.split(':',1)[0]
            lines.extend('    '+c for c in pending);pending=[]
            if key not in emitted:
                field(lines,key,current[key]);emitted.add(key)
    lines.extend('  '+c for c in pending)
    (HERE/'fixtures/yaml'/f.name.replace('.mreq.fixture','.yaml')).write_text('\n'.join(lines).rstrip()+'\n')
(HERE/'provenance.json').write_text(json.dumps({'source_commit':'ea463f8','selections':provenance},indent=2)+'\n')
