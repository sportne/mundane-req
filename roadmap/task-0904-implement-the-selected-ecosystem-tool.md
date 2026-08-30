# Task TC-0904: Implement the Selected Ecosystem Tool

Status: Planned

Current disposition: Experiment 0024 supplied the repeated workflow evidence
that TC-0901 required and selected a focused verification-plan analyzer. TC-0905
must define and survive a bounded contract experiment before implementation is
unlocked.

Roadmap stage: 9

Type: Implementation

Depends on: TC-0905

Unlocks: TC-1001

## Question

Can the single selected ecosystem capability be delivered as another replaceable tool without expanding the core platform?

## Outcome

One evidence-backed tool is implemented, verified, and documented with an explicit relationship to authoritative source and existing infrastructure.

## Work

- Write a bounded contract from the selecting experiment.
- Reuse shared Java components only where semantic consistency requires it.
- Keep the executable or integration independently usable and removable.
- Ensure outputs are disposable or explicitly external interchange.
- Build with Java 21 and GraalVM Native Image when it is a core command-line tool.
- Run the workflow that justified selection.

## Acceptance evidence

- The tool solves the stated problem in the recorded workflow.
- Removing it does not make authoritative requirements unintelligible.
- It does not absorb unrelated validator, formatter, trace, forge, or editor responsibilities.
- Generated state is reproducible.
- Maintenance and compatibility boundaries are documented separately.

## Out of scope

- Implementing unselected Stage 9 candidates.
- A generalized plugin system.
- A monolithic umbrella command.

## Completion decision

Publish only if the implementation remains focused after real use. If it accumulates unrelated capabilities, split or stop it before release.

## References

- [TC-0901](closed/task-0901-prioritize-the-next-ecosystem-tool.md)
- [TC-0905](task-0905-define-verification-analyzer-contract.md)
- [Pilot decision](../research/0032-end-to-end-pilot-decision.md)
- [Roadmap product direction](0001-initial-roadmap.md#product-direction)
