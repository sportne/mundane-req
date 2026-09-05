"""Serialized-only experimental consumer; intentionally Python standard library only."""
import json
import sys
from pathlib import Path


def analyze(imports, plan):
    if plan['format'] != 'experiment-0027-plan-1' or not plan['complete']:
        return ['analysis: incomplete or unsupported plan']
    for scope, artifact in imports.items():
        if artifact['format'] != 'experiment-0027-requirements-1' or not artifact['complete']:
            return [f'interpretation: {scope} incomplete or unsupported']
    findings = []
    for row in plan['coverage']:
        scopes = [row['scope']] if row['scope'] else sorted(imports)
        matches = [(scope, r) for scope in scopes for r in imports.get(scope, {}).get('requirements', []) if r['id'] == row['target']]
        location = row['location']
        if len(matches) != 1:
            findings.append(f"link: {location} {row['target']} {'missing' if not matches else 'ambiguous'}")
            continue
        scope, requirement = matches[0]
        prior = row['basis']
        state = 'current' if requirement == prior else 'review-stale'
        findings.append(f"planned: {location} {row['activity']} -> {scope}:{row['target']} [{state}] context={plan['context']}")
    return findings


if __name__ == '__main__':
    imports = json.loads(Path(sys.argv[1]).read_text())
    plan = json.loads(Path(sys.argv[2]).read_text())
    print('\n'.join(analyze(imports, plan)))
