# Research 0034: Requirements YAML contract decision

Status: Decision recorded before implementation

Date: 2026-09-05

Select [mundanereq-yaml-0.3](../specification/0010-requirements-yaml-0.3.md)
for requirements only. Preserve the source 0.2 abstract model. Separate structural
schema authority from presentation/domain/selection rules; explicit conformance
checks keep them consistent. Retain ordinary SnakeYAML Engine 3.1.1 parsing with
explicit node validation instead of adding a second runtime schema interpreter.
The normative JSON Schema remains independently checkable; implementation tests
exercise the same structural cases against both validators.

Select quoted/block values to reject the comparison's silent unquoted-hash loss.
Reject aliases/tags/directives rather than maintain hidden expansion behavior.
Select explicit CLI format plus a required document identifier to avoid accidental
selection of unrelated YAML. Leave default custom invocation intact during bounded
migration. The distinctive suffix is .mreq.yaml. A conservative formatter preserves
text instead of using the emitters that lost comments. Use a separate-output,
no-overwrite migration utility; validate whole-set semantic equality before writes.

Uniform lists alone would remove shorthand but add noise to ordinary single-
paragraph requirements. Retain the comparison's string shorthand, with exact
ordered list mapping. Permit quoted math payloads so unusual leading/trailing
newlines remain representable; do not force literal-block chomping heuristics.
The finite byte/nesting/record/diagnostic limits bound malformed-input processing.
No source-contract change for verification plans, safety assessments or other
artifacts follows from these choices.

## Clause disposition from source 0.2

| Old clauses | Successor disposition |
| --- | --- |
| 1 scope/references, 2 conformance, 3 terms, 4 notation | New sections 1–3 and 7; YAML/JSON Schema normative references and profile replace custom grammar terminology |
| 5 model | Retained by section 1 and explicit field mapping in section 3 |
| 6 physical/coordinates | Sections 2 and 6; decoded escape controls and scalar-start diagnostics explicit |
| 7 selection/granularity | Section 5; explicit mode, suffix, no automatic mixed-source interpretation |
| 8 syntax | Sections 2–3 and structural schema; custom opener/closer grammar replaced |
| 9 interpretation/folding/order | Section 3; YAML decoding then exact strings/ordered blocks; no custom second folding pass |
| 10 file validity, 11 set validity | Sections 2–4; schema constraints plus explicit IDs/references; policy remains separate |
| 12 diagnostics | Section 6; bounded failures, independent files and incomplete-source suppression |
| 13 equivalence, 14 revisions/baselines | Section 4; exact model values and authored identity retained |
| 15 version/compatibility, 16 exclusions | Sections 1 and 5; explicit experimental compatibility and requirements-only scope |
| Annex A grammar, B examples, C conformance, D rationale | Structural schema, section 7, maintained conformance evidence and this decision |

Every original clause has a disposition. Historical specifications remain
unaltered. Future attributes remain TC-1301/TC-1302 decisions. The new specification
is a source contract, not an implementation completion or release claim.
