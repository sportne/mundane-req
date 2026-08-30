# Trace Policy Decision

Status: Policy boundary selected; no implementation selected

Date: 2026-08-29

[Experiment 0022](../experiments/0022-trace-policies/README.md) runs required
downward coverage and acyclic-decomposition policies against conforming source
that passes and fails each rule. Language diagnostics and policy diagnostics
remain separate.

Scope and waivers are human-readable authoritative project policy. Explicit
focused operations are sufficient for the experiment; no arbitrary rule
language, plugin architecture, or universal configuration model is justified.

The FRET LPC-mini counterexample shows that blindly applying downward coverage
yields 19 true violations of an inapplicable policy because the selected corpus
has no lower-level source. Downward coverage has one observed UAS workflow;
acyclicity is a boundary-control candidate. Neither is demonstrated to recur,
so this study does not select implementation or executable architecture.
