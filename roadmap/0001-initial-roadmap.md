# Roadmap 0001: Initial Investigation

Status: Draft

## Objective

Determine whether a purpose-built, human-readable requirements source format stored in Git provides a useful foundation for requirements engineering before investing in a broader tool ecosystem.

The roadmap is organized around questions and evidence. Later work should be revised when earlier experiments change what the project believes.

## Phase 1 — Understand practice, workflows, and prior art

**Question:** Which real requirements-management needs place pressure on the conceptual model or source language?

Survey common RM capabilities and, separately, common systems-engineering workflows. For each, distinguish the observed practice, underlying engineering need, possible model or language implications, responsibilities already handled by Git or development infrastructure, and open questions.

Study text-first approaches such as StrictDoc and Doorstop for language, data-model, traceability, and workflow ideas without assuming either must become a dependency.

Capture organizational constraints affecting dependency selection, licensing, provenance, or distribution.

**Learning milestone:** Identify the smallest representative workflows that a source experiment must explain without treating existing product features as requirements for mundane-req.

Initial result: the first experiment will focus on change review, rationale and decomposition tracing, and baseline comparison.

## Phase 2 — Define a representative scenario and experiment

**Question:** What small corpus and change history can expose important tradeoffs without becoming a toy?

Create an original UAS mission-control corpus informed by the structure and traceability patterns observed in Dronology. Do not copy externally authored requirements without adequate redistribution permission.

Define:

- the corpus scope and levels of abstraction;
- the relationships and rationales needed by the workflows;
- two synthetic but meaningful baselines;
- one safety-driven change affecting statements and relationships;
- at least one requirement containing mathematical source notation that can be rendered separately;
- an evaluation protocol and rubric;
- a focused study of similar projects, standards, and generic text carriers;
- candidate syntax-and-file-granularity combinations with genuinely different tradeoffs.

**Learning milestone:** Agree that the scenario is realistic enough to challenge a conceptual model and small enough to compare manually.

Initial result: [Research 0004](../research/0004-uas-semantic-corpus.md) defines the syntax-neutral UAS corpus, trace relationships, mathematical source, view order, and exact Baseline A-to-B change. Candidate encoding remains the next step.

## Phase 3 — Compare source representations

**Question:** Which representation best preserves human understanding and ordinary Git behavior?

Encode the same corpus and synthetic history in several candidate textual representations. Treat file granularity as an experimental variable by comparing the same purpose-built grammar in multi-requirement modules and single-requirement files. Compare those with a document-embedded Markdown alternative rather than assuming Markdown for every candidate.

Keep record syntax, field-content notation, and rendering conceptually separate. Include the same explicitly delimited LaTeX-style mathematical expression in every candidate and assess both its raw source and an optional disposable rendering.

Evaluate each with ordinary tools:

- text editors;
- git diff and git log;
- branches, merges, and conflict resolution;
- Bitbucket, GitLab, or GitHub pull or merge request views;
- command-line searching;
- tagged baseline comparison;
- manual rationale and decomposition tracing;
- raw and rendered inspection of mathematical notation.

Judge the candidates first on source readability, diff clarity, merge locality, file-operation overhead, navigation, move behavior, identity stability, relationship visibility, embedded-notation behavior, authoring effort, searchability, and durability without specialized tooling.

A parser is not initially required. Build a minimal parsing probe only if it answers an ambiguity that inspection cannot.

**Learning milestone:** Reject inadequate candidates, record the tradeoffs of viable candidates, and decide whether evidence supports selecting or revising a representation.

Initial result: [Research 0005](../research/0005-purpose-built-record-syntax-sketches.md) selects explicit keyword records with indented fields for the shared Candidate A/B experiment grammar. [Research 0006](../research/0006-non-markdown-view-notation.md) supplies a disposable flat view fixture rather than a language decision. [Experiment 0001](../experiments/0001-source-representations/README.md) records Baselines A and B in all three candidates; its [ordinary-diff review](../experiments/0001-source-representations/baseline-a-to-b-review.md) captures the first comparative evidence and a review correction.

## Phase 4 — Define the minimum language and model

**Question:** Which concepts did the experiment demonstrate are necessary?

Define only the concepts required by the successful workflows. Likely questions include:

- whether one human-usable ID can provide stable identity;
- how normative statements and local context are delimited;
- where requirement and derivation rationale belong;
- which relationship meanings are necessary;
- how collections select and order requirements;
- how repository snapshots acquire baseline meaning;
- how project-specific durable facts can be represented without a generalized metamodel.

Avoid macros, inheritance, generalized metamodeling, configurable grammars, execution semantics, plugin systems, workflow engines, and variant machinery without demonstrated need.

**Learning milestone:** Publish a small language specification that clearly distinguishes decisions, current hypotheses, alternatives, deferred topics, and open questions.

## Phase 5 — Test deterministic interpretation

**Question:** Can the selected source be interpreted and diagnosed reliably by an independent tool?

Implement the smallest useful parsing and validation experiment. It may include:

- parsing into a minimal semantic representation;
- duplicate-identity detection;
- missing required information;
- dangling requirement references;
- invalid relationship forms;
- clear source-positioned diagnostics.

Do not begin a production compiler architecture. The implementation exists to test the language decision.

**Learning milestone:** Determine whether the language is both pleasant source and a sufficiently deterministic interface for independent tools.

## Phase 6 — Use and learn

**Question:** What repeated friction appears when the format is used for realistic work?

Use the format for additional changes and reviews. Record where source and Git workflows work well and where focused tooling would materially help.

Possible later experiments include formatting, rendering, traceability queries, linting, editor integration, semantic comparison, and export. ReqIF round-trip interchange remains future work and should not shape the initial source syntax prematurely.

**Learning milestone:** Add tooling only in response to observed problems and revise the specification when use contradicts current hypotheses.

## Near-term success criterion

A small formally traced requirements set can be:

- read and edited as ordinary text;
- changed on a branch and reviewed through an ordinary Git diff;
- followed from a lower-level requirement through rationale and decomposition;
- captured at two meaningful repository baselines and compared;
- merged and understood later from the repository alone.

Passing this criterion would justify continued language and toolchain work. It would not demonstrate a complete requirements-management product.
