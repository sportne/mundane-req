# CR-001 Impact Analysis Before Editing

Date: 2026-08-30

Baseline analyzed: `experiment-0024-baseline-a`

## Trace query

    mundanereq-trace impact SYS-009 requirements

Result:

```text
Lower-level impact paths to SYS-009:
1 NTF-002 -> SYS-009
1 RDS-002 -> SYS-009
```

`RDS-002` contains the allocated retention duration and must change. `NTF-002`
records notification outcomes under the same hosted-record obligation but has
no duration text; inspect it and retain it unchanged if its parent remains
satisfied.

## Planned edits

- Change the Baseline A context assumption from three years to five years.
- Change `SYS-009` and `RDS-002` from three years to five years and add `CR-001`
  to their provenance.
- Change the `ACT-RETENTION` objective to five years.
- Recalculate the requirement and activity digests and bind `PLAN-B` to the new
  baseline.
- Update every safety assertion's whole-source-set digest even though none of
  the assessed requirement values changes. Record this as a binding-granularity
  observation rather than pretending the assessments were substantively
  reassessed.
- Re-run validation, formatting, trace, coverage, and review checks.

## Inspected but not expected to change

- `NEED-004` asks for an investigable historical record without prescribing a
  period.
- `NEED-005` asks for controlled access throughout the contracted period.
- `NTF-002` defines which delivery facts enter the retained record, not how long
  they remain available.
- `RDS-006` exports a selected interval and does not constrain the service
  period.
