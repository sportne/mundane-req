# Outline for a YAML-based requirements specification

This is a clause-level change outline for TC-1106, not normative language. It
maps the current [source 0.2 specification](../../specification/0005-mundanereq-source-language-0.2.md)
to a future explicit YAML profile. The experiment selects a direction; TC-1106
must turn the remaining choices into worked, testable rules before implementation.

| Current clause | Retain, replace or clarify |
| --- | --- |
| Foreword; 1.1–1.2 Scope/normative language | Keep requirements-language scope and normative terminology; state the exact YAML profile and source-contract identifier |
| 1.3 External references | Replace the no-external-reference claim with pinned YAML 1.2.2 and selected JSON Schema dialect references; resolve precedence and supported subsets explicitly |
| 2.1–2.4 Conformance | Separate YAML syntactic validity, profile/schema validity, domain/source-set validity and source-selection conformance; define partial-result obligations |
| 3 Terms | Retain requirement, ID, source set, content block, relationship, revision and diagnostic; introduce document, mapping, sequence, decoded scalar and schema/profile; revise line/scalar/comment definitions |
| 4 Notation | Delegate YAML grammar to its specification; use schema and mapping tables for the profile; retain exact character comparison with no implicit Unicode normalization |
| 5.1–5.6 Abstract model | Preserve all current values, authored identity, relationship set semantics and ordered content; distinguish source order/style from semantic order |
| 6.1 Encoding | Decide exact UTF-8/BOM policy, malformed byte behavior and escape decoding before character checks |
| 6.2–6.3 Lines/characters | Decide final newline, CRLF, tabs, decoded controls and scalar boundary rules; enumerate any intentional difference from 0.2 rather than inheriting library defaults |
| 6.4 Coordinates | Define byte/code-point coordinate conventions and ranges through escaped/folded/literal scalars; distinguish record-anchor diagnostics from precise value spans |
| 7.1–7.3 Selection | Define explicit format/version choice, distinctive extension, directory discovery, unrelated YAML exclusion, duplicate paths and empty selection; no heuristic parser fallback |
| 7.4 Granularity | Keep IDs independent of file and record order; specify one document containing a requirements list, including one-record files; test splitting/combining source sets |
| 8.1–8.6 Syntax | Replace custom record/prose grammar with the YAML subset, allowed keys and schema; decide mandatory text styles, mapping key types, directives, anchors, aliases, tags, streams, flow collections and limits |
| 9.1 Record interpretation | Specify required/optional fields, omission versus null/empty, duplicate-key errors before object construction and unknown-field behavior |
| 9.2 Prose folding | Delegate YAML decoding, then define string-to-paragraph mapping; cover folded blank lines, chomping, more-indented lines and equivalence with current paragraph semantics |
| 9.3 Math | Retain opaque LaTeX block values and block order; define payload encoding, blank lines and leading/trailing newline preservation without evaluation |
| 9.4 Statement/rationale | Decide scalar shorthand versus uniform block lists; retain rationale's prose-only model; map every accepted representation to one semantic value |
| 9.5 Concrete order | Mapping order and file/record order nonsemantic; content order semantic; canonical suggestions belong to formatting policy, not accidental validity rules |
| 10 File validity | Enumerate physical, YAML syntax, profile, schema and domain errors; do not silently coerce values or discard duplicate/unknown keys |
| 11.1–11.3 Set validity | Keep global ID uniqueness and referential integrity; retain policy separation; define useful diagnostics when some files are invalid |
| 12.1–12.3 Diagnostics | Require locations, stable rule meanings and truthful completeness; specify independent-error collection, input failures and resource-limit failures |
| 13 Equivalence | Keep semantic equality independent of comments/layout; add paired custom/YAML migration examples including exact prose/math values |
| 14 Revisions/baselines | Keep Git/source ownership and explicit human IDs; YAML order/paths do not generate identity; compiled content remains derived |
| 15 Version/compatibility | Select an experimental source-contract identifier separately from tool/package and compiled format; record breaking syntax changes and migration notes without promising permanent stability |
| 16 Excluded facilities | Keep contextual engineering policy and unrelated artifact semantics outside requirements; attributes require TC-1301/TC-1302 decisions, not arbitrary YAML keys |
| Annex A Grammar | Replace collected custom grammar with the pinned schema and non-schema profile/domain rules; define which artifacts are normative and how consistency is verified |
| Annex B Examples | Paired small/multiple-record, prose/math, comment and invalid examples with explicit scalar-style guidance |
| Annex C Conformance | Add YAML profile fixtures and negative corpus; preserve historical 0.1/0.2 evidence under its original versions |
| Annex D Rationale | Link this experiment and the successor decision; explain rejected alternatives and remaining implementation obligations |

## Migration contract to decide

Use a distinctive candidate suffix such as `.mreq.yaml` rather than selecting
every YAML file in a project. This suffix is a proposal, not current behavior.
Make format and source version explicit through a documented command or checked-in
project declaration; decide standalone-file behavior and conflict diagnostics.
Do not let `.mreq` silently change grammar or reinterpret arbitrary YAML as
requirements. The present experiment's out-of-band profile choice is insufficient
as a complete production discovery/version contract.

A bounded converter must parse current source, emit the selected YAML profile and
compare normalized models for the entire selected source set before publication
of its outputs. It must retain IDs and all semantic text, preserve comments with
a documented placement policy, and report ambiguous or unsupported cases. Dry-run
and separate-output modes make the conversion reviewable. Test interrupted writes,
existing outputs and conflicting IDs rather than overwriting source blindly.

Preserve historical conformance fixtures and their versioned interpreters or
reproduction instructions. Convert maintained examples only in TC-1109 after
parser/formatter checks pass. If temporary mixed-format reading is needed, specify
one combined source set, duplicate-ID behavior, explicit selectors and retirement
conditions for the adapter. Do not make continued support of both authoring
formats the default outcome of an experiment. Generated semantic JSON stays a
compiler/analysis interface; YAML requirements remain human-authored source.
