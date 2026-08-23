# Task TC-0807: Test Authored Views and Specifications

Status: Conditional

Roadmap stage: 8

Type: Model-pressure experiment

Depends on: TC-0603 and a delivery or review workflow requiring composition

Unlocks: TC-0903 and a view-language decision

## Question

Does a real workflow require authored ordering and composition, or is a disposable generated presentation sufficient?

## Outcome

A delivery or review experiment compares ID-sorted generation, tool-specific ordering input, and a minimal authored view artifact without changing requirement identity.

## Work

- Select a concrete specification or report delivery scenario.
- Generate a useful presentation with no standardized view language first.
- Identify where authored sections, ordering, inclusion, or reuse materially affect the workflow.
- Test a minimal separate view candidate only for those needs.
- Exercise requirement movement, reuse in two views, and missing references.

## Acceptance evidence

- The requirement model remains independent of file and document position.
- The experiment distinguishes delivery structure from requirement hierarchy.
- Ordinary source remains understandable without the generated document.
- Any proposed view syntax has fewer concepts than the workflow it replaces.

## Out of scope

- Making Markdown the authoritative requirement container.
- A general publishing system.
- Standardizing layout or styling.

## Completion decision

Defer a view language if a disposable ordering convention serves the workflow. Select a companion view artifact only when authored composition is demonstrably authoritative.

## References

- [Roadmap views study](0001-initial-roadmap.md#views-and-specifications)
- [Non-Markdown view research](../research/0006-non-markdown-view-notation.md)
