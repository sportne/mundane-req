# Experiment 0027: Compile, link, analyze and rebuild

Run from repository root after `make test`:

```sh
python3 experiments/0027-compilation-linking/run.py
```

The maintained YAML interpreter compiles the 57-requirement maintained pilot copy.
The two coverage rows are copied from Experiment 0024's PLAN-B ACT-RETENTION
entries for SYS-009 and RDS-002. A focused baseline A changes those two statements
from five to three years; baseline B restores the pilot CR-001 obligations. This
is a controlled projection, not a reconstruction of every historical baseline-A
rationale/citation. The plan adapter binds full requirement values as its review
basis, records row locations/context, and deliberately retains experimental TSV.

RequirementAdapter is an experimental serialization boundary. consumer.py reads
only JSON, uses the Python standard library, and runs in an output directory
without Java/parser imports. The runner is orchestration and may access source;
the consumer may not. It compiles separately initialized local Git checkouts to
prove checkout-independent normalization and qualified duplicate IDs. Git tree
objects bind exact selected file contents/paths; no commits or invented author
identities are needed. Compiler/adapter hashes and source checksums are provenance,
not human ID replacements. No digest is written into authored requirements.

The runner deletes only build/experiment-0027, then builds twice and compares
artifact/report bytes. [Expected reports](expected/results.txt) cover 13 cases:
A, normative change, unrelated change, comments, move, corrected ID, missing target,
qualified/unqualified duplicate scopes, a self relationship cycle, invalid source,
incomplete plan, unknown compiled format. Local syntax failure suppresses compiled
records; link failures are distinguished from unsupported/incomplete analysis.
The cycle is legal and does not imply a cyclic build dependency.

Only two requirements are planned-covered in this projection. “Current” means
unchanged comparison basis, not approved/satisfied/passing. A missing target or
ambiguous reference never becomes an assertion of failed verification. Outputs
are derived. The experiment does not implement a maintained linker or select
verification-plan syntax; its private JSON formats may be discarded.

The later maintained compiler implementation is verified by `make compiled-verify`
and `make verify`. [Captured full-gate results](results/maintained-compiler-verification.txt)
record that TC-1202 follow-up separately from the experiment's private format.
