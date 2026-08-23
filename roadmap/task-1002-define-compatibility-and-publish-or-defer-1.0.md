# Task TC-1002: Define Compatibility and Publish or Defer 1.0

Status: Conditional

Roadmap stage: 10

Type: Specification and release decision

Depends on: TC-1001

Unlocks: A stable 1.x roadmap or explicit provisional continuation

## Question

Exactly which source and tool behaviors will the project preserve, and should that promise be named 1.0 now?

## Outcome

The project either publishes a scoped 1.0 source contract with separate tool compatibility policies or records a concrete deferral plan.

## Work

- Define source acceptance and semantic-interpretation compatibility.
- Define repository contract selection and version coexistence behavior.
- State validator, formatter, and trace CLI promises separately.
- State that shared Java APIs and interchange profiles have their own policies.
- Reconcile normative standard, conformance fixtures, README, roadmap, and release tags.
- Publish the explicit non-goals and migration expectations—or name the blockers and next cards.

## Acceptance evidence

- No compatibility promise depends on hidden implementation state.
- An independent implementation can understand the normative source promise.
- The release or deferral is reproducibly identified by Git.
- All documentation agrees on current status.
- A deferral has bounded blockers rather than an indefinite aspiration.

## Out of scope

- Freezing every executable output byte.
- Guaranteeing future ecosystem tools.
- Certification or qualification claims.

## Completion decision

Publish 1.0 only if preserving its contract is a deliberate long-term commitment. Otherwise prefer an honest provisional successor over nominal stability.

## References

- [TC-1001](task-1001-audit-readiness-for-1.0.md)
- [Current 0.2 contract](../specification/0006-provisional-0.2-contract.md)
- [Roadmap 0001](0001-initial-roadmap.md)
