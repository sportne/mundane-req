# Safety Classification Ownership Decision

Status: Ownership decided; carrier provisional

Date: 2026-08-29

[Experiment 0018](../experiments/0018-safety-classification/README.md) compares
inline and separate representations of the same project-defined classification
facts. One unchanged requirement simultaneously has different levels in two
deployment contexts, and one level changes only because hazard analysis changes.

Treat a consequential assessed level as a baseline-bound assertion with an
identified requirement revision, scheme, context, level, rationale, evidence
source, and assessor authority. Keep it separate from the requirement record.
A bare list by level loses the arguments that make the assertion reviewable.

This is consistent with requirements needing project context: context is an
explicit argument of the assessment rather than a reason to collapse every
project fact into the requirement object. Descriptive tags remain ordinary
project annotations and must not masquerade as assessed criticality.

Do not add `.mreq` safety syntax or a generalized attribute bag. The TSV carrier
and WMSCS scheme are experimental and make no certification claim.
