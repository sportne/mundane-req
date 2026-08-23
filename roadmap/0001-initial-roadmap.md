# Roadmap 0001: From Source Experiment to Composable Toolchain

Status: Draft living roadmap

Last reconciled: 2026-08-22 at `mundanereq-source-0.2`

Execution is decomposed into the [roadmap task-card index](0002-task-card-index.md). This document remains the strategic narrative; the cards carry bounded work, dependencies, and acceptance evidence.

## Purpose

This roadmap describes how mundane-req should progress from a successful source-language investigation to a usable requirements-engineering toolchain.

The destination is not one application that owns authoring, storage, review, traceability, rendering, interchange, and workflow. The destination is a durable textual requirements language in Git, surrounded by independent purpose-built tools that can be used together or separately:

```text
authoritative .mreq source and Git history
    |
    +-- ordinary editor and text search
    +-- ordinary Git diff, merge, history, tags, and forge review
    +-- mundanereq-validate
    +-- mundanereq-format
    +-- mundanereq-trace
    +-- later focused linters, renderers, interchange tools, and editor support
```

The tools may share implementation code. They must not become a mandatory platform, proprietary store, or alternate authority. A repository must remain understandable if every mundane-req executable disappears.

The roadmap is organized around learning questions and decision gates. A listed later capability is not a promise to build it. Work should continue only when the preceding evidence justifies it, and the roadmap should change when experiments contradict it.

## Product direction

mundane-req is for systems-engineering teams that need formal traceability and want requirements work to behave more like software development:

- requirements are ordinary human-readable source artifacts;
- Git supplies history, branches, merges, repository snapshots, and the substrate for baselines;
- a forge supplies ordinary change proposals, review discussion, and review workflow;
- CI runs independent checks over a clean checkout;
- validators, formatters, trace analyzers, renderers, and interchange adapters add capabilities without owning the source;
- generated indexes, matrices, documents, databases, and reports remain disposable unless a specific delivery obligation makes one authoritative.

The project should prefer several small tools with clear contracts over one command that gradually becomes a requirements-management platform. It should also prefer reuse inside the implementation where reuse reduces inconsistency without coupling tools unnecessarily.

## Where the project is now

The initial feasibility investigation is complete. The project is between a validated language prototype and its first usable tool suite.

### Evidence already earned

1. **Practice and prior-art survey.** The [practice survey](../research/0001-requirements-management-practice-survey.md) and [representation study](../research/0003-representation-prior-art.md) separate common requirements-management workflows from the architectural choices of existing products. StrictDoc, Doorstop, ReqIF, lightweight markup, and language-tooling approaches have informed the work without becoming mandatory dependencies or storage models.
2. **Source-representation comparison.** [Experiment 0001](../experiments/0001-source-representations/README.md) compared a purpose-built keyword record syntax using multiple file granularities with a Markdown-document representation. Complete records, IDs inside records, non-semantic file boundaries, and ordinary Git merge behavior survived the comparison. [Research 0007](../research/0007-provisional-source-representation-decision.md) records the resulting decision.
3. **Minimum semantic model.** [Specification 0002](../specification/0002-minimum-source-language-and-model.md) defines requirements with an ID, title, statement, optional allocation, optional rationale, optional external source, and zero or more directed `decomposes` relationships. Repository snapshots supply requirement revisions and the initial baseline substrate.
4. **Deterministic interpretation.** [Experiment 0002](../experiments/0002-deterministic-interpretation/README.md) provides a dependency-free Java 21 probe that parses and validates the language, creates a semantic inventory, and produces source-positioned diagnostics. It builds successfully as a GraalVM native executable.
5. **Sustained authoring and Git use.** [Experiment 0003](../experiments/0003-sustained-authoring/README.md) exercised addition, splitting, coordinated normative changes, reallocation, file movement, retirement, baseline comparison, and concurrent edits as ordinary commits and diffs.
6. **Transferability.** [Experiment 0004](../experiments/0004-transferability/README.md) represented nineteen requirements from the licensed NASA FRET Lift-Plus-Cruise case study without a grammar change. The study separated requirement-source preservation from lossless interchange and separated verification planning from requirement fields.
7. **Focused trace analysis.** [Experiment 0005](../experiments/0005-incoming-trace-query/README.md) derives incoming and transitive decomposition paths from an in-memory reverse index without adding redundant reverse links to source.
8. **Formal source contract.** The normative [0.2 language standard](../specification/0005-mundanereq-source-language-0.2.md), [trial contract](../specification/0006-provisional-0.2-contract.md), and [conformance fixtures](../conformance/0.2/README.md) state the language independently of the reference probe. Version 0.2 adds only the nonsemantic full-line author comments tested by [Experiment 0007](../experiments/0007-source-comments/README.md).
9. **Bounded ReqIF experiment.** [Experiment 0006](../experiments/0006-reqif-interchange/README.md) provides a second Java 21/GraalVM probe that performs schema-valid semantic self-roundtrips through a deliberately narrow ReqIF 1.2 profile. The experiment did not require changing authoritative `.mreq` source.

### What has not yet been established

- The experiment probe is not a maintained first implementation of the standard.
- Three narrow Unicode conformance discrepancies identified during formalization remain to be repaired and tested.
- Parsing, validation, inventory output, and one trace query currently coexist in an experimental class rather than in deliberate reusable components.
- There is no formatter or formatting contract.
- There is no independently packaged traceability executable.
- Separate tools have not yet been exercised together in a normal edit-review-CI workflow.
- Operational scale, independent-team authoring, and sustained use on a substantial real corpus are unproven.
- Requirement identity across ID corrections remains unresolved.
- Verification, safety classification, controlled vocabularies, and similar information have conceptual questions but no selected companion source models.
- ReqIF work has demonstrated only a self-roundtrip through a bounded profile, not practical interoperability with an independent tool.
- There is no stable language or tool compatibility promise.

The next objective is therefore not to enlarge the requirement language. It is to turn the evidence-backed 0.2 language into a small, coherent, native toolchain and use that toolchain to expose the next real modeling problems.

## First implementation hypothesis

The first maintained implementation should use Java 21 and GraalVM Native Image. The intended deliverables are separate native executables with working names:

- `mundanereq-validate` — source discovery, parsing, language conformance, and plain diagnostics;
- `mundanereq-format` — deterministic formatting and formatting checks without semantic change;
- `mundanereq-trace` — derived trace navigation and analysis over valid source sets.

They may be built from one repository codebase and share a small set of Java components. Likely shared responsibilities are:

- physical source reading and explicit source-set discovery;
- a concrete source representation that retains comments and other syntax needed for faithful rewriting;
- the normative semantic requirement model;
- parsing and source-position tracking;
- diagnostic values and presentation conventions;
- an in-memory requirement and relationship index.

This is an architectural hypothesis to test, not permission to construct a generalized compiler framework. Module boundaries should be introduced because at least two real tools need them. The shared code must not:

- require a database, daemon, server, repository manifest, or network service;
- make one executable invoke another internally as its library interface;
- mix formatting trivia into the semantic requirement model;
- make trace-derived reverse links authoritative;
- expose a prematurely stable Java API merely because the command-line tools share classes;
- create a plugin architecture or configurable metamodel.

Each executable should accept explicit files or directories, operate from a clean checkout, return meaningful exit status, produce useful plain-text output, and build as a standalone native image. Native executables are platform-specific, so the first supported build target may be Linux; additional packaging targets should follow demonstrated users rather than precede them.

The existing probes are evidence and potential source material. They should not be mechanically promoted into production structure. In particular, the formatter will need concrete source and comment information that the current semantic inventory intentionally discards.

## Primary sequence

```text
completed source investigation
    -> close the 0.2 conformance gap
    -> establish the smallest shared Java foundation
    -> deliver validator, formatter, and trace executables
    -> exercise them together in Git and CI
    -> run a real team/corpus trial
    -> resolve model pressure with focused experiments
    -> add only the independent tools justified by use
    -> decide whether a stable 1.0 contract is warranted
```

Stages 2 through 5 define the first implementation. Their order permits learning from each tool, but the goal is a coherent suite rather than one executable absorbing the others.

## Stage 0 — Preserve the completed investigation

Status: Completed

**Question:** Is human-readable requirements source in Git credible enough to justify purpose-built tooling?

Experiments 0001 through 0007 and the provisional 0.2 contract answer yes for the bounded corpora and workflows tested. The source is readable without tools, behaves intelligibly in ordinary diffs and merges, can be parsed deterministically, transfers to a second corpus, supports derived trace navigation, and can participate in a bounded ReqIF mapping.

**Decision:** Continue with the purpose-built record language and non-semantic file boundaries. Keep Git and `.mreq` source authoritative. Do not infer operational readiness, language stability, or a complete requirement model from the experiments.

## Stage 1 — Establish an exact 0.2 implementation baseline

Status: Next

**Question:** Can the formal 0.2 standard and conformance suite serve as an unambiguous contract for the first maintained implementation?

### Work

1. Add conformance fixtures for the Unicode whitespace, C1 control-character, and Unicode-scalar diagnostic-column discrepancies identified by the [formalization review](../research/0008-source-language-formalization-review.md).
2. Correct the three known discrepancies in the experimental parser and verify both JVM and native-image execution.
3. Review the 0.2 conformance corpus against the normative standard, including comment placement and semantic omission.
4. Define minimal cross-tool conventions for input selection, exit status, source coordinates, and human-readable diagnostics. Standardize only behavior needed for tools to work predictably in editors and CI.
5. Record whether the reference probe can be evolved safely or whether the maintained implementation should be constructed alongside it and compared through conformance fixtures.

### Learning milestone

An implementation can claim strict 0.2 interpretation without known deviations, and the conformance suite is strong enough to protect extraction of shared code.

### Decision gate

If formal rules prove unreasonable in real source handling, revise the provisional language explicitly rather than preserving accidental implementation behavior. Otherwise freeze the 0.2 semantic target for the first tool suite.

## Stage 2 — Extract the smallest shared foundation

Status: Planned

**Question:** What code genuinely needs to be shared by validation, formatting, and trace analysis?

### Work

1. Establish a maintained source area distinct from the historical experiment implementations.
2. Separate physical source, concrete syntax, semantic model, and derived indexes so that each layer has one clear responsibility.
3. Preserve source comments, record boundaries, physical locations, and any other trivia required by the formatter without adding those values to requirement semantics.
4. Parse once into data usable by both semantic consumers and faithful source rewriting.
5. Keep source discovery deterministic and independent of repository databases or manifests.
6. Select the smallest reproducible Java build arrangement that can test shared code and create three GraalVM native images. Do not add a dependency framework merely to obtain project structure.
7. Port existing parser tests and semantic-inventory comparisons before removing any duplication from the probes.

### Learning milestone

The three intended executables can depend on a small common foundation without depending on one another, and the native build remains straightforward.

### Decision gate

Retain shared code only where it prevents conflicting interpretations. If a proposed abstraction serves only imagined future tools, defer it.

## Stage 3 — Deliver `mundanereq-validate`

Status: Planned

**Question:** Can a clean checkout receive a fast, precise answer about whether its requirement source conforms to a named language contract?

### Minimum capability

- select explicit files and directory trees according to the 0.2 rules;
- report lexical, syntactic, identity, cardinality, and referential failures with source positions;
- distinguish language-conformance failures from optional project-policy failures;
- provide stable success and failure exit behavior for CI;
- identify the implemented source contract;
- optionally summarize selected files, requirement counts, and relationship counts without making inventory text a normative interchange format.

### Experiment

Run the validator against every conformance fixture and project corpus from a clean checkout on both the JVM and the native executable. Exercise editor-style repair of representative failures and ordinary CI log output. Measure startup and repository-scan behavior only far enough to catch obvious native-tool regressions.

### Learning milestone

`mundanereq-validate` is the first maintained, independently usable tool. It can replace the validation role of the experiment probe without changing source semantics or requiring project-specific state.

### Decision gate

Do not add prose-quality rules, controlled vocabularies, decomposition completeness, or workflow checks to core conformance. A check that expresses project policy must remain identifiable as policy and should be added only after a concrete workflow defines it.

## Stage 4 — Define and deliver `mundanereq-format`

Status: Planned

**Question:** Can mundane-req remove avoidable formatting debate and diff noise while preserving authored meaning and useful source comments?

### Questions to settle before implementation

- Which whitespace and wrapping choices are canonical?
- Should prose be reflowed, and at what width, or should the first formatter preserve author wrapping?
- Which original line-ending choice, if any, should be preserved?
- How are nonsemantic comments retained without inventing semantic attachment to a nearby field or record?
- Must opaque mathematical payloads remain character-for-character unchanged?
- Does formatting preserve record and relationship order even where that order is non-semantic?
- Which operations write files, print formatted source, or perform a CI-friendly check only?

The safest first hypothesis is conservative formatting: normalize unambiguous structural layout, preserve source order and comment order, and never rewrite opaque math payloads. Prose reflow should be tested rather than assumed.

### Required properties

1. **Semantic preservation:** parsing before and after formatting produces the same semantic inventory.
2. **Idempotence:** formatting already formatted input produces identical bytes.
3. **Comment preservation:** comment text and relative source order survive even though comments do not enter the semantic model.
4. **Visible behavior:** every rewrite is understandable in an ordinary Git diff.
5. **Safe operation:** check and standard-output modes precede or accompany any in-place write mode.
6. **Native delivery:** the formatter is its own GraalVM native executable and does not require the validator executable at runtime.

### Experiment

Construct deliberately inconsistent but valid fixtures, including multiple records per file, one record per file, comments at every valid boundary, long prose, paragraph breaks, CRLF input, and LaTeX payloads. Compare conservative and prose-reflow policies through ordinary diffs. Run semantic-equivalence and idempotence checks over every maintained corpus.

### Learning milestone

The project selects the smallest formatting contract that creates useful consistency without erasing intentional source structure or turning formatting into semantic editing.

### Decision gate

If comment preservation requires complex inferred attachment or a lossless tree substantially larger than the language warrants, narrow the formatter rather than adding comment semantics.

## Stage 5 — Define and deliver `mundanereq-trace`

Status: Planned

**Question:** Which traceability questions can be answered directly and usefully from the current source model?

### First capability

- direct outgoing decomposition targets for a requirement;
- direct incoming decomposition sources derived in memory;
- deterministic transitive paths in either direction;
- focused change-impact navigation for one or more IDs;
- clear diagnostics when an ID is absent or the source set is invalid;
- optional structural observations such as cycles, clearly distinguished from universal language validity.

The current `--incoming` experiment supplies evidence and an algorithm, but not the eventual tool boundary or interface. The trace executable should consume the shared semantic model, build disposable indexes, and leave authoritative source unchanged.

### Experiment

Use the sustained-authoring history to answer the trace questions that previously required repeated search. Compare the tool output with manual inspection for addition, splitting, retargeting, coordinated change, and retirement. Test graphs with multiple shortest paths and cycles before specifying deterministic output.

### Learning milestone

`mundanereq-trace` materially improves navigation and impact analysis while proving that reverse links and graph indexes need not be stored in requirement records.

### Decision gate

Do not generalize `decomposes` into arbitrary relationship types merely to make the trace engine look reusable. New relationship semantics must come from a demonstrated engineering workflow and a model decision.

## Stage 6 — Exercise the tools in concert

Status: Planned

**Question:** Do three independent native tools collectively create a better requirements workflow than either raw text alone or a monolithic application?

### Trial workflow

1. Clone a clean repository.
2. Read and edit `.mreq` files in an ordinary editor.
3. Run the formatter in check or write mode.
4. Run language validation locally and in CI.
5. Use trace queries to inspect the impact of a proposed change.
6. Review the ordinary source diff in Git and a normal forge pull or merge request.
7. Merge through the forge and identify a baseline with an annotated Git tag and project convention.
8. Rebuild every index or report from the tagged source.

### Work

- provide concise installation and clean-checkout usage documentation;
- produce reproducible Linux native builds for all three tools;
- keep each CI invocation explicit so failures name the responsible tool;
- create an example repository workflow rather than a custom review system;
- test formatter, validator, and trace versions independently enough to expose accidental coupling;
- record what remains understandable when only source and Git are available.

### Learning milestone

A systems-engineering team can use the first tool suite in a software-like edit-check-review-merge-baseline loop. Each executable adds one recognizable capability, and removing any one executable degrades only that capability.

### Decision gate

If ordinary use requires a wrapper command, configuration file, or generated index, identify the exact need before adding one. Convenience must not silently become a mandatory project runtime.

## Stage 7 — Test operational use and scale

Status: Planned after the first toolchain trial

**Question:** Does the approach remain understandable with a larger corpus and authors who did not design the language?

### Work

1. Obtain or develop a substantially larger, legally usable requirements corpus with multi-level formal traceability.
2. Have at least one independent author work from the specification and tool documentation rather than from project history.
3. Exercise both subject-file and one-record-per-file layouts without assigning semantics to either.
4. Run concurrent branch changes, review, conflict resolution, baseline comparison, formatting, validation, and trace impact analysis.
5. Observe scan time, native startup, memory use, diagnostic volume, output navigability, and CI behavior.
6. Record every workaround and classify it as a source-language, conceptual-model, tool, Git/forge, project-policy, or training issue.
7. Attempt a second conforming parser or independent implementation review if the cost is proportionate; this is stronger evidence than further testing of one codebase alone.

### Learning milestone

The project has evidence about team adoption and corpus scale, not merely deterministic behavior on fixtures.

### Decision gate

Prefer improvements to diagnostics, documentation, or focused tools over grammar growth when they solve the observed problem. Add indexes or caches only if measured performance requires them, and keep them disposable.

## Stage 8 — Resolve model pressure through companion-artifact experiments

Status: Open research track; decisions must be evidence-driven

**Question:** Which engineering facts belong to a requirement, which belong to relationships, and which belong to separately managed assessments or activities?

This stage should test workflows, not add a general attribute bag. Candidate studies include:

### Identity continuity

Test correction of a human-facing ID across Git baselines, trace links, and interchange. Decide among ordinary replacement, an explicit continuity assertion, or a separate durable identity. Do not introduce hidden IDs without evidence that Git history and atomic link updates are insufficient.

### Verification planning and evidence

Develop the smallest human-readable model that can distinguish a verification activity, planned coverage of requirement revisions, executions, evidence, and results. Keep passing status and evidence out of timeless requirement fields.

### Safety classification and other assessments

Compare an inline requirement field with a separate baseline-bound classification artifact. Exercise changes caused by hazard analysis, multiple safety schemes, product variants, rationale, assessor authority, and unchanged requirement text. Decide whether the authoritative fact is requirement content, a relationship, or an assessment assertion. Avoid duplicate authoritative copies merely for display convenience.

### Allocation and controlled vocabulary

Use a corpus with real component identity, renaming, heterogeneous allocation targets, and project vocabulary. Determine whether the current label is sufficient or whether allocation should reference separately identified model objects.

### Glossary and formal symbols

Test whether shared terms and mathematical symbols need a human-readable companion artifact. Do not turn opaque LaTeX or requirement prose into an executable expression language without a concrete analysis need.

### Trace policy

Define example project policies for required decomposition coverage, permitted cycles, allowed allocation values, or verification coverage. Determine which checks are reusable while keeping policy failure distinct from source-language invalidity.

### Views and specifications

Revisit authored ordering or document composition only for a demonstrated delivery or review workflow. Requirement files must remain the model; a specification or report should remain a view unless contrary evidence is compelling.

### Learning milestone

For each study, record the observed workflow, underlying engineering need, ownership and revision semantics, candidate representations, ordinary Git behavior, and whether language support is necessary at all.

### Decision gate

Add a source field, relationship, or companion language only when the experiment shows that independent tooling and project policy cannot express the need clearly over existing authoritative source. Prefer a small explicit model over arbitrary attributes or a generalized metamodel.

## Stage 9 — Add focused ecosystem tools only where use justifies them

Status: Future options, not a feature commitment

**Question:** After the first toolchain and operational trial, which independent capability delivers the next largest improvement?

Candidates include:

- a linter for explicitly selected prose or project-policy checks;
- a renderer for disposable HTML or a delivery document;
- a semantic baseline comparison tool that supplements rather than replaces `git diff`;
- a verification-coverage or safety-assessment analyzer if Stage 8 selects those models;
- editor or language-server support built over the same parser without making a specialized editor mandatory;
- a ReqIF converter that advances the bounded profile through a real independent-tool roundtrip;
- machine-readable analysis output for integration with other independently written tools.

Each candidate should normally become its own executable or optional integration. Java and GraalVM Native Image remain the default implementation direction for the core command-line suite, but implementation reuse is not a language requirement and should not prevent independent implementations.

### ReqIF milestone

Before expanding the ReqIF profile, roundtrip the bounded export through an independent implementation such as Eclipse RMF/ProR. Exercise edits to IDs, rich text, relationships, ordering, and unknown attributes. Record preserved, transformed, rejected, and lost information. Add configurable mappings only if actual exchange partners require them.

### Derived presentation milestone

Before standardizing a view language, generate at least one useful specification, traceability matrix, or browser directly from the model using a disposable ordering convention. Determine whether authored composition is an engineering need or merely presentation convenience.

### Learning milestone

Every new tool has one stated engineering problem, consumes authoritative text, emits disposable results or explicit external interchange, and remains independently replaceable.

## Stage 10 — Decide whether to stabilize 1.0

Status: Future decision

**Question:** Is the source contract and toolchain mature enough to deserve compatibility promises?

A stable source-language contract should not be declared merely because three executables exist. Before 1.0, the project should have evidence that:

- independent users can understand and author the source from the written standard;
- the validator implements the standard without known deviations;
- formatter output is semantically preserving, idempotent, and accepted in normal review;
- trace analysis answers real formal-traceability workflows;
- a meaningful corpus and multi-author Git history have been maintained;
- language evolution and repository version selection are understood;
- identity correction and the most consequential model-pressure questions have explicit dispositions;
- conformance fixtures support another implementation without relying on reference CLI accidents;
- native tools can be built, versioned, distributed, and used independently;
- the project can state what 1.0 intentionally does not provide.

The language compatibility promise, command-line compatibility promises, Java implementation APIs, and interchange profiles must be versioned separately. Stabilizing human-readable source must not require freezing every tool interface simultaneously.

### Learning milestone

Publish 1.0 only if it represents a small demonstrated foundation that the project is prepared to preserve. Otherwise continue with explicit provisional contracts and reproducible Git tags.

## Cross-cutting rules for every stage

1. **Source remains sufficient for understanding.** Rich tools may improve navigation and analysis but may not hide authoritative requirement content in generated or private state.
2. **Ordinary Git behavior is part of evaluation.** Every representation or rewrite must be inspected through normal diffs, moves, merges, history, and forge review.
3. **Tools are composable and disposable.** No tool owns the repository. Generated state must be reproducible.
4. **Semantics precede syntax.** Establish what a fact means, who owns it, and which revision it describes before adding a field or companion grammar.
5. **Workflow precedes feature imitation.** Existing RM products provide evidence, not a checklist.
6. **Policy is not language validity.** Project-specific trace completeness, vocabulary, quality, approval, and governance checks should remain distinguishable from parsing and conformance.
7. **Separate tools may share code.** Shared implementation reduces inconsistent interpretation; separate executables preserve focused responsibilities and independent use.
8. **Native delivery is a product choice, not a source dependency.** The first tools target Java 21 and GraalVM Native Image, while the written language remains implementation-independent.
9. **Complexity must earn its place.** Databases, servers, plugins, generalized schemas, persistent indexes, custom review systems, and semantic merge remain deferred until measured need justifies them.
10. **Experiments end in decisions.** Each stage must record what was learned, what changed, what was rejected, and which question should be tested next.

## Immediate execution order

The next concrete work should proceed in this order:

1. close the three known 0.2 conformance gaps and strengthen fixtures;
2. specify only the cross-tool behaviors needed for predictable native command-line use;
3. extract the smallest shared concrete-syntax, semantic-model, parsing, diagnostic, and indexing foundation;
4. ship `mundanereq-validate` as the first maintained executable;
5. run the formatting-policy experiment and ship `mundanereq-format` only after semantic preservation and comment behavior are settled;
6. extract the existing incoming-query evidence into `mundanereq-trace` and broaden it only through trace workflows;
7. run all three executables together in a clean-checkout Git/forge/CI trial;
8. use the trial to choose the next model-pressure experiment or focused tool.

Completion of this sequence would produce the project's first credible toolchain: three small GraalVM native executables working in concert over durable source, while ordinary editors, Git, a forge, and CI continue to supply the surrounding development workflow.
