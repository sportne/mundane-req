# Task TC-0703: Run the Multi-Author and Layout Trial

Status: Planned

Roadmap stage: 7

Type: Git workflow trial

Depends on: TC-0701

Unlocks: TC-0704

## Question

Do the source language and tool suite remain workable across concurrent changes and different non-semantic file granularities?

## Outcome

A recorded multi-author history compares subject files and one-record-per-file layouts through branches, reviews, conflicts, moves, and baseline comparison.

## Work

- Prepare semantically equivalent corpus layouts.
- Assign separated, overlapping, move-versus-edit, and relationship-retargeting changes to independent branches.
- Run formatter, validator, and trace analysis on each branch.
- Resolve conflicts using ordinary Git and inspect forge-style diffs.
- Compare semantic inventories and author effort across layouts.
- Establish and compare annotated baselines.

## Acceptance evidence

- Equivalent layouts retain equal semantic models.
- Conflict behavior and file-operation overhead are recorded rather than inferred.
- No path or file-count semantics enter the tools.
- Ordinary Git resolution remains sufficient or a precise counterexample is preserved.
- Recommendations remain project conventions unless semantic evidence requires otherwise.

## Out of scope

- Semantic merge machinery.
- Mandating one file granularity.
- Benchmarking every forge.

## Completion decision

If one layout is operationally preferable, document it as guidance without changing the language unless file boundaries demonstrably need semantics.

## References

- [TC-0701](task-0701-select-a-larger-corpus-and-trial-protocol.md)
- [Experiment 0001 merge review](../experiments/0001-source-representations/concurrent-edit-merge-review.md)
