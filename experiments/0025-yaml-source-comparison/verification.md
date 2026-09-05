# Completion verification

Executed locally on 2026-09-04. This records actual checks, not future acceptance
claims for maintained YAML tools.

| Check | Result |
| --- | --- |
| Pinned setup and Java helper compilation | Passed with Java 21, `--release 21 -Xlint:all -Werror` |
| `run.sh` golden replay | Passed: 11 paired records, 28 pressure cases, 12 workflow outcomes |
| Native parser/emitter build and parity replay | Passed: 43 comparisons; schema/domain checks remain Python |
| Frozen fixture regeneration | Re-running both preparation scripts produced identical bytes |
| Maintained `make verify` | Passed with GraalVM CE 21.0.2; [captured PASS lines](results/maintained-verification.txt) |
| Shell syntax and Python parse checks | Passed for experiment scripts |
| Changed Markdown relative links | All resolved; no separate repository documentation lint command was found |
| Task inventory and graph | 66 unique task IDs, one status-table entry each, matching statuses, existing dependencies and no cycles; Ready prerequisites complete |
| Change scope | Experiment, research and roadmap only; production source, maintained tests, normative specification, Makefile and CI unchanged |
| Planning-language review | No new milestone-oriented plan, readiness gate or stability promise; retained historical references unchanged |
| `git diff --check` | Reports only intentionally retained evidence listed below; scoped check excluding those files passes |

The raw diff checker reports trailing spaces in the empty-field and trailing-space
negative custom fixtures; unified-diff context lines; SnakeYAML node-emitter
captures; and actual conflict markers in the two conflicting merge captures.
Removing those bytes would corrupt the evidence. They are not source changes or
unresolved repository merge conflicts. No whitespace exceptions were applied to
production files or global Git configuration.

The task-card planning commit `ea463f8` was pushed before executing the experiment.
The comparison's source provenance remains pinned to that commit.
