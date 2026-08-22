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

Merge result: [the concurrent-edit review](../experiments/0001-source-representations/concurrent-edit-merge-review.md) shows that all candidates handle separated edits and expose readable same-statement conflicts, while model/view separation prevents the move-versus-edit conflict seen in the Markdown-coupled candidate.

Decision: [Research 0007](../research/0007-provisional-source-representation-decision.md) continues with the purpose-built record language, makes file boundaries non-semantic, preserves model/view separation without selecting the experiment's view syntax, and rejects document-coupled Markdown as the initial authoritative layout.

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

Initial draft: [Specification 0002](../specification/0002-minimum-source-language-and-model.md) defines the minimum source set, record model, grammar, and diagnostics to test. It deliberately leaves authored views, configurable metadata, semantic merge, and ReqIF machinery outside the first interpretation experiment.

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

Result: [Experiment 0002](../experiments/0002-deterministic-interpretation/README.md) implements a dependency-free GraalVM native Java probe. Its 11 grouped tests confirm equal semantic inventories for the module and one-record-per-file layouts, deterministic prose and math handling, and source-positioned rejection of the planned invalid cases. No production architecture or additional language machinery was needed.

## Phase 6 — Use and learn

**Question:** What repeated friction appears when the format is used for realistic work?

Use the format for additional changes and reviews. Record where source and Git workflows work well and where focused tooling would materially help.

Possible later experiments include formatting, rendering, traceability queries, linting, editor integration, semantic comparison, and export. ReqIF round-trip interchange remains future work and should not shape the initial source syntax prematurely.

**Learning milestone:** Add tooling only in response to observed problems and revise the specification when use contradicts current hypotheses.

Result: [Experiment 0003](../experiments/0003-sustained-authoring/README.md) exercises six independently committed authoring changes and a [formal model-pressure review](../experiments/0003-sustained-authoring/model-pressure-review.md). The language remained usable without grammar changes. Incoming trace navigation was the only repeated focused-tool opportunity; verification planning and identity continuity across ID correction remain unresolved model questions.

## Phase 7 — Test transferability

**Question:** Does the minimum model work on requirements that were not designed around it?

Find a suitably licensed public requirements corpus and document its provenance. If no adequate corpus can be redistributed, create a second original corpus informed by the observed structure without copying protected text.

Encode a bounded subset and include explicit pressure cases for:

- verification planning and coverage;
- identifier correction or replacement;
- external-source locator and revision fidelity;
- allocation vocabulary;
- relationship and coverage policy.

**Learning milestone:** Decide whether the current minimum model transfers, requires a small revision, or is overfitted to the UAS corpus.

Result: [Experiment 0004](../experiments/0004-transferability/README.md) transfers all 19 requirements from NASA FRET's Lift-Plus-Cruise mini case at a pinned Apache-2.0 revision. Both annotated baselines validate without grammar changes. A [baseline-bound verification plan](../experiments/0004-transferability/verification-plan.md) separates planned coverage from execution and results, while the [transferability review](../experiments/0004-transferability/transferability-review.md) confirms source-locator fidelity, records a controlled ID correction, and distinguishes requirement-source preservation from lossless FRET interchange. The minimum requirement model transfers; verification planning receives a conceptual refinement but no source syntax yet.

## Phase 8 — Add one evidence-driven tool

**Question:** Which repeated source-level task most benefits from focused tooling?

Choose one observed friction point after transferability testing. The leading candidates are an incoming/transitive decomposition query and a baseline-bound verification-coverage query. Prefer the smaller query that exercises existing model information without prematurely defining the whole verification-plan language. Keep the tool independent of source ownership and avoid refactoring the probe into a general platform unless the experiment requires it.

**Learning milestone:** Demonstrate that a focused tool adds useful capability while plain source and Git remain authoritative.

Result: [Experiment 0005](../experiments/0005-incoming-trace-query/README.md) adds one `--incoming ID` query to the GraalVM native probe. It builds a disposable in-memory reverse index and emits deterministic shortest child-to-parent paths. The UAS query exposes six direct and four second-level incoming requirements for `OPS-001` without source changes or persistent state. The dependency-free harness now passes 12 grouped tests.

## Phase 9 — Consolidate a provisional 0.1 contract

**Question:** Is the accumulated evidence coherent enough for independent trial use?

Reconcile the project brief, mission, principles, conceptual model, source-language specification, conformance fixtures, diagnostics, compatibility policy, native packaging, and CI validation guidance.

**Learning milestone:** Publish a small provisional contract that another implementation or engineering team can evaluate without inheriting experimental accidents.

Result: [Specification 0003](../specification/0003-provisional-0.1-contract.md) consolidates mission, audience, authoritative source behavior, baselines, verification concept ownership, conformance, diagnostics, optional trace querying, native reference use, CI trial guidance, compatibility boundaries, exclusions, and a trial checklist. The [`conformance/0.1`](../conformance/0.1/README.md) fixtures provide a normative valid interpretation example and representative invalid cases. Annotated tag `provisional-0.1` identifies the complete contract baseline.

## Phase 10 — Investigate ReqIF interchange

**Question:** Can mundane-req exchange requirements without allowing the interchange format to own the source model?

Study the applicable ReqIF standard and real implementations, define an explicit fidelity mapping, then test bounded export, import, and eventually round-tripping. Record preserved, transformed, and unrepresentable information.

**Learning milestone:** Determine whether useful ReqIF interoperability is possible and which interchange concerns, if any, justify changes to the core model.

## Near-term success criterion

A small formally traced requirements set can be:

- read and edited as ordinary text;
- changed on a branch and reviewed through an ordinary Git diff;
- followed from a lower-level requirement through rationale and decomposition;
- captured at two meaningful repository baselines and compared;
- merged and understood later from the repository alone.

Passing this criterion would justify continued language and toolchain work. It would not demonstrate a complete requirements-management product.

Current result: Experiments 0001 through 0003 meet this criterion for the small synthetic UAS corpus. Experiment 0004 shows that the minimum requirement model also transfers to an independently structured 19-requirement FRET case study without grammar changes. Operational scale and independent-team authoring remain unproven.
