# Task TC-0104: Select the Maintained Implementation Lineage

Status: Ready

Roadmap stage: 1

Type: Decision

Depends on: TC-0103

Unlocks: TC-0201

## Question

Should the first maintained implementation evolve from the experimental probe or be constructed alongside it and compared through conformance evidence?

## Outcome

A short decision record selects the implementation lineage and states which experiment code may be reused, retained as an oracle, or retired.

## Work

- Assess the 772-line probe's coupling among CLI, source discovery, parsing, validation, model, inventory, and trace query.
- Assess whether incremental extraction can preserve test evidence more safely than a parallel implementation.
- Identify ReqIF probe duplication that should not dictate the core architecture.
- Compare the smallest transition paths against formatter and separate-executable needs.
- Record the selected path and rollback condition.

## Acceptance evidence

- The decision names the maintained source location to be created by TC-0201.
- Reuse is justified component by component rather than by implementation convenience alone.
- Historical experiment reproducibility is preserved.
- The decision does not claim a stable Java API.

## Out of scope

- Performing the extraction.
- Selecting generalized compiler frameworks.
- Changing executable behavior or source semantics.

## Completion decision

Favor evolution when behavior can remain continuously covered; favor a parallel implementation when extracting concrete syntax and tool boundaries would otherwise distort the experiment. Do not maintain two implementations indefinitely without a stated comparison purpose.

## References

- [TC-0103](task-0103-audit-the-0.2-conformance-baseline.md)
- [Experiment probe source](../experiments/0002-deterministic-interpretation/src/mundanereq/Probe.java)
- [Roadmap implementation hypothesis](0001-initial-roadmap.md#first-implementation-hypothesis)
