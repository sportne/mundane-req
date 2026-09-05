"""Black-box plan and verification acceptance, independent of requirement parser APIs."""
import copy
import json
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
FIXTURE=ROOT/'experiments/0028-verification-contract'
PLAN=Path(sys.argv[1]).resolve();VERIFY=Path(sys.argv[2]).resolve()
CP=str(ROOT/'build/maintained/classes')
PLAN_COMMANDS=[['java','-cp',CP,'engineering.verification.PlanMain'],[str(PLAN)]]
VERIFY_COMMANDS=[['java','-cp',CP,'engineering.verification.VerifyMain'],[str(VERIFY)]]


def write(path,obj):path.write_text(json.dumps(obj,sort_keys=True,ensure_ascii=False,separators=(',',':'))+'\n')
def plan(command,root,directory=None,status=0):
    result=subprocess.run(command+['--root',str(root),str(directory or root/'source')],capture_output=True,timeout=30)
    assert result.returncode==status,(result.returncode,result.stdout[-500:],result.stderr)
    assert not result.stderr
    artifact=json.loads(result.stdout);assert artifact['complete']==(status==0)
    if status:assert not artifact['plans'] and not artifact['activities'] and not artifact['coverage']
    return artifact,result.stdout

def verify(command,root,status=0,context=None):
    args=command+['--root',str(root),'--plan',str(root/'fixtures/plan.json')]
    if context:args+=['--context',context]
    result=subprocess.run(args+[str(root/'fixtures/imports.json')],capture_output=True,timeout=30)
    assert result.returncode==status,(result.returncode,result.stdout[-500:],result.stderr)
    assert not result.stderr
    artifact=json.loads(result.stdout);assert artifact['complete']==(status!=2)
    if status==2:assert not artifact['coverage'] and not artifact['uncovered'] and artifact['diagnostics']
    return artifact,result.stdout


for command in PLAN_COMMANDS:
    compiled,raw=plan(command,FIXTURE)
    assert compiled==json.loads((FIXTURE/'fixtures/plan.json').read_text()),'design fixture mismatch'
assert plan(PLAN_COMMANDS[0],FIXTURE)[1]==plan(PLAN_COMMANDS[1],FIXTURE)[1]
results=[verify(command,FIXTURE,1) for command in VERIFY_COMMANDS];assert results[0][1]==results[1][1]
assert [(r['requirementId'],r['changedFields']) for r in results[0][0]['coverage'] if r['state']=='review-stale']==[('RDS-002',['statement']),('SYS-009',['statement'])]
assert len(results[0][0]['coverage'])==57 and not results[0][0]['uncovered']
with tempfile.TemporaryDirectory(prefix='verification-workflow-') as directory:
    root=Path(directory);shutil.copytree(FIXTURE/'source',root/'source');shutil.copytree(FIXTURE/'fixtures',root/'fixtures')
    original={p.name:p.read_bytes() for p in (root/'source').iterdir()}
    original_plan=json.loads((root/'fixtures/plan.json').read_text())
    original_manifest=json.loads((root/'fixtures/imports.json').read_text())
    baseline=json.loads((root/'fixtures/baseline.json').read_text())
    def reset_source():
        for name,data in original.items():(root/'source'/name).write_bytes(data)
    invalid=[('plan.tsv',b'wrong\n'),('plan.tsv',original['plan.tsv'].replace(b'mundane-plan-source-0.1',b'unknown')),
        ('activities.tsv',original['activities.tsv'].replace(b'\ttest\t',b'\tunknown\t',1)),
        ('activities.tsv',original['activities.tsv']+original['activities.tsv'].splitlines(keepends=True)[1]),
        ('coverage.tsv',original['coverage.tsv']+original['coverage.tsv'].splitlines(keepends=True)[1]),
        ('coverage.tsv',original['coverage.tsv'].replace(b'PLAN-B',b'ABSENT',1)),
        ('coverage.tsv',original['coverage.tsv'].replace(b'ACT-NEED-REVIEW',b'ABSENT',1)),
        ('coverage.tsv',original['coverage.tsv'][:-1]),('coverage.tsv',b'\xef\xbb\xbf'+original['coverage.tsv']),
        ('coverage.tsv',original['coverage.tsv']+b'\n'),('coverage.tsv',b'\xff\n'),
        ('coverage.tsv',original['coverage.tsv'].replace(b'PLAN-B',b' PLAN-B',1)),
        ('activities.tsv',original['activities.tsv'].splitlines(keepends=True)[0])]
    for name,data in invalid:
        reset_source();(root/'source'/name).write_bytes(data)
        outputs=[plan(command,root,status=1)[1] for command in PLAN_COMMANDS];assert outputs[0]==outputs[1]
    reset_source()
    for p in (root/'source').iterdir():p.write_bytes(p.read_bytes().replace(b'\n',b'\r\n'))
    assert plan(PLAN_COMMANDS[1],root)[0]['coverage']==original_plan['coverage']
    reset_source()
    # Analyze a reselected baseline without rewriting requirements or pretending approval.
    manifest=copy.deepcopy(original_manifest);manifest['imports'][1]['path']='fixtures/baseline.json';write(root/'fixtures/imports.json',manifest)
    for command in VERIFY_COMMANDS:
        result,_=verify(command,root);assert all(r['state']=='current' and not r['possibleImpact'] for r in result['coverage'])
    # Header-only coverage is valid and exposes every in-scope requirement as uncovered.
    (root/'source/coverage.tsv').write_bytes(original['coverage.tsv'].splitlines(keepends=True)[0]);compiled,raw=plan(PLAN_COMMANDS[1],root);(root/'fixtures/plan.json').write_bytes(raw)
    for command in VERIFY_COMMANDS:assert len(verify(command,root,1)[0]['uncovered'])==57
    write(root/'fixtures/plan.json',original_plan);write(root/'fixtures/imports.json',original_manifest)
    # Missing and incomplete references never turn into apparently complete counts.
    for change in [lambda p:p['coverage'][0].update(requirementId='ABSENT'),lambda p:p.update(complete=False),lambda p:p.update(format='unknown')]:
        selected=copy.deepcopy(original_plan);change(selected);write(root/'fixtures/plan.json',selected)
        for command in VERIFY_COMMANDS:verify(command,root,2)
    write(root/'fixtures/plan.json',original_plan)
    for command in VERIFY_COMMANDS:verify(command,root,2,'unknown-context')
    # Rebuild maintained source after comment/move/normative and unrelated edits.
    source=root/'requirements';shutil.copytree(ROOT/'examples/yaml/vaccine-monitoring',source)
    compiler=ROOT/'build/maintained/mundanereq-compile'
    def compile_current():
        with (root/'fixtures/current.json').open('wb') as output:subprocess.run([str(compiler),'--source=yaml-0.3','--root',str(source),str(source)],stdout=output,check=True)
    # Baseline B selected for both sides: comments and moves change provenance only.
    compile_current();current=json.loads((root/'fixtures/current.json').read_text());write(root/'fixtures/baseline.json',current)
    manifest=copy.deepcopy(original_manifest);manifest['imports'][0]['sha256']=None;write(root/'fixtures/imports.json',manifest)
    p=source/'system.mreq.yaml';p.write_text('# comment\n'+p.read_text());compile_current()
    for command in VERIFY_COMMANDS:verify(command,root)
    p.rename(source/'moved.mreq.yaml');compile_current()
    for command in VERIFY_COMMANDS:verify(command,root)
    p=source/'moved.mreq.yaml';p.write_text(p.read_text().replace('at least five years','at least six years'));compile_current()
    for command in VERIFY_COMMANDS:
        result,_=verify(command,root,1);assert [r['requirementId'] for r in result['coverage'] if r['state']=='review-stale']==['SYS-009']
    # Unrelated device edits do not stale the retention activity projection.
    chosen=copy.deepcopy(original_plan);chosen['coverage']=[r for r in chosen['coverage'] if r['activityId']=='ACT-RETENTION'];write(root/'fixtures/plan.json',chosen)
    p.write_text(p.read_text().replace('at least six years','at least five years'))
    p=source/'device.mreq.yaml';p.write_text(p.read_text().replace('title: "','title: "Revised ',1));compile_current()
    for command in VERIFY_COMMANDS:
        result,_=verify(command,root,1);assert all(r['state']=='current' for r in result['coverage']) and len(result['uncovered'])==55
    for p in source.glob('*.mreq.yaml'):p.write_text(p.read_text().replace('SYS-009','SYS-009-NEW'))
    compile_current()
    for command in VERIFY_COMMANDS:verify(command,root,2)
    # Output failure evidence uses payloads larger than pipe buffers.
    reset_source();p=root/'source/activities.tsv';p.write_text(p.read_text().replace('Confirm each',('x'*1024*1024)+'Confirm each',1))
    (root/'fixtures/plan.json').write_bytes(plan(PLAN_COMMANDS[1],root)[1]);write(root/'fixtures/current.json',current)
    for command in PLAN_COMMANDS+VERIFY_COMMANDS:
        args=command+['--root',str(root)]+([str(root/'source')] if command in PLAN_COMMANDS else ['--plan',str(root/'fixtures/plan.json'),str(root/'fixtures/imports.json')])
        process=subprocess.Popen(args,stdout=subprocess.PIPE,stderr=subprocess.PIPE);process.stdout.read(64);process.stdout.close();assert process.wait(timeout=30)==2;assert process.stderr.read()
        process=subprocess.run(['bash','-c','exec "$@" 1>&-','closed']+args,capture_output=True,timeout=30);assert process.returncode==2
print('PASS plan/analyzer: compiled design fixture, 57 assertions, 13 invalid source cases, current/stale/uncovered/blocked results, edit/rebuild matrix and JVM/native broken output')
