# Task TC-0901: Prioritize the Next Ecosystem Tool

Status: Conditional

Roadmap stage: 9

Type: Portfolio decision

Depends on: TC-0603 and TC-0704

Unlocks: TC-0904

## Question

After the core suite and operational trial, which one independent capability removes the most consequential remaining friction?

## Outcome

An evidence comparison selects one next tool or explicitly decides that model research, documentation, or no additional tool has higher value.

## Work

- Collect friction from integrated and operational trials.
- Compare linter, renderer, semantic baseline comparison, model-specific analyzer, editor support, ReqIF conversion, and machine-readable output candidates.
- For each candidate, identify the workflow, authoritative inputs, disposable outputs, and capability already supplied by Git, forge, editor, or CI.
- Estimate conceptual and maintenance cost without designing full architectures.
- Select one bounded experiment and define its stop condition.

## Acceptance evidence

- Every candidate is tied to observed evidence rather than product imitation.
- The selected tool has one primary responsibility.
- No candidate requires owning requirement source or a persistent server by default.
- The decision explains why simpler documentation, policy, or existing infrastructure is insufficient.

## Out of scope

- Implementing the selected tool.
- Committing to the entire ecosystem list.
- Building a plugin platform for future candidates.

## Completion decision

Select at most one next tool. If no candidate materially improves a demonstrated workflow, do not build one.

## References

- [TC-0603](task-0603-run-the-integrated-toolchain-trial.md)
- [TC-0704](task-0704-measure-operational-scale.md)
- [Roadmap Stage 9](0001-initial-roadmap.md#stage-9--add-focused-ecosystem-tools-only-where-use-justifies-them)
