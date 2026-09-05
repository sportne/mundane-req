#!/usr/bin/env python3
"""Reproduce both safety bugs at the fixed pre-batch commit and compare maintained fixes."""
from pathlib import Path
import subprocess
root = Path(__file__).resolve().parents[2]
build = root / 'build/yaml-batch-regression'
build.mkdir(parents=True, exist_ok=True)
baseline = '1c6ec7f'
paths = subprocess.check_output(['git', 'ls-tree', '-r', '--name-only', baseline, 'src/main/java'], cwd=root, text=True).splitlines()
for name in paths:
    path = build / 'before-src' / name
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(subprocess.check_output(['git', 'show', baseline + ':' + name], cwd=root))
subprocess.run(['javac', '--release', '21', '-d', str(build / 'before-classes'),
                *map(str, (build / 'before-src').rglob('*.java'))], check=True)
subprocess.run(['javac', '--release', '21', '-cp', str(build / 'before-classes'), '-d', str(build / 'proof'),
                str(Path(__file__).parent / 'src/SafetyRegressionProof.java')], check=True)
results = []
for classes in [str(build / 'before-classes'), str(root / 'build/maintained/classes') + ':' + str(root / 'build/dependencies/snakeyaml-engine-3.1.1.jar')]:
    results.append(subprocess.check_output(['java', '-cp', classes + ':' + str(build / 'proof'), 'mundanereq.cli.SafetyRegressionProof'], text=True).strip())
expected = [
    'validator failed-output status=0; formatter changed-source status=0; external edit preserved=false',
    'validator failed-output status=2; formatter changed-source status=2; external edit preserved=true']
assert results == expected, results
print('\n'.join(results))
print('PASS reproduced safety regressions and observed corrected behavior')
