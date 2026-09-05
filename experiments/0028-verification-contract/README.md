# Experiment 0028: Verification plan contract

After `make native-compile`, run `python3 experiments/0028-verification-contract/run.py`.
The experiment consumes published requirement artifacts only. Its 57-requirement
source is the maintained YAML pilot copy; activity/coverage tables are copied from
Experiment 0024. The new plan header makes source version, context and scope explicit.
All covered IDs remain human-authored. No required source notation is selected for
other engineering domains.

The controlled baseline-A projection changes only the two retained-record obligations
from five years to three. Baseline B restores the current pilot statements; historical
rationale/citation changes are not reconstructed. The expected results were reviewed
before maintained analyzer implementation. Eight cases distinguish current/stale,
comment/move/unrelated changes, ID correction, uncovered source and incomplete input.
Experiment 0027 supplies the complementary actual Git-tree comparison.

fixtures/ contains published baseline/current requirement artifacts, a compiled plan
written from the selected contract before its adapter exists, and an exact-pinned
baseline import declaration. These are derived test fixtures, never authored source.
The plan fixture records all original table row locations. Regeneration preserves
explicit source inputs and requires deliberate golden review when contracts change.
The expected result JSON is an experimental summary, not the later analyzer format.
