# Research 0040: Requirement semantic output decision

Date: 2026-09-05

Select [semantic output 0.1](../specification/0012-requirement-semantic-output-0.1.md)
as the requirements component's first published compiled boundary. It follows
[Experiment 0027](0038-compilation-linking-experiment.md): values, provenance,
completeness and diagnostics are distinct. TC-1502 supplies independent identifiers.
TC-1202 implements a separate `mundanereq-compile` command; existing commands retain
their contracts and explicit source selectors.

Require an explicit source root to make provenance paths reviewable and reproducible
across checkouts. Reject outside-root inputs rather than emitting unstable absolute
paths. Source checksums have a concrete exact-revision/integrity use; they hash
raw bytes, including comments, and never replace authored IDs. Compare semantic
values directly for review questions; defer public semantic digests until a
consumer justifies a projection/canonicalization policy.

Publish complete valid artifacts or diagnostic-only invalid artifacts. A partial
record list would invite unsafe coverage counts before TC-1403 and editor semantics
exist. Point diagnostics truthfully retain unknown ends; requirement/field/reference
syntax spans are retained during parsing so consumers never reconstruct locations.
Locations are separate from authored source citations and semantic values.

Reject: Java class serialization (couples consumers to implementation); generated
JSON as source (competing authority); a universal graph envelope (no demonstrated
consumer); guessed custom attributes (ownership/schema unresolved); implicit Git
lookup (changes artifact identity with environment); re-parsing in the emitter
(duplicates interpretation and risks mismatched locations).

The [worked fixtures](../specification/examples/requirements-artifact-0.1/README.md)
cover both source representations, optional fields, prose/math and Unicode spans,
plus invalid/deduplicated/unresolved source cases and unknown format rejection.
A Python-only contract consumer can inspect values and locations using these files.
TC-1202 must match golden bytes and validate source spans against the maintained
parser. TC-1203 remains responsible for import selection/qualification and revision
binding; TC-0905 chooses plan authoring format and staleness policy independently.
