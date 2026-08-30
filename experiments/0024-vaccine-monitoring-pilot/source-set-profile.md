# Source-Set and Baseline Profile

Status: Pilot project convention; not source-language syntax

## Authoritative requirement selection

For each baseline, the authoritative requirement source set is the directory
named `requirements` immediately beneath that baseline directory. Every `.mreq`
file recursively selected from that directory is in scope under Clause 7 of the
0.2 language standard.

Baseline A selection:

    build/maintained/mundanereq-validate experiments/0024-vaccine-monitoring-pilot/baseline-a/requirements

Baseline B selection:

    build/maintained/mundanereq-validate experiments/0024-vaccine-monitoring-pilot/baseline-b/requirements

Markdown, TSV, tool output, and Git metadata are not requirement source. They
may be authoritative workflow artifacts in the experiment, but they are not
members of a mundanereq source set.

## Contract and policy

- Source contract: `mundanereq-source-0.2`.
- Requirement identity is the ID in each record.
- File and directory names are nonsemantic.
- Allowed allocation labels are `SVEMS`, `Monitoring device`, `Remote data
  service`, `Browser interface`, and `Notification delivery`.
- Every `SYS-*` requirement must decompose at least one `NEED-*` requirement.
- Every `DEV-*`, `RDS-*`, `UI-*`, and `NTF-*` requirement must decompose at
  least one `SYS-*` requirement.
- Top-level `NEED-*` records have no required parent.
- These naming and coverage rules are pilot policy, not 0.2 language validity.

## Baseline identification

An annotated Git tag gives each reviewed snapshot durable pilot meaning:

- `experiment-0024-baseline-a`
- `experiment-0024-baseline-b`

The tag identifies the complete repository snapshot, including requirement
source, context, source register, companion artifacts, and review evidence. A
tag does not imply external approval, qualification, or certification.
