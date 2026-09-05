# Experimental YAML profile 1

This is the tested candidate, not the current source specification or the final
successor design. It delegates syntax to [YAML 1.2.2](https://yaml.org/spec/1.2.2/)
and structural validation to [JSON Schema Draft 2020-12](https://json-schema.org/draft/2020-12/json-schema-validation).
The profile identifier is `urn:mundanereq:experiment:0025:yaml-profile:1` in
[schema.json](schema.json); the experiment selects it explicitly. A production
source-version selector and final extension are left to TC-1106.

## Container and identity

Each file is one YAML document containing a mapping with one `requirements` key,
whose value is a nonempty sequence of records. A single requirement uses the same
shape. Separate files and sequence order do not define identity. IDs are authored
strings, unique in the selected source set. Moving, splitting or reordering a file
does not create a new requirement. Document streams add a second record/container
mechanism without a demonstrated benefit here; the candidate rejects them.

```yaml
requirements:
  - id: "SYS-001"
    title: "Measure temperature"
    statement: >-
      The logger shall measure temperature
      once per second.
  - id: "SYS-002"
    title: "Display temperature"
    statement: "The logger shall display the measured temperature."
    decomposes: ["SYS-001"]
```

This example illustrates representation only, not an engineering analysis of the
relationship. The maintained semantic model remains the comparison baseline.

## Complete field mapping

| Current semantic field | Candidate YAML | Interpretation |
| --- | --- | --- |
| ID in record opener | Required `id` string | Same ASCII identifier grammar and authored identity |
| `title` | Required nonempty string | Same scalar value |
| `allocation` | Optional nonempty string | Same opaque label; no product model added |
| `statement` prose | Required string or ordered block sequence | String means one prose paragraph; `{prose: ...}` means one paragraph |
| Statement math | `{math: {language: latex, payload: ...}}` block | Opaque payload, including internal newlines; block position retained |
| `rationale` | Optional string or nonempty string sequence | One or several ordered prose paragraphs; no math interpretation |
| `source` | Optional nonempty string | Same uninterpreted source value; not automatically a URI |
| Repeated `decomposes` | Optional nonempty sequence of ID strings | Same unordered relationship set; duplicate entries rejected |
| Source comments | YAML comments at source boundaries | Nonsemantic author information retained separately from compiled values |

All three required keys must be present. Optional fields are absent rather than
null or empty. Unknown/duplicate keys are errors, including nested block fields.
Mapping order is nonsemantic; the fixture convention follows current field order.
Only ordered content and rationale lists carry semantic ordering. Normalized
output sorts requirements by ID and decomposition targets for comparison; that
output is derived, not another authoring format.

## Text, scalar resolution and physical source

Use YAML 1.2 Core scalar resolution, not YAML 1.1 or implementation-specific
extended defaults. Quoted `"001"` is an ID string; plain `001` is an integer and
fails the schema. Plain `true` is Boolean; `yes` is a string. The experiment had to
remove ruamel's additional timestamp resolver to match SnakeYAML's selected Core
schema: `2026-09-04` then stays text. See the captured defaults comparison.

Quoted scalars are the recommended authoring convention, but the candidate also
accepts plain strings. This exposes an important hazard: `title: status # ready`
is valid YAML whose title is only `status`. Structural validation cannot recover
intended text. TC-1106 must decide whether to require quoted/block styles for text
rather than merely recommend them. Inline comments on text values need a clear
policy if styles are restricted. No preprocessor silently repairs these cases.

A string paragraph cannot contain decoded CR or LF. `>-` folds ordinary wrapped
lines into one paragraph and removes the terminal newline. Blank lines inside a
folded scalar can create newlines; separate paragraphs are represented explicitly
as list entries. `>` with its default trailing newline fails the candidate's
paragraph rule. Opaque math uses `|-` for exact internal newlines without an added
terminal newline. YAML double-quoted escapes are decoded before checking values;
backslashes in LaTeX are most naturally written in literal blocks. The future
specification must explicitly account for leading/trailing payload newlines and
all current paragraph cases; this sample does not prove those exhaustively.

The experiment requires UTF-8, LF or CRLF, a final line ending, no BOM, no tabs and
no bare CR. It rejects prohibited decoded controls and surrogate characters,
including those introduced by escapes. Boundary whitespace is rejected for title,
allocation and source. These are an experimental conservative policy, not a claim
that YAML itself prohibits every such source form.

## Validation layers and source preservation

1. Decode and enforce the physical profile.
2. Use ordinary YAML parsing; reject duplicate keys before mapping construction
   can overwrite them. Reject anchors, aliases and explicit tags through parser
   events; no implicit object construction or external resolution is needed.
3. Apply the checked-in structural schema, collecting independent schema errors.
4. Check domain restrictions, cross-file ID uniqueness and reference resolution.
   Cycles and engineering policy are not newly prohibited.

The Python adapter uses source marks to report file, line, column and a layer.
Missing fields map to their record; schema paths often map to keys rather than
exact value spans. Syntax failure currently stops that file, while independent
files continue. This is enough to compare diagnostics, not a finished recovery or
source-range contract. Java probe results cover YAML syntax only.

Comments are assessed separately from normalized values. Python round-trip data
preserves all tested comment lines; a generic Java object dump does not. A
conservative Java path validates YAML syntax, retains the original text, and
normalizes CRLF to LF. The harness additionally checks the entire profile before
and after formatting. A maintained formatter must integrate that full validation,
source-set safety and output/write-back behavior before claiming safe rewriting.

## Hypothetical attributes, outside this candidate

A future project might declare `safety-criticality` as an enumeration in a
checked-in project schema and write `attributes: {safety-criticality: "B"}` on a
requirement. A custom representation could carry the same typed value using a
separately selected field grammar. Either can compile to the same attribute map;
YAML does not decide who owns the fact or make a contextual safety assessment
intrinsic. `references` could likewise be text or a list only after the type and
ownership decisions. These examples are deliberately not accepted by schema.json.
TC-1301/TC-1302 retain control of scope, names, types, defaults, discovery, and
whether a fact belongs in an independently linked artifact.
