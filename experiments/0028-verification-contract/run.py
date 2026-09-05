"""Design experiment: use published requirement JSON and existing TSV rows only."""
import copy
import csv
import hashlib
import json
import shutil
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
BUILD=ROOT/'build/experiment-0028'
COMPILER=ROOT/'build/maintained/mundanereq-compile'


def table(name):
    with (HERE/'source'/name).open() as stream:return list(csv.DictReader(stream,delimiter='\t'))


def compile_req(source):
    return json.loads(subprocess.check_output([str(COMPILER),'--source=yaml-0.3','--root',str(source),str(source)]))


def basis(artifact):return {r['values']['id']:r['values'] for r in artifact['requirements']}


def analyze(old,new,coverage):
    if not old['complete'] or not new['complete']:return 'incomplete'
    a,b=basis(old),basis(new)
    missing=sorted({r['requirement_id'] for r in coverage if r['requirement_id'] not in a or r['requirement_id'] not in b})
    if missing:return 'missing:'+','.join(missing)
    changed=sorted({r['requirement_id'] for r in coverage if a[r['requirement_id']]!=b[r['requirement_id']]})
    uncovered=sorted(set(b)-{r['requirement_id'] for r in coverage})
    return 'stale='+','.join(changed)+'; uncovered='+','.join(uncovered)


def run():
    if BUILD.exists():shutil.rmtree(BUILD)
    source=BUILD/'requirements';shutil.copytree(ROOT/'examples/yaml/vaccine-monitoring',source)
    b=compile_req(source)
    for name in ['system.mreq.yaml','remote.mreq.yaml']:
        p=source/name;p.write_text(p.read_text().replace('at least five years','at least three years'))
    a=compile_req(source)
    activities=table('activities.tsv');coverage=table('coverage.tsv');plans=table('plan.tsv')
    assert len(plans)==1 and {r['activity_id'] for r in coverage}<={r['activity_id'] for r in activities}
    assert {r['requirement_id'] for r in coverage}==set(basis(a))
    results={'baseline-a':analyze(a,a,coverage),'baseline-b':analyze(a,b,coverage)}
    assert results['baseline-b']=='stale=RDS-002,SYS-009; uncovered='
    p=source/'system.mreq.yaml';p.write_text('# comment\n'+p.read_text());comment=compile_req(source)
    results['comment']=analyze(a,comment,coverage);p.rename(source/'moved.mreq.yaml')
    moved=compile_req(source);results['move']=analyze(a,moved,coverage)
    # Retention plan projection excludes unrelated activity coverage for this comparison.
    retention=[r for r in coverage if r['activity_id']=='ACT-RETENTION']
    p=source/'device.mreq.yaml';p.write_text(p.read_text().replace('title: "','title: "Revised ',1));unrelated=compile_req(source)
    results['unrelated-retention']=analyze(a,unrelated,retention)
    assert results['unrelated-retention'].startswith('stale=;')
    for p in source.glob('*.mreq.yaml'):p.write_text(p.read_text().replace('SYS-009','SYS-009-NEW'))
    corrected=compile_req(source);results['id-correction']=analyze(a,corrected,coverage)
    assert results['id-correction']=='missing:SYS-009'
    results['uncovered']=analyze(a,a,[r for r in coverage if r['requirement_id']!='SYS-009'])
    invalid=copy.deepcopy(a);invalid['complete']=False;results['incomplete']=analyze(a,invalid,coverage)
    # Broad source-set/Git-tree binding notices comment/move, semantic comparison does not.
    assert a['sources']!=comment['sources']!=moved['sources'] and basis(a)==basis(comment)==basis(moved)
    p=BUILD/'results.json';p.write_text(json.dumps(results,sort_keys=True,indent=2)+'\n')
    return a,b,results


if __name__=='__main__':
    a,b,results=run()
    expected=HERE/'expected/results.json'
    if not expected.exists():raise SystemExit('Review build/experiment-0028/results.json before freezing expectations')
    assert json.loads(expected.read_text())==results
    print(f'PASS verification design: {len(basis(a))} requirements, {len(table("coverage.tsv"))} assertions, 8 change/failure cases')
