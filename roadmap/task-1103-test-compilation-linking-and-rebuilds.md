# Task TC-1103: Test Compilation, Linking, and Rebuilds

Status: Planned

Roadmap stage: 11

Type: Experiment and decision

Depends on: TC-1102

Unlocks: TC-1201, TC-0905

## Question

Can the existing verification-plan workflow be built from compiled artifacts and
explicit linking with a smaller, inspectable contract than a general platform?

## Outcome

A bounded experimental requirements/verification pipeline compares representations
and records the minimum interfaces needed for maintained compilation and linking.

## Work

- Reuse a small selection from Experiment 0024 plus its two-baseline change case.
  Produce experimental requirement and plan representations, resolve references,
  calculate planned coverage/staleness, and generate one plain report.
- Use a small adapter for the experimental plan carrier; compare shared notation
  with separate representations only where it affects this workflow.
- Delete generated outputs and rebuild from explicit inputs. Capture source,
  compiler/adapter, format, and resolved dependency provenance.
- Exercise a normative edit, unrelated edit, comment edit, file move, ID correction,
  missing target, duplicate ID across imported scopes, and a relationship cycle.
- Distinguish local interpretation, linking, and analysis failures. A partially
  interpreted artifact must not masquerade as a complete input.
- Compare Git revision binding and narrowly scoped content comparison for TC-0905;
  identify which experiment-specific mechanics should be discarded.

## Acceptance evidence

- Checked-in source fixtures, experimental scripts, expected reports, and commands
  reproduce the successful build and every negative/change case.
- One consumer reads the compiled requirement representation without importing
  requirement-parser internals.
- Each reported impact traces to an authored reference; possible impact does not
  automatically assert invalidity, failed verification, or requirement satisfaction.
- Repeated builds from the same recorded inputs produce identical semantic outputs
  and report content under documented path/provenance normalization.
- A decision record compares alternatives, names required common fields, and
  enables TC-1201 without claiming the experiment is a public artifact contract.

## Out of scope

- Production compiler/linker APIs, new core syntax, test execution, safety tooling,
  a remote registry, a new build system, or a universal graph query language.

## Compatibility and affected components

Reuse the pilot and maintained interpreter as experimental inputs without changing
their contracts. New experiment outputs remain derived. Human-authored requirement
IDs remain identity; any digest describes content under explicit comparison rules,
changes with relevant content edits, and is not added to authored requirements.
Likely components: a new bounded experiment, existing pilot fixtures, inventory
utility, and verification-plan adapters.

## Completion decision

Select maintained contracts only if the complete change loop works with explicit
inputs and understandable source. Narrow or stop the common integration layer if
a generalized type system or mandatory platform is needed for this first consumer.

## References

- [Roadmap](0001-initial-roadmap.md)
- [TC-1102](task-1102-define-requirement-and-assertion-ownership.md)
- [Pilot assessment](../experiments/0024-vaccine-monitoring-pilot/assessment.md)
- [Pilot decision](../research/0032-end-to-end-pilot-decision.md)
- [TC-0905](task-0905-define-verification-analyzer-contract.md)
