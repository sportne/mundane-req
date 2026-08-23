# Task TC-0903: Run a Derived Presentation Experiment

Status: Conditional

Roadmap stage: 9

Type: Rendering experiment

Depends on: TC-0603 and the TC-0807 disposition

Unlocks: A renderer or view decision

## Question

Can a useful specification, matrix, or browser be generated without making presentation structure authoritative?

## Outcome

One disposable presentation is generated from source and evaluated against a concrete delivery or review workflow.

## Work

- Select one output such as HTML specification, traceability matrix, or requirement browser.
- Start with deterministic derived ordering and no new authoritative view syntax.
- Render statement prose and opaque LaTeX content without changing their source ownership.
- Include source IDs and trace navigation needed by the selected workflow.
- Delete and reproduce the output from a clean tagged checkout.
- Compare the result with the TC-0807 composition decision.

## Acceptance evidence

- The output is reproducible and disposable.
- Every displayed requirement value traces to readable source.
- Rendering failure does not prevent understanding or editing requirements.
- Any required authored ordering is explicitly identified rather than hidden in code.
- The experiment determines whether a standalone renderer merits a task.

## Out of scope

- A hosted requirements database.
- A mandatory web editor.
- General document-publishing features.

## Completion decision

Build a maintained renderer only if the presentation materially serves the selected workflow. Do not standardize a view language solely because a renderer can consume one.

## References

- [TC-0807](task-0807-test-authored-views-and-specifications.md)
- [Roadmap derived presentation milestone](0001-initial-roadmap.md#derived-presentation-milestone)
