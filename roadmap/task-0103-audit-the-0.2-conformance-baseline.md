# Task TC-0103: Audit the 0.2 Conformance Baseline

Status: Planned

Roadmap stage: 1

Type: Conformance review

Depends on: TC-0101 and TC-0102

Unlocks: TC-0104

## Question

Does the maintained 0.2 corpus cover the contract well enough to protect the first implementation refactoring?

## Outcome

A written audit maps normative rule areas to positive or negative evidence and records deliberate coverage gaps.

## Work

- Map physical source, discovery, record grammar, fields, prose, math, comments, identity, and relationships to fixtures or tests.
- Confirm comments remain absent from semantic inventories and ReqIF output.
- Identify accidental probe CLI behavior that is not part of language conformance.
- Classify uncovered cases as required before refactoring, useful later, or intentionally outside the fixture suite.
- Update conformance documentation where expected behavior is implicit.

## Acceptance evidence

- The audit is committed as a research or conformance record.
- Every normative rule area has evidence or an explicit gap disposition.
- No experimental inventory or diagnostic wording is accidentally declared normative.
- Both contract versions remain reproducible from their existing tags.

## Out of scope

- Attempting exhaustive combinatorial grammar testing.
- Defining the future tool CLI.
- Promoting the probe to maintained architecture.

## Completion decision

Proceed only if the audit finds no unresolved ambiguity that could make two conforming implementations assign different semantic values.

## References

- [TC-0102](task-0102-correct-reference-parser-conformance.md)
- [0.2 trial contract](../specification/0006-provisional-0.2-contract.md)
- [Source-comment experiment](../experiments/0007-source-comments/README.md)
