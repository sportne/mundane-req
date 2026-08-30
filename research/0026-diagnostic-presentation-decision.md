# Diagnostic Presentation Decision

Status: Interface behavior selected; implementation deferred

Date: 2026-08-29

[Experiment 0019](../experiments/0019-diagnostic-presentation/README.md) compares
complete text, explicitly bounded text, and complete JSON Lines over the
reproducible 1,200-error corpus.

Retain unlimited text by default. For a later validator trial, add only
`--max-diagnostics N`: validate the complete source set, print the first N
diagnostics, then print shown, total, and omitted counts plus the complete-stream
recovery instruction. Exit semantics must not change.

Defer machine-readable diagnostics. The projection works, but no concrete CI
consumer yet justifies freezing provisional categories and messages into a new
compatibility surface. Do not store diagnostic output as authoritative state.
