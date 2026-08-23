# Task TC-0401: Run the Formatting Policy Experiment

Status: Planned

Roadmap stage: 4

Type: Experiment and decision

Depends on: TC-0203

Unlocks: TC-0402

## Question

Which minimum canonicalization rules reduce diff noise without erasing useful source structure or changing semantics?

## Outcome

A recorded comparison selects or rejects conservative structural formatting, prose reflow, line-ending normalization, and source-order policies.

## Work

- Create varied but valid fixtures covering wrapping, blank lines, CRLF, comments, multiple records, and opaque LaTeX payloads.
- Compare a conservative policy with at least one prose-reflow alternative.
- Inspect every candidate rewrite using ordinary unified and word-level Git diffs.
- Define expected handling of record order, relationship order, comments, final line endings, and math payloads.
- Select check, standard-output, and possible in-place operating modes.

## Acceptance evidence

- The experiment records concrete before-and-after fixtures and diff observations.
- Every selected rewrite has a semantic-preservation argument.
- Comment behavior does not invent semantic attachment.
- Opaque math payload preservation is explicit.
- Rejected formatting choices and their reasons are retained.

## Out of scope

- Implementing the formatter.
- Repairing invalid source.
- Sorting requirements into a document view.
- Styling rendered output.

## Completion decision

Select the smallest policy that creates material consistency. If only a narrow subset is clearly safe, ship a narrow formatter rather than expanding comment or source semantics.

## References

- [Roadmap Stage 4](0001-initial-roadmap.md#stage-4--define-and-deliver-mundanereq-format)
- [TC-0202](task-0202-design-the-shared-source-representation.md)
- [Source-comment experiment](../experiments/0007-source-comments/README.md)
