# Baseline B Self-Review

Date: 2026-08-30

Reviewer: Codex acting as the pilot systems engineer

Independence: none. The same limitation as Baseline A applies.

## Change reviewed

CR-001 increases hosted record availability from three years to five years.
The focused requirement diff changes only `SYS-009` and `RDS-002`; their IDs,
allocations, and decomposition relationships remain stable. The changed source
fields identify the project change and retain the applicable WHO hosting clause.

`NTF-002`, `NEED-004`, `NEED-005`, and `RDS-006` were inspected from the impact
paths and related workflow. They remain unchanged because they define retained
content, the contracted-period need, and export behavior without fixing the
duration.

## Companion review

- `ACT-RETENTION` now evaluates five years.
- Every coverage row now belongs to `PLAN-B`; all 57 requirement IDs remain
  covered exactly once in this bounded plan.
- `PLAN-B` binds the new requirement and activity source digests.
- Baseline A's plan is detectably stale against Baseline B.
- The six safety judgments did not change substantively because none of their
  requirement values or contexts changed. Their whole-source-set binding still
  required six mechanical assertion revisions.

## Tool review

- Formatter check: pass with no source rewrite.
- Validator: 57 requirements and 70 decomposition relationships in four files.
- Requirement manifest: all four files pass SHA-256 checking.
- Verification binding: requirement and activity digests match `PLAN-B`.
- Coverage: 57 rows, 57 known requirements, zero uncovered requirements.
- Trace: `SYS-009` still has `RDS-002` and `NTF-002` as incoming lower-level
  requirements; `RDS-002` reaches `NEED-004` and `NEED-005` through `SYS-009`.

## Finding

The controlled change is easy to understand in ordinary requirement-source
diffs. The most significant friction is not in `.mreq`; it is the coarse digest
used by provisional companion artifacts. A change to two unassessed retention
requirements made all six safety assertions stale because they bind the whole
source set. This preserves honesty but creates irrelevant churn. A future
companion experiment should compare exact per-requirement semantic revision
bindings with whole-source-set bindings before any carrier is standardized.

## Disposition

Accept Baseline B. CR-001 is fully represented and no source-language change is
justified by the workflow.
