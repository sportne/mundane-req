# Allocation Model Decision

Status: Core model retained; companion carrier provisional

Date: 2026-08-29

[Experiment 0020](../experiments/0020-allocation-model/README.md) compares an
opaque allocation label, a separately enforced project vocabulary, and stable
target references through rename, reallocation, typo, and multi-target
workflows. It also replays Experiment 0003's committed reallocation.

Keep the optional opaque `allocation` label in source language 0.2. A
reallocation remains an especially readable one-line source diff. Allowed-value
control is project policy and does not require parser semantics.

The synthetic rename and multi-target cases show what stable target identity
could represent, but do not establish an observed project need. Defer the
companion. If a later workflow demonstrates that display-name changes must not
edit requirements or that one requirement needs multiple allocation
assertions, retest a separate artifact rather than making requirement records
own a component model. Target IDs, types, allocation roles, and TSV syntax are
experimental. This decision does not introduce systems-modeling scope or
change the `.mreq` grammar.
