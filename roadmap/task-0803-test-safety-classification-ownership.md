# Task TC-0803: Test Safety Classification Ownership

Status: Conditional

Roadmap stage: 8

Type: Model-pressure experiment

Depends on: TC-0603 and a safety-classification workflow

Unlocks: A safety classification ownership decision

## Question

Is safety criticality authoritative requirement content, a relation, or a baseline-bound assessment assertion?

## Outcome

An evidence record compares inline attributes with separate classification assertions under realistic changes of context and analysis.

## Work

- Select a named safety scheme and classify a bounded requirement set.
- Represent the same facts inline and in a separate assessment artifact.
- Change a classification because hazard analysis changes while requirement text does not.
- Exercise multiple variants or schemes and record rationale, source, and assessor authority.
- Compare ordinary diffs, traceability, review ownership, and derived inline display.

## Acceptance evidence

- The comparison identifies the full arguments needed to make each classification true.
- Exactly one authoritative representation exists in each candidate.
- The external-artifact candidate is richer than a bare list-by-level where provenance is needed.
- The decision distinguishes a descriptive tag from a consequential assessed level.

## Out of scope

- A generalized attribute schema.
- Safety certification claims.
- Embedding an entire hazard-analysis model in mundane-req.

## Completion decision

Prefer a separate assessment when classification can change independently, vary by context, or needs evidence. Add an intrinsic field only if the workflow demonstrates one authoritative value whose change is defined as a requirement change.

## References

- [Roadmap safety study](0001-initial-roadmap.md#safety-classification-and-other-assessments)
- [Practice survey](../research/0001-requirements-management-practice-survey.md)
