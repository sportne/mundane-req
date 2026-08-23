# Task TC-0203: Extract the Semantic Parser and Diagnostics

Status: Planned

Roadmap stage: 2

Type: Implementation

Depends on: TC-0202

Unlocks: TC-0204, TC-0401, and TC-0501

## Question

Can all semantic consumers share one 0.2 interpretation without depending on an experimental CLI or one another?

## Outcome

Maintained components perform source discovery, parsing, semantic conversion, source-set validation, and diagnostic production independently of executable entry points.

## Work

- Implement or extract deterministic explicit-input source discovery.
- Implement concrete parsing and semantic conversion under the audited 0.2 rules.
- Build the source-set identity and outgoing-relationship index.
- Represent diagnostics independently of plain-text rendering.
- Keep policy checks and reverse trace analysis outside core language validity.

## Acceptance evidence

- The maintained parser matches normative inventories for all valid conformance corpora.
- It rejects representative invalid fixtures at conforming source positions.
- Library components have no dependency on validator, formatter, or trace CLI packages.
- No persistent state or hidden repository configuration is required.

## Out of scope

- Stable Java APIs.
- Formatting rewrites.
- Trace query interfaces.
- ReqIF import or export.

## Completion decision

If a shared component is used by only one executable and adds abstraction rather than consistency, keep it local to that executable.

## References

- [TC-0202](task-0202-design-the-shared-source-representation.md)
- [Minimum model rationale](../specification/0002-minimum-source-language-and-model.md)
- [0.2 language standard](../specification/0005-mundanereq-source-language-0.2.md)
