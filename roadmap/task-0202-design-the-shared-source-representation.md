# Task TC-0202: Design the Shared Source Representation

Status: Planned

Roadmap stage: 2

Type: Design experiment and implementation

Depends on: TC-0201

Unlocks: TC-0203

## Question

What concrete source information must be retained so semantic tools and a safe formatter can share parsing without making trivia semantic?

## Outcome

A deliberately small physical and concrete source representation preserves required locations, record structure, comments, and opaque payloads separately from the requirement model.

## Work

- Inventory information consumed by validation, formatting, and trace analysis.
- Represent physical lines, source spans, structural elements, and permitted comments only as needed by real consumers.
- Keep comment proximity and whitespace out of requirement semantics.
- Preserve opaque math payload characters and line structure.
- Demonstrate conversion from concrete representation to the existing normalized semantic inventory.

## Acceptance evidence

- Commented and comment-free 0.2 fixtures produce equal semantic models.
- The representation can reproduce comment text and relative order needed by a conservative formatter.
- The semantic model contains no formatting trivia.
- Every retained concrete-syntax concept has an identified consumer.

## Out of scope

- A general lossless syntax framework.
- Comment attachment semantics.
- Formatter policy or prose reflow.
- Public Java API stability.

## Completion decision

If faithful formatting needs inferred attachment or generalized token machinery, narrow the formatter hypothesis before broadening the representation.

## References

- [TC-0201](task-0201-establish-the-maintained-java-project.md)
- [Source-comment decision](../research/0009-nonsemantic-source-comments.md)
- [Roadmap formatter stage](0001-initial-roadmap.md#stage-4--define-and-deliver-mundanereq-format)
