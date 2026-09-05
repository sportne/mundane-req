"""Bounded rebuild/change experiment. Outputs live only under build/experiment-0027."""
import copy
import csv
import hashlib
import json
import shutil
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
BUILD = ROOT / 'build/experiment-0027'
CP = str(ROOT/'build/maintained/classes') + ':' + str(ROOT/'build/dependencies/snakeyaml-engine-3.1.1.jar')


def dump(value):
    return json.dumps(value, sort_keys=True, ensure_ascii=False, separators=(',', ':')) + '\n'


def compile_requirements(source):
    subprocess.run(['git', 'init', '-q', str(source)], check=True)
    subprocess.run(['git', '-C', str(source), 'add', '-A'], check=True)
    tree = subprocess.check_output(['git', '-C', str(source), 'write-tree'], text=True).strip()
    raw = subprocess.check_output(['java', '-cp', str(BUILD/'classes')+':'+CP, 'RequirementAdapter', str(source)], text=True)
    artifact = json.loads(raw)
    artifact['provenance'] = {
        'sourceContract': 'mundanereq-yaml-0.3', 'gitTree': tree,
        'adapter': hashlib.sha256((HERE/'src/RequirementAdapter.java').read_bytes()).hexdigest(),
        'interpreter': hashlib.sha256((ROOT/'src/main/java/mundanereq/Interpreter.java').read_bytes()+(ROOT/'src/main/java/mundanereq/YamlRequirements.java').read_bytes()).hexdigest(),
        'sources': [{'path': p.relative_to(source).as_posix(), 'sha256': hashlib.sha256(p.read_bytes()).hexdigest()} for p in sorted(source.glob('*.mreq.yaml'))],
    }
    return artifact


def build():
    if BUILD.exists():
        shutil.rmtree(BUILD)
    (BUILD/'classes').mkdir(parents=True)
    subprocess.run(['javac', '--release', '21', '-cp', CP, '-d', str(BUILD/'classes'), str(HERE/'src/RequirementAdapter.java')], check=True)
    source = BUILD/'source'
    shutil.copytree(ROOT/'examples/yaml/vaccine-monitoring', source)
    baseline_b = compile_requirements(source)
    assert baseline_b['complete'] and len(baseline_b['requirements']) == 57
    # Replay pilot CR-001 A -> B: only the two retained-record obligations differ
    # in this focused projection; the original pilot also changed rationale/citations.
    for filename in ['system.mreq.yaml', 'remote.mreq.yaml']:
        p = source/filename
        p.write_text(p.read_text().replace('at least five years', 'at least three years'))
    baseline_a = compile_requirements(source)
    other_source = BUILD/'other-checkout'
    shutil.copytree(source, other_source, ignore=shutil.ignore_patterns('.git'))
    other = compile_requirements(other_source)
    assert other == baseline_a, 'checkout path leaked into normalized artifact'
    rows = list(csv.DictReader((HERE/'fixtures/coverage.tsv').open(), delimiter='\t'))
    plan = {'format': 'experiment-0027-plan-1', 'complete': True, 'context': 'pilot-vaccine-store',
            'provenance': {'carrier': 'pilot TSV projection', 'sha256': hashlib.sha256((HERE/'fixtures/coverage.tsv').read_bytes()).hexdigest()},
            'coverage': [{'scope': 'product', 'target': r['requirement_id'], 'activity': r['activity_id'],
                          'location': 'coverage.tsv:'+str(i+2),
                          'basis': next(v for v in baseline_a['requirements'] if v['id'] == r['requirement_id'])} for i,r in enumerate(rows)]}
    cases = {}
    def check(name, imports, chosen_plan=plan):
        (BUILD/'imports.json').write_text(dump(imports))
        (BUILD/'plan.json').write_text(dump(chosen_plan))
        text = subprocess.check_output(['python3', str(HERE/'consumer.py'), str(BUILD/'imports.json'), str(BUILD/'plan.json')], cwd=BUILD, text=True)
        cases[name] = text
        return text
    assert 'review-stale' not in check('baseline-a', {'product': baseline_a})
    assert check('normative-edit', {'product': baseline_b}).count('review-stale') == 2
    original = (source/'device.mreq.yaml').read_text()
    # Ensure an unrelated requirement value is actually changed.
    (source/'device.mreq.yaml').write_text(original.replace('title: "', 'title: "Revised ', 1))
    unrelated = compile_requirements(source)
    assert unrelated['requirements'] != baseline_a['requirements']
    assert 'review-stale' not in check('unrelated-edit', {'product': unrelated})
    (source/'device.mreq.yaml').write_text(original)
    p = source/'system.mreq.yaml';p.write_text('# review comment\n'+p.read_text())
    comment = compile_requirements(source)
    assert comment['requirements'] == baseline_a['requirements'] and comment['provenance'] != baseline_a['provenance']
    assert 'review-stale' not in check('comment-edit', {'product': comment})
    p.rename(source/'moved.mreq.yaml')
    moved = compile_requirements(source)
    assert moved['requirements'] == baseline_a['requirements'] and moved['provenance'] != comment['provenance']
    assert 'review-stale' not in check('file-move', {'product': moved})
    # Atomic source ID correction includes references, but external plan stays stale.
    for p in source.glob('*.mreq.yaml'): p.write_text(p.read_text().replace('SYS-009', 'SYS-009-CORRECTED'))
    corrected = compile_requirements(source)
    assert corrected['complete']
    assert 'SYS-009 missing' in check('id-correction', {'product': corrected})
    missing = copy.deepcopy(plan);missing['coverage'][0]['target'] = 'ABSENT'
    assert 'ABSENT missing' in check('missing-target', {'product': baseline_a}, missing)
    assert 'ambiguous' not in check('qualified-duplicate-scopes', {'product': baseline_a, 'other': other})
    unqualified = copy.deepcopy(plan);unqualified['coverage'][0]['scope'] = ''
    assert 'ambiguous' in check('unqualified-duplicate-scopes', {'product': baseline_a, 'other': other}, unqualified)
    for p in source.glob('*.mreq.yaml'): p.write_text(p.read_text().replace('SYS-009-CORRECTED', 'SYS-009'))
    p=source/'needs.mreq.yaml';p.write_text(p.read_text().replace('  - id: "NEED-001"', '  - id: "NEED-001"\n    decomposes: ["NEED-001"]'))
    cycle = compile_requirements(source)
    assert cycle['complete']
    assert 'review-stale' not in check('relationship-cycle', {'product': cycle})
    assert len({a['provenance']['gitTree'] for a in [baseline_a, baseline_b, unrelated, comment, moved]}) == 5
    p.write_text(p.read_text()+'unknown: "bad"\n')
    invalid = compile_requirements(source)
    assert not invalid['complete'] and not invalid['requirements']
    assert 'interpretation:' in check('invalid-source', {'product': invalid})
    broken_plan=copy.deepcopy(plan);broken_plan['complete']=False
    assert 'analysis:' in check('invalid-plan', {'product': baseline_a}, broken_plan)
    unsupported=copy.deepcopy(baseline_a);unsupported['format']='unknown'
    assert 'interpretation:' in check('unknown-format', {'product': unsupported})
    results = ''.join('CASE '+name+'\n'+value for name,value in cases.items())
    (BUILD/'baseline-a.json').write_text(dump(baseline_a))
    (BUILD/'baseline-b.json').write_text(dump(baseline_b))
    (BUILD/'results.txt').write_text(results)
    return results, dump(baseline_a), dump(baseline_b)


if __name__ == '__main__':
    first = build()
    second = build()  # Deletes every derived output and rebuilds from recorded source.
    assert first == second, 'rebuild changed artifact/report bytes'
    expected = HERE/'expected/results.txt'
    if expected.exists(): assert expected.read_text() == first[0], 'report golden mismatch'
    else: raise SystemExit('Missing reviewed expected/results.txt; actual is under build/experiment-0027')
    print('PASS experiment 0027: 57 requirements, 13 cases, two identical clean rebuilds; serialized-only consumer')
