# Task TC-0806: Test Reusable Trace Policies

Status: Conditional

Roadmap stage: 8

Type: Policy experiment

Depends on: TC-0603 and observed completeness needs

Unlocks: A policy/tool boundary decision

## Question

Which trace rules are reusable project policies rather than universal source-language validity?

## Outcome

Example policies for coverage, cycles, vocabularies, or verification distinguish configuration, diagnostics, and applicability from parsing.

## Work

- Select two or three policies required by actual workflows.
- Define the source set and baseline to which each policy applies.
- Run each policy against conforming source that both passes and fails it.
- Specify diagnostics that identify policy separately from language conformance.
- Assess whether policy needs configuration or can be an explicit command invocation.

## Acceptance evidence

- Conforming source may fail policy without being called syntactically invalid.
- Each policy states scope, rationale, and false-positive risk.
- Policy data remains human-readable and versioned if authoritative.
- The validator core need not understand every project convention.

## Out of scope

- An arbitrary rule language.
- A plugin architecture.
- Universalizing one organization's assurance process.

## Completion decision

Implement only policies demonstrated to recur. Prefer explicit focused checks before inventing generalized configuration.

## References

- [Roadmap trace-policy study](0001-initial-roadmap.md#trace-policy)
- [0.2 contract policy guidance](../specification/0006-provisional-0.2-contract.md#ci-trial-guidance)
