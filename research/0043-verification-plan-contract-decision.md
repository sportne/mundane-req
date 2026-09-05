# Research 0043: Verification plan and staleness contract

Date: 2026-09-05

Select [verification planning 0.1](../specification/0015-verification-planning-0.1.md):
three narrow TSV tables for explicit plans, activity definitions and coverage rows.
This choice follows the pilot's existing row-oriented workflow, not requirements'
YAML decision. The compiled interface remains independently specified JSON.

| Carrier | Present workflow fit | Decision |
| --- | --- | --- |
| TSV with explicit headers/version/scopes | Existing activity and coverage rows copy without rewriting objectives; one assertion per diff line | Select for bounded plans; reject multiline/embedded tabs rather than invent quoting |
| JSON source | Useful for nested values but adds repetitive keys to the current flat rows | Defer until nested/multiline authoring has a concrete need |
| YAML source | Capable representation, but no benefit established over existing flat plan carrier | Not selected by the requirements decision; remains a future independent option |

The [experiment](../experiments/0028-verification-contract/README.md) passes 57
requirements and its complete pilot coverage table through eight change/failure
cases. Baseline B makes RDS-002 and SYS-009 review-stale; comment/move edits preserve
review-current. An unrelated device edit does not stale the retention projection.
An ID correction is missing:SYS-009; dropping SYS-009 coverage reports it uncovered.
Incomplete artifacts prohibit analysis. Full fixtures and expected summaries precede
the maintained implementation.

Whole-source-set checksums and Git-tree binding notice comments, moves and unrelated
edits (Experiment 0027). Compare full seven-field requirement values for review
staleness, while retaining exact artifact/source pins for provenance. This avoids
invalidating unrelated assertions and does not require a public semantic hash.
Changed citation/rationale/decomposition is conservatively review-stale; a narrower
policy would require its own explicit contract change. Rebaselining is a deliberate
source revision selection, not automatic acceptance of generated snapshots.

Plan context, activity definition and coverage assertions retain their own source
locations and authority. A requirement basis being current says nothing about an
activity's approval or execution. Planned coverage, possible impact, review staleness
and satisfaction remain separate. Execution/evidence/result storage, safety analysis
and general policy are excluded. No companion universal schema is introduced.

Select the small resolver from TC-1203 for TC-1204, then the plan adapter and native
analyzer in TC-0904. The complete fixture contract now supplies both implementations'
acceptance inputs. The source-carrier decision deliberately leaves other engineering
formats undecided and preserves existing requirement tools and source contracts.
