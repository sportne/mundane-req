# Task TC-1002: Define Compatibility and Publish or Defer 1.0

Status: In progress

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
- Evaluate TC-1001's no-feature, semantics-identical source-language candidate
  as the only publication candidate; do not add features to manufacture
  release maturity.
- Restore the formatter verification's maintained-source-set inventory to
  include the valid experiment corpora added after Experiment 0011, then require
  the complete `make verify` run to pass before publication.
- Decide explicitly whether the absent independent-human authoring,
  normal-review, and real formal-traceability workflow evidence is acceptable
  for a source-compatibility release; otherwise defer publication and create
  bounded evidence cards.
- Define repository contract selection and version coexistence behavior.
- State validator, formatter, and trace CLI promises separately.
- State that shared Java APIs and interchange profiles have their own policies.
- Reconcile normative standard, conformance fixtures, README, roadmap, and release tags.
- Publish the explicit non-goals and migration expectations—or name the blockers and next cards.

## Acceptance evidence

- No compatibility promise depends on hidden implementation state.
- An independent implementation can understand the normative source promise.
- The release or deferral is reproducibly identified by Git.
- A clean-checkout `make verify` run covers every maintained valid source corpus
  and passes before the 1.0 publication commit or tag is finalized.
- The release or deferral records how the unmet independent-human,
  normal-review, and real-workflow criteria affect the decision and forbids
  unsupported usability or operational-maturity claims.
- All documentation agrees on current status.
- A deferral has bounded blockers rather than an indefinite aspiration.

## Out of scope

- Freezing every executable output byte.
- Guaranteeing future ecosystem tools.
- Certification or qualification claims.

## Completion decision

Publish 1.0 only if preserving its contract is a deliberate long-term commitment. Otherwise prefer an honest provisional successor over nominal stability.

## Progress

Documentation reconciliation completed on 2026-08-30. The README, strategic
roadmap, project foundation, minimum-model design record, provisional 0.2
contract, conformance entry points, and documentation indexes now describe the
same current state. This does not clear the formatter verification blocker or
make the publication-or-deferral decision.

## References

- [TC-1001](closed/task-1001-audit-readiness-for-1.0.md)
- [Readiness audit](../research/0031-source-1.0-readiness-audit.md)
- [Current 0.2 contract](../specification/0006-provisional-0.2-contract.md)
- [Roadmap 0001](0001-initial-roadmap.md)
