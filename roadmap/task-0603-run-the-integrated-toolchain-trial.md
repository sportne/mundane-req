# Task TC-0603: Run the Integrated Toolchain Trial

Status: Ready

Roadmap stage: 6

Type: End-to-end trial and decision

Depends on: TC-0602

Unlocks: TC-0701 and prioritized TC-08xx research

## Question

Do three independent native tools collectively improve a software-like requirements workflow while source and Git remain sufficient?

## Outcome

A recorded branch-to-baseline trial evaluates editing, formatting, validation, trace impact, forge-style review, merge, tagging, and reconstruction of derived results.

## Work

- Perform a meaningful multi-requirement change on a branch.
- Run local format, validation, and trace steps before review.
- Inspect ordinary source diffs and simulate or use a normal forge review.
- Merge and establish an annotated baseline according to project convention.
- Rebuild every derived output from the tagged source.
- Repeat the workflow with one tool absent and record the degraded capability.

## Acceptance evidence

- The complete workflow is reproducible from repository history.
- The requirement change remains understandable without specialized output.
- Each tool contributes one separable capability.
- No authoritative data exists only in tool output.
- A decision record names friction by source, tool, Git/forge, policy, or training ownership.

## Out of scope

- Claiming operational scale.
- Adding language features during the trial.
- Building a custom forge integration to conceal friction.

## Completion decision

Proceed to operational and model-pressure trials only if the suite demonstrates additive value without becoming repository infrastructure that must exist to understand source.

## References

- [TC-0602](closed/task-0602-create-the-clean-checkout-ci-workflow.md)
- [Roadmap Stage 6](0001-initial-roadmap.md#stage-6--exercise-the-tools-in-concert)
