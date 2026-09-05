# Task TC-1108: Format YAML Source Without Content Loss

Status: Planned

Roadmap stage: 11

Type: Implementation

Depends on: TC-1107

Unlocks: TC-1109

## Question

Generic YAML emitters in Experiment 0025 lost comments; node emission also failed
idempotence. Authoritative source needs a deliberately bounded formatter.

## Outcome

A specified, conservative YAML formatting path preserves semantic values and
source comments while reporting write/output failures accurately.

## Work

- Select the smallest transformations justified by authoring needs, beginning
  with the proven line-ending policy. Broader normalization needs new evidence.
- Retain concrete source/comments and validate the full selected source set before
  rewriting. Do not serialize semantic objects as a substitute for source editing.
- Integrate formatter check/write modes and current safety behavior. Reconcile
  snapshot protection and output failures with TC-1401/TC-1402 without duplicating
  their independent work or claiming their future fixes are already present.
- Add inline/boundary comments, quoted values, folded/literal scalars and math
  payloads to preservation fixtures, including the failed emitter examples.

## Acceptance evidence

- Parse-format-parse equality, exact comment preservation under the documented
  policy and byte idempotence pass for every supported transformation.
- Invalid input does not produce a claimed successful rewrite. Tests cover CRLF,
  source changes, partial multi-file outcomes and failing output/write paths under
  the adopted safety contract; any remaining races are explicitly documented.
- Check and write modes report deterministic results on JVM/native. The formatter
  contract lists supported transformations and deliberate non-transformations.

## Out of scope

No general YAML pretty-printer, arbitrary field sorting, source migration utility,
new attributes or unrelated formatter cleanup.

## Compatibility, affected components and completion decision

Likely components: concrete source representation, SourceFormatter, formatter CLI,
fixtures and formatter contract. Current custom formatting stays available according
to TC-1106's compatibility policy. Stop rather than rewrite when source/comment
fidelity cannot be demonstrated; a narrower formatter is an acceptable outcome.

## References

- [Roadmap](0001-initial-roadmap.md)
- [Representation decision](../research/0033-yaml-source-representation-decision.md)
- [Comparison and specification outline](../experiments/0025-yaml-source-comparison/README.md)
- [TC-1107](task-1107-interpret-and-validate-yaml-requirement-source.md)
