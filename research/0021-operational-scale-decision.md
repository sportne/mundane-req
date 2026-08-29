# Operational Scale Decision

Status: Decided for the selected corpus

Date: 2026-08-29

[Experiment 0014](../experiments/0014-operational-scale/README.md) found no
operational pressure for persistent state. Native formatter, validator, and
trace commands reconstructed the complete 60-requirement model in low
single-digit milliseconds on the recorded host. Six-file and 60-file
measurements occupied the same envelope at the timer's resolution.

Keep the in-memory architecture, independent executables, and non-semantic file
layout. The result is a bounded case, not a throughput claim. Repeat it when a
concrete larger corpus exists.

The only suggested scale friction was human navigation of 1,200 complete
diagnostics; the evidence establishes volume, not human difficulty. Test that
as a focused output-interface question in TC-0707, not as evidence for caching,
databases, or grammar changes.
