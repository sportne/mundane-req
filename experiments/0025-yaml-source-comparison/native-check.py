#!/usr/bin/env python3
"""Parser/emitter JVM-native parity only: not a native schema/domain validator."""
from pathlib import Path
import argparse, json, os, subprocess
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[1];BUILD=ROOT/'build/yaml-comparison'
CP=str(BUILD/'classes')+os.pathsep+str(BUILD/'snakeyaml-engine-3.1.1.jar')
parser=argparse.ArgumentParser();parser.add_argument('--record',action='store_true');args=parser.parse_args()
checks=[]
for path in sorted((HERE/'fixtures/yaml').glob('*.yaml'))+sorted((HERE/'invalid').glob('*/input.yaml')):
    if path.parent.name=='invalid-utf8':continue # Byte-level UTF-8 failure is measured in the profile suite.
    modes=['load'] if 'invalid' in path.parts else ['load','dump','roundtrip','conservative']
    for mode in modes:
        j=subprocess.run([os.environ.get('COMPARISON_JAVA','java'),'-cp',CP,'YamlProbe',mode,str(path)],capture_output=True)
        n=subprocess.run([str(BUILD/'yaml-probe'),mode,str(path)],capture_output=True)
        assert (j.returncode,j.stdout)==(n.returncode,n.stdout),(path,mode,j.stderr,n.stderr)
        checks.append({'file':str(path.relative_to(HERE)),'mode':mode,'exit':j.returncode,'equal_stdout':True})
result=json.dumps({'scope':'SnakeYAML parser/emitter JVM-native parity; schema/domain checks remain Python','checks':checks},indent=2)+'\n'
p=BUILD/'native-summary.json';p.write_text(result)
expected=HERE/'results/native-summary.json'
if args.record:expected.write_text(result)
else:assert expected.read_text()==result,'native parity evidence changed'
print(f'PASS {len(checks)} JVM/native parser and emitter comparisons')
