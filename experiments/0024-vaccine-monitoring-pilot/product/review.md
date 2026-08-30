# Current Self-Review

Date: 2026-08-30

Reviewer: Codex acting as the pilot systems engineer

Independence: none. This review is a disciplined second pass by the same agent,
not independent-human evidence or external approval.

## Review performed

- Compared each selected source-backed statement with the source register and
  relevant clause locator.
- Checked requirement statements for a named subject, observable obligation or
  stakeholder need, and an identifiable operational context where needed.
- Queried every authored decomposition target through source validation.
- Confirmed every system requirement has a stakeholder parent and every
  allocated lower-level requirement has a system parent.
- Compared system obligations with lower-level coverage and added `DEV-019`
  when the integrity requirement had no producer for the digest verified by
  `RDS-005`.
- Added `RDS-008` when the interval-status system requirement had presentation
  but no durable remote classification behavior.
- Confirmed all 57 requirements have at least one planned verification or
  stakeholder-validation activity.
- Kept verification execution, evidence, results, and safety assessment out of
  requirement records.

## Findings closed before baselining

1. `SYS-016` initially decomposed only to file preservation, service checking,
   and export-digest behavior. `DEV-019` now establishes the pre-transfer digest
   needed to detect transfer corruption.
2. `SYS-012` initially relied on `UI-001` to display status without allocating
   persistent interval classification. `RDS-008` now owns that behavior.

## Accepted limitations and friction

- Several requirements have compound provenance. The single opaque `source`
  field remains readable, but software cannot reliably split or query those
  references. This is a concrete provenance-analysis limitation, not evidence
  that arbitrary attributes belong in the requirement language.
- Source-set scope, allowed allocations, and trace-coverage policy require the
  separate project profile. The validator correctly does not infer them, but no
  focused project-policy tool checks them yet.
- The 57-row verification coverage table is understandable and diffable but
  laborious to audit manually. A focused analyzer would now solve an observed
  problem: referential checking, exact baseline binding, uncovered-item queries,
  and stale-plan detection.
- Safety levels are meaningful only with their context, scheme, rationale,
  evidence source, authority, and exact source-set digest. Putting only `L1` or
  `L2` into requirement records would lose material semantics.
- Public source documents and the source register provide realistic inputs but
  do not substitute for stakeholder validation of the pilot's derived needs.

## Disposition

Accept Baseline A for the controlled-change phase. The source model is adequate
for this baseline without new syntax. The accepted limitations remain explicit
inputs to the final assessment.
