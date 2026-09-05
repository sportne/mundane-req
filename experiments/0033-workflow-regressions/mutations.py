"""Six isolated non-equivalent mutations; only public CLI behavior kills a mutant."""
import json
import shutil
import subprocess
import tempfile
import time
from pathlib import Path

ROOT=Path(__file__).resolve().parents[2];BUILD=ROOT/'build/experiment-0033';BUILD.mkdir(parents=True,exist_ok=True)
JAR=ROOT/'build/dependencies/snakeyaml-engine-3.1.1.jar'
BASE=ROOT/'build/maintained/classes';GENERATED=ROOT/'build/maintained/generated/mundanereq/Versions.java'
FIXTURES=ROOT/'experiments/0028-verification-contract/fixtures'
MUTANTS=[
 ('parser-padded-scalar','mundanereq/Interpreter.java','if (isScalarBoundaryWhitespace(value.codePointBefore(value.length())))', 'if (false && isScalarBoundaryWhitespace(value.codePointBefore(value.length())))','scalar'),
 ('validator-missing-target','mundanereq/Interpreter.java','if (!byId.containsKey(relationship.target())','if (false && !byId.containsKey(relationship.target())','reference'),
 ('resolver-exact-pin','engineering/artifacts/Linker.java','if(pin!=null&&!pin.equals(snapshot.sha256()))','if(false&&pin!=null&&!pin.equals(snapshot.sha256()))','pin'),
 ('resolver-incomplete-import','engineering/artifacts/Artifacts.java','if(!Boolean.TRUE.equals(a.get("complete")))','if(false&&!Boolean.TRUE.equals(a.get("complete")))','incomplete'),
 ('compiler-partial-publication','mundanereq/compile/SemanticArtifact.java','if (result.valid()) {','if (true) {','publication'),
 ('analyzer-statement-basis','engineering/artifacts/Artifacts.java','"allocation","statement","rationale"','"allocation","rationale"','statement'),
]

def run(args):return subprocess.run([str(x) for x in args],capture_output=True,timeout=45)

def witness(classes,kind,root):
    cmd=['java','-cp',str(classes)+':'+str(JAR)]
    if kind in ['scalar','reference']:
        text='requirement GOOD\ntitle: Good\nstatement:\n  Shall respond.\nend requirement\n'
        text=text.replace('title: Good','title: Good ') if kind=='scalar' else text.replace('end requirement','decomposes: ABSENT\nend requirement')
        file=root/'witness.mreq';file.write_text(text)
        result=run(cmd+['mundanereq.cli.ValidatorMain',file]);return {'status':result.returncode,'rule':('empty-or-padded-scalar' if kind=='scalar' else 'dangling-reference') in result.stderr.decode()}
    if kind=='publication':
        source=ROOT/'experiments/0031-parser-recovery/invalid/custom'
        result=run(cmd+['mundanereq.cli.CompileMain','--root',source,source]);a=json.loads(result.stdout)
        return {'status':result.returncode,'complete':a['complete'],'records':len(a['requirements'])}
    shutil.copytree(FIXTURES,root/'fixtures',dirs_exist_ok=True)
    if kind=='pin':
        p=root/'fixtures/imports.json';a=json.loads(p.read_text());a['imports'][0]['sha256']='0'*64;p.write_text(json.dumps(a))
    if kind=='incomplete':
        p=root/'fixtures/current.json';a=json.loads(p.read_text());a['complete']=False;p.write_text(json.dumps(a))
    main='engineering.verification.VerifyMain' if kind=='statement' else 'engineering.artifacts.LinkMain'
    result=run(cmd+[main,'--root',root,'--plan',root/'fixtures/plan.json',root/'fixtures/imports.json']);a=json.loads(result.stdout)
    if kind=='statement':return {'status':result.returncode,'stale':[r['requirementId'] for r in a['coverage'] if r['state']=='review-stale']}
    return {'status':result.returncode,'complete':a['complete'],'codes':[d['code'] for d in a['diagnostics']]}

EXPECTED={'scalar':{'status':1,'rule':True},'reference':{'status':1,'rule':True},
          'publication':{'status':1,'complete':False,'records':0},
          'pin':{'status':1,'complete':False,'codes':['digest-mismatch']},
          'incomplete':{'status':1,'complete':False,'codes':['incomplete-import']},
          'statement':{'status':1,'stale':['RDS-002','SYS-009']}}
started=time.monotonic();results=[]
with tempfile.TemporaryDirectory(prefix='workflow-mutations-') as temporary:
    root=Path(temporary)
    for name,path,before,after,kind in MUTANTS:
        if time.monotonic()-started>180:raise TimeoutError('180-second mutation budget exceeded')
        folder=root/name;folder.mkdir();baseline=folder/'baseline';baseline.mkdir()
        observed=witness(BASE,kind,baseline);assert observed==EXPECTED[kind],('unmutated witness failed',name,observed)
        source=folder/'source';shutil.copytree(ROOT/'src/main/java',source)
        p=source/path;text=p.read_text();assert text.count(before)==1,('mutation operator needs review',name);p.write_text(text.replace(before,after))
        classes=folder/'classes';classes.mkdir()
        compiled=run(['javac','--release','21','-cp',JAR,'-d',classes]+sorted(source.rglob('*.java'))+[GENERATED]);assert compiled.returncode==0,('noncompiling mutant is not evidence',name,compiled.stderr)
        candidate=folder/'candidate';candidate.mkdir();changed=witness(classes,kind,candidate)
        assert changed!=EXPECTED[kind],('surviving mutant requires a behavior test or explanation',name,changed)
        # A crashed mutant is not counted as rule-detection evidence.
        assert changed['status'] in [0,1,2],('mutant crashed',name,changed)
        results.append({'mutation':name,'component':path,'expected':EXPECTED[kind],'observed':changed,'result':'killed by observable behavior'})
        print('PASS mutation',name,flush=True)
(BUILD/'mutations.json').write_text(json.dumps(results,sort_keys=True,indent=2)+'\n')
print('PASS six compiled, non-equivalent mutations killed; original tracked source unchanged; elapsed %.2fs'%(time.monotonic()-started))
