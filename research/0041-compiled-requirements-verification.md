# Research 0041: Maintained requirement compilation verification

Date: 2026-09-05

The independent `mundanereq-compile` command implements
[semantic output 0.1](../specification/0012-requirement-semantic-output-0.1.md).
[Usage](../distribution/compile.md) shows compilation and a JSON-only consumer.
Both existing custom 0.2 and explicit YAML 0.3 feed the same semantic representation.
The earlier 0.1 corpus remains checked under its compatible custom 0.2 reader.

## Implementation and observable boundaries

The maintained parsers retain record, field and authored-reference syntax spans
while constructing requirements. The emitter consumes those retained origins and
the selected byte snapshots; it never reconstructs locations by parsing text again.
Semantic values exclude locations and source checksums. Raw source SHA-256 identifies
only exact bytes at read time; it changes with edits and is never authored identity.

Inspection confirmed repeated decoding in Interpreter.decode/SourceDocument.read
and the formatter's later concrete-source construction. The emitter needs no
additional concrete document and reuses one interpretation. Changing the formatter
pipeline would add risk without being required for the consumer, so this card
leaves that optimization for measured follow-up. No performance claim is made.
One targeted coordinate conversion is necessary: historical invalid-UTF-8 diagnostics
use byte columns. The compiled interface converts only the known-valid line prefix
to code-point columns, preserving historical validator/formatter/trace behavior.

Invalid source emits a diagnostic-only artifact; operational input failure has its
own phase/status. Unknown selectors and out-of-root inputs fail invocation before
artifact output. No partial record inventory can masquerade as complete. Serialization
checks reject missing origins and unpaired Unicode surrogates. Failed output delivery,
including a written prefix or a failed flush, cannot return success.

## Reproducible checks

`make compiled-verify` builds the independent native compiler and runs
[scripts/check-compiled-artifacts.py](../scripts/check-compiled-artifacts.py).
`make verify` includes that check, independent version checking and replay of
[Experiment 0027](../experiments/0027-compilation-linking/README.md), alongside the
existing full JVM/native, formatter, package, corpus and schema checks.

Evidence includes:

- Exact contract golden bytes for multi-file valid source, optional/null values,
  opaque multiline math, a supplementary Unicode character before a later field,
  missing fields, duplicate IDs and dangling references.
- Eight source selections with field/record/reference spans checked against actual
  source slices, exact byte checksums, duplicate/reversed argument selection and
  JVM/native output agreement. Three migration pairs retain 120 equal requirement
  values. Semantic values are frozen in corpus-values.json.
- All checked-in custom 0.1/0.2 and YAML invalid selections produce diagnostics and
  no compiled records, with JVM/native agreement. Diagnostic ends remain explicitly
  unknown rather than fabricated.
- A separate Python standard-library consumer reads only emitted JSON from a fresh
  working directory. Unknown/invalid/incomplete artifacts block its analysis.
  Another checkout yields identical normalized output.
- Comment edit, file move and normative edit distinguish provenance from semantics.
  A post-read filesystem edit does not replace the selected snapshot's semantics.
- Missing/empty inputs, invalid invocation, malformed UTF-8 after supplementary
  Unicode, an oversized YAML file with unknown full-file checksum, actual closed
  stdout and actual broken pipes exercise failure/status behavior.
- JVM fault injection covers failure before output, after a prefix, on flush,
  closed stderr/stdout, and unavailable origins. Custom-source assertions verify
  exact exclusive ends and repeated decomposition-field spans.

## Compatibility and scope

Existing source validity, default custom selector and command contracts remain
covered by the existing regression suite. The compiler is an additional independent
native target; the historical three-command trial archive remains its documented
inventory. Current generated version metadata includes the new independent compiler,
command and artifact identifiers. No release was published and hosted CI remains
TC-1503 work.

This implements only requirement compilation. Maintained import/link contracts,
verification-plan notation, contextual assertions and typed custom attributes retain
their separate cards. YAML remains a requirements-only decision. Diagnostics provide
points, not editor-complete recovery; TC-1403 and TC-1504 remain open. Artifact output
is built in memory; no streaming/performance guarantee is made. Source snapshot hashes
do not establish that a file stayed unchanged after its read.

## Design fixture reconciliation

The TC-1201 fixtures had decoded math newlines encoded as JSON `\n`, while the
selected byte contract specified `\u000a`. The two relevant expected files now
use the selected escape spelling; decoded semantics, provenance and source spans
are unchanged. This is an explicit golden-encoding correction, not a contract change.

## Recorded completion

The final `make verify` completed with exit 0 using GraalVM CE 21.0.2 / Java 21,
gcc 13.3.0 on Linux x86_64/WSL2, after the malformed-UTF-8 coordinate conversion.
[Captured results](../experiments/0027-compilation-linking/results/maintained-compiler-verification.txt)
include all 13 maintained JVM groups, existing native/package/formatter checks,
independent schema and version checks, compiled artifact checks and the 13-case
rebuild experiment. This supports completing TC-1202.
