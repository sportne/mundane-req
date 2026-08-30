# Experiment 0024 Assessment

Status: Complete

Date: 2026-08-30

## Outcome

The current requirement model and three-tool suite supported this bounded
formal-traceability workflow without a source-language change. The pilot
produced 57 requirements, 70 authored decomposition relationships, five
allocation labels, 17 verification activities, complete planned coverage, six
contextual safety assertions, two annotated Git baselines, and one controlled
change.

Both baselines pass the maintained formatter and validator. Direct and
transitive trace queries make the selected allocation, decomposition, and
change-impact questions understandable. CR-001's ordinary source diff clearly
shows two duration changes, changed rationale, and provenance while stable IDs
and trace links remain unchanged.

The final complete `make verify` gate passes with GraalVM 21.0.2 after adding
the pilot's four files as one independent maintained formatter source set. The
gate now covers 30 source sets and 64 files without changing formatter behavior.

This is positive model evidence, not proof of product correctness, requirement
completeness, independent usability, or 1.0 readiness.

## What belonged in requirement records

- Stable human-facing identity.
- A concise statement and title.
- Rationale for why the obligation exists in this product context.
- A human-readable provenance locator.
- A readable single allocation where one owning product element existed.
- Authored child-to-parent decomposition relationships.

The records remained understandable without a renderer. Prefixes such as
`NEED`, `SYS`, and `DEV` were useful project naming policy; the language did not
need a requirement-type field. Four files made navigation comfortable, but the
model and tools did not derive hierarchy or meaning from their paths.

## What did not belong in requirement records

- Source-document versions and roles belong to the source register.
- Stakeholders, operating contexts, assumptions, and exclusions belong to the
  system context.
- Allowed allocation values and required trace coverage belong to project
  policy.
- Verification activity, planned coverage, execution, evidence, and result are
  separate facts. This pilot planned but did not execute product verification.
- Safety consequence is an assertion over a requirement revision, context,
  scheme, rationale, evidence, and assessor authority. A bare inline level
  would have been materially incomplete.
- Change authorization, impact analysis, review, and acceptance belong to the
  change workflow and Git history.
- Baseline meaning belongs to annotated Git tags and documented convention.

This distinction is not based on a requirement being meaningful outside a
project. Every pilot requirement is contextual. The distinction is whether a
fact is part of the obligation being baselined or a separate assertion about
that obligation for a particular plan, context, authority, or workflow state.

## Model pressure observed

### Provenance

The single opaque `source` field was readable but sometimes contained multiple
locators joined in prose. This supports manual review but not reliable source
coverage or reverse-provenance queries. The demonstrated need is a focused
provenance relation or analyzer, not a generalized attribute bag. No syntax is
selected by this experiment.

### Allocation

The opaque single allocation label was sufficient because each lower-level
requirement had one accountable product element. The pilot does not prove that
one label is sufficient for shared responsibility or allocation to separately
versioned architecture objects. It did not encounter that workflow, so the
existing deferral remains appropriate.

### Relationship vocabulary

`decomposes` supported stakeholder-to-system-to-element reasoning and useful
impact queries. Verification, evidence, safety, and provenance would have been
distorted if forced into decomposition. Typed relationships beyond
decomposition remain companion concerns until a repeated workflow establishes
their semantics.

### Baseline binding

Whole-source-set SHA-256 binding reliably detected stale verification and
safety artifacts. It was also too coarse: changing two retention requirements
forced all six unrelated safety assertions to receive new bindings although
their referenced requirements and judgments were unchanged. This is the
clearest new modeling problem. Candidate remedies—per-requirement semantic
digests, Git snapshot plus requirement ID, or a smaller declared source slice—
must be compared before a carrier is standardized.

### Source-set scope

A small human-readable profile stated the selected directory, source contract,
allocation vocabulary, and trace policy. The native tools continued to accept
explicit inputs without hidden repository state. This was adequate, though the
profile policies were checked manually. The evidence does not justify making a
manifest part of source-language semantics.

## Tool assessment

- `mundanereq-format` appropriately did almost nothing: the authored source was
  already conservative, and no reflow or semantic rewrite was needed.
- `mundanereq-validate` caught language and graph validity but correctly did not
  claim requirement quality, policy compliance, or verification coverage.
- `mundanereq-trace` answered parent, child, higher-level, and impact questions
  without an index or reverse links in source.
- No maintained tool checked verification companion references, exact baseline
  binding, uncovered requirements, duplicate coverage, or staleness. Manual
  shell checks were necessary twice. This is now repeated workflow evidence for
  a focused verification-plan analyzer.

The analyzer should be a separate Java 21/GraalVM native executable, may reuse
the shared parser and semantic model, and must not own requirement source or
absorb product test execution, evidence storage, safety assessment, general
project policy, or requirement satisfaction semantics.

## Limits

- One agent authored and self-reviewed all work; no independence claim is made.
- Public WHO and CDC documents supplied realistic constraints but no source
  organization or stakeholder reviewed the derivation.
- The selected clauses are intentionally incomplete relative to a qualified
  product dossier.
- No product design, implementation, test execution, evidence, or satisfaction
  decision exists.
- The TSV carriers were useful experiment materials, not standards.

## Decision

Retain `mundanereq-source-0.2` unchanged. Keep 1.0 deferred: this pilot reduces
uncertainty about model sufficiency but does not provide independent-human use
or justify a compatibility promise by itself.

Select a bounded next experiment to define the verification analyzer's input
contract and compare baseline-binding granularities before implementation. Do
not combine that tool with validation, formatting, trace navigation, safety
classification, or a generalized policy framework.
