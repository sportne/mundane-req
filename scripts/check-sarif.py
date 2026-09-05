"""OASIS-schema validation, independent source-point navigation and actual JVM/native output checks."""
import hashlib
import json
import re
import subprocess
import sys
import tempfile
from pathlib import Path
from urllib.parse import unquote,urlsplit
from jsonschema import Draft4Validator,FormatChecker
ROOT=Path(__file__).resolve().parents[1];BASE=ROOT/'experiments/0032-sarif-diagnostics'
SCHEMA=ROOT/'dependencies/sarif/sarif-schema-2.1.0.json'
assert hashlib.sha256(SCHEMA.read_bytes()).hexdigest()==(SCHEMA.parent/'SHA256SUMS').read_text().split()[0]
schema=json.loads(SCHEMA.read_text());Draft4Validator.check_schema(schema)
validator=Draft4Validator(schema,format_checker=FormatChecker())
NATIVE=Path(sys.argv[1]).resolve();CP=str(ROOT/'build/maintained/classes')+':'+str(ROOT/'build/dependencies/snakeyaml-engine-3.1.1.jar')
COMMANDS=[['java','-cp',CP,'mundanereq.cli.ValidatorMain'],[str(NATIVE)]]
rules=set(re.findall(r'^\| ([a-z][a-z0-9-]+) \|',(ROOT/'specification/0013-compiled-diagnostic-rules.md').read_text(),re.M))

def run(command,root,paths,source='custom-0.2',status=1):
    result=subprocess.run(command+['--source='+source,'--output=sarif','--root',str(root),'--']+[str(p) for p in paths],capture_output=True,timeout=30)
    assert result.returncode==status and not result.stderr,(result.returncode,result.stderr)
    artifact=json.loads(result.stdout);validator.validate(artifact);a=artifact['runs'][0]
    assert a['invocations'][0]['exitCode']==status and a['properties']['sourceSetValid']==(status==0)
    assert a['columnKind']=='unicodeCodePoints'
    for item in a['results']:
        assert item['ruleId'] in rules and item['level']=='error'
        assert a['tool']['driver']['rules'][item['ruleIndex']]['id']==item['ruleId']
        physical=item['locations'][0]['physicalLocation'];uri=physical['artifactLocation']['uri']
        assert not urlsplit(uri).scheme and not uri.startswith('/')
        path=root/unquote(uri)
        if 'region' not in physical:continue
        # A deliberately small independent consumer resolves each source point.
        region=physical['region'];assert set(region)=={'startLine','startColumn'}
        lines=path.read_bytes().decode('utf-8',errors='replace').splitlines()
        assert 1<=region['startLine']<=len(lines)+1
        line=lines[region['startLine']-1] if region['startLine']<=len(lines) else ''
        assert 1<=region['startColumn']<=len(line)+1
    return artifact,result.stdout

cases=[('clean',BASE/'valid',[BASE/'valid'],0,'custom-0.2'),
       ('multi',BASE/'invalid',[BASE/'invalid'],1,'custom-0.2'),
       ('semantic',BASE/'invalid',[BASE/'invalid/semantic.mreq'],1,'custom-0.2'),
       ('recovery',ROOT/'experiments/0031-parser-recovery/custom',[ROOT/'experiments/0031-parser-recovery/custom'],1,'custom-0.2'),
       ('yaml',ROOT/'experiments/0031-parser-recovery/yaml',[ROOT/'experiments/0031-parser-recovery/yaml'],1,'yaml-0.3'),
       ('missing',BASE/'valid',[BASE/'valid/absent.mreq'],2,'custom-0.2')]
for name,root,paths,status,source in cases:
    pair=[run(c,root,paths,source,status) for c in COMMANDS]
    assert pair[0][1]==pair[1][1]
    expected=BASE/'golden'/(name+'.sarif.json');assert expected.read_bytes()==pair[0][1],('review SARIF golden',name)
    a=pair[0][0]['runs'][0]
    if name=='multi':
        assert len(a['results'])==2 and not a['invocations'][0]['executionSuccessful']
        points={unquote(r['locations'][0]['physicalLocation']['artifactLocation']['uri']):r['locations'][0]['physicalLocation']['region'] for r in a['results']}
        assert points=={'byte 😀.mreq':{'startLine':2,'startColumn':9},'α space #%.mreq':{'startLine':2,'startColumn':10}}
        assert '%23%25' in pair[0][1].decode() and '%F0%9F%98%80' in pair[0][1].decode()
        reverse=run(COMMANDS[1],root,sorted(root.iterdir(),reverse=True))[1];assert reverse==pair[0][1]
    if name=='semantic':assert a['invocations'][0]['executionSuccessful'] and not a['properties']['sourceSetValid']
    if name=='missing':assert 'region' not in a['results'][0]['locations'][0]['physicalLocation']

with tempfile.TemporaryDirectory(prefix='sarif-cases-') as temporary:
    root=Path(temporary)
    for c in COMMANDS:run(c,root,[root],status=2)
    source=root/'one:two.mreq';source.write_text((BASE/'invalid/semantic.mreq').read_text())
    for c in COMMANDS:
        a,_=run(c,root,[source]);assert a['runs'][0]['results'][0]['locations'][0]['physicalLocation']['artifactLocation']['uri']=='./one:two.mreq'
    # Enough output to exercise a genuine broken pipe after a prefix.
    for n in range(120): (root/f'{n:03}.mreq').write_text('requirement BAD\ntitle: Bad\nstatement:\n  Shall respond.\n'+'x'*20000+': unknown\nend requirement\n')
    for c in COMMANDS:
        args=c+['--output=sarif','--root',str(root),str(root)]
        p=subprocess.Popen(args,stdout=subprocess.PIPE,stderr=subprocess.PIPE);p.stdout.read(64);p.stdout.close();assert p.wait(timeout=30)==2
        for descriptor in [1,2]:
            delivery_args=args if descriptor==1 else c+['--unknown']
            r=subprocess.run(['bash','-c',f'exec "$@" {descriptor}>&-','closed']+delivery_args,capture_output=True,timeout=30);assert r.returncode==2,(c,descriptor,r.returncode,len(r.stdout),r.stderr[:200])
print('PASS SARIF: six OASIS-schema goldens, JVM/native parity, Unicode source navigation, encoded/colon paths, deterministic selection, incomplete/input failures and real broken output')
