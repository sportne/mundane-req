#!/usr/bin/env python3
"""Record actual tool versions and resolver defaults; no inferred compatibility claims."""
from pathlib import Path
import hashlib, importlib.metadata, json, os, platform, subprocess, tempfile
from ruamel.yaml import YAML
import profile
HERE=Path(__file__).resolve().parent; ROOT=HERE.parents[1]; BUILD=ROOT/'build/yaml-comparison'
def version(args):
    p=subprocess.run(args,capture_output=True,text=True,check=True)
    return (p.stdout+p.stderr).strip()
jar=BUILD/'snakeyaml-engine-3.1.1.jar'
env={'date':'2026-09-04','source_baseline':'ea463f8','python':platform.python_version(),
     'packages':{n:importlib.metadata.version(n) for n in ['ruamel.yaml','jsonschema','attrs','jsonschema-specifications','referencing','rpds-py','typing-extensions']},
     'java':version([os.environ.get('COMPARISON_JAVA','java'),'-version']),
     'javac':version([os.environ.get('COMPARISON_JAVAC','javac'),'-version']),
     'native_image':version([os.environ.get('COMPARISON_NATIVE_IMAGE','native-image'),'--version']),
     'platform':version(['uname','-srm']),'git':version(['git','--version']),
     'snakeyaml_engine':'3.1.1','jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),
     'native_flags':['-O0','--no-fallback','-march=compatibility'],
     'native_scope':'Parsing/emission only; native JSON Schema/domain validation not implemented or tested'}
(HERE/'results/environment.json').write_text(json.dumps(env,indent=2)+'\n')
rows=[]
with tempfile.TemporaryDirectory(dir=BUILD) as d:
 for scalar in ['001','true','yes','2026-09-04','1e3']:
    text='value: '+scalar+'\n'
    default=YAML(typ='rt',pure=True);default.version=(1,2)
    a=default.load(text)['value'];b=profile.yaml().load(text)['value']
    p=Path(d)/'scalar.yaml';p.write_text(text)
    j=json.loads(subprocess.check_output([os.environ.get('COMPARISON_JAVA','java'),'-cp',str(BUILD/'classes')+os.pathsep+str(jar),'YamlProbe','load',str(p)],text=True))['data']['value']
    rows.append({'source':scalar,'ruamel_default_type':type(a).__name__,'ruamel_default_value':str(a),
                 'selected_profile_type':type(b).__name__,'selected_profile_value':str(b),
                 'snake_core_json_value':j,'selected_values_equal':b==j})
(HERE/'results/parser-defaults.json').write_text(json.dumps(rows,indent=2)+'\n')
print('Recorded environment and five scalar-resolution comparisons')
