# Research 0002: Dronology-Informed Source Experiment

Status: Proposed

## Purpose

This experiment will compare several candidate source representations by using them in ordinary text and Git workflows before selecting a syntax or implementation architecture.

The experiment is intended to answer a narrow question:

> Can a small requirements corpus remain understandable, traceable, and reviewable as ordinary source while supporting realistic formal-traceability work?

It is not intended to prove a complete requirements-management product.

## Relationship to Dronology

[Dronology](https://dronology.info/datasets/) is a safety-oriented unmanned-aircraft research system with requirements, design definitions, tasks, code, and trace links. It provides realistic evidence about artifact granularity and relationship patterns.

The official site invites research use and asks users to cite the project, but it does not publish a clear redistribution license for the first dataset. A third-party copy labels the data as MIT, but that does not establish authority to relicense the original material.

Therefore:

- the external Dronology dataset may be inspected and cited as research evidence;
- no original Dronology text will be committed to this repository without adequate permission;
- the experiment corpus will be newly written;
- the corpus may adopt the UAS domain and workflow shape, but not copy Dronology requirements;
- synthetic history will be identified explicitly rather than presented as project history.

## Scenario

The corpus will describe a small UAS mission-control capability concerned with vehicle registration, activation, flight-plan execution, loss of communication, and safe recovery.

This slice is useful because it can include:

- operational and system-level requirements;
- lower-level requirements allocated to mission control, a ground-control adapter, and a vehicle manager;
- requirements derived from a safety analysis;
- one-to-many decomposition;
- cross-cutting timing and identification constraints;
- rationales and assumptions;
- a coherent change with downstream impact.

The scenario remains requirements-only. Design elements, test cases, hazards, and change requests may be referenced in workflow descriptions, but they will not silently become additional mundane-req artifact types.

## Corpus shape

The initial corpus should be deliberately small:

- approximately 12–18 requirements;
- two or three levels of abstraction;
- at least one parent collectively addressed by several children;
- at least one explicitly self-derived requirement;
- at least one requirement with rationale that is not obvious from its statement;
- at least one requirement whose normative meaning includes a nontrivial mathematical expression;
- at least one external source reference;
- approximately 15–25 requirement-to-requirement relationships;
- one authored specification view with contextual prose and deliberate ordering.

The exact counts are constraints against both toy examples and premature scale testing. They may change if drafting shows that a smaller coherent set exercises the same questions.

Initial result: [Research 0004](0004-uas-semantic-corpus.md) defines syntax-neutral Baseline A content with 18 requirements, 21 decomposition relationships, one self-derived timing requirement, one mathematical statement, and the authored view order. It also defines the exact semantic change leading to a 20-requirement Baseline B.

## Synthetic history

### Baseline A — reviewed initial capability

Baseline A will contain a coherent reviewed set. It will be identified by a Git commit and an annotated experimental tag.

The tag demonstrates snapshot mechanics only. It must not be described as proof of organizational approval or certification.

Initial result: Baseline A is recorded by annotated tag `experiment-0001-baseline-a` at commit `8ea6e5f`.

### Proposed change

A synthetic change request will introduce a safety-driven communication-loss response. It should exercise several kinds of source change:

1. tighten a quantitative response time in one existing requirement;
2. add a lower-level requirement needed to satisfy the revised behavior;
3. split an overly compound requirement while preserving understandable identity history;
4. add, remove, or retarget decomposition relationships;
5. update rationale without mixing it with the change-request rationale;
6. move or reorder a requirement in the specification view without changing its identity;
7. leave at least one plausible downstream relationship requiring impact review.

The numeric threshold and domain facts will be invented for the experiment and clearly marked as illustrative, not engineering advice.

### Baseline B — accepted changed capability

After review corrections, the changed corpus will be committed and tagged as Baseline B. The comparison from A to B must be understandable using ordinary Git tools.

Initial result: [Experiment 0001's review](../experiments/0001-source-representations/baseline-a-to-b-review.md) records the ordinary-diff observations, human impact disposition, and one title correction accepted before the Baseline B tag.

## Workflows under test

### Workflow 1: Review a proposed change

A reviewer receives a branch or pull-request-style diff and must determine:

- which normative statements changed;
- which requirements were added or removed;
- whether identity was preserved or replaced intentionally;
- which relationships changed;
- why the change was proposed;
- whether unrelated formatting noise obscures the review.

The reviewer should not need a custom renderer to answer these questions.

### Workflow 2: Follow rationale and decomposition

Starting from a lower-level requirement, a reader must determine:

- which higher-level requirement or source justifies it;
- whether it is allocated, decomposed, or self-derived;
- why it exists;
- what other requirements jointly address the same parent;
- whether the relevant context is understandable from source alone.

This workflow will test whether rationale belongs on requirements, relationships, or both.

### Workflow 3: Compare baselines

A reader compares Baselines A and B and must determine:

- the meaningful content changes;
- additions, removals, and identity-preserving moves;
- relationship changes;
- the scope and purpose of each experimental baseline;
- whether an ordinary Git comparison is sufficient for the core review.

A later semantic comparator may add value, but the source representation fails this experiment if it is required for basic comprehension.

## Prior-art and standards study

Candidate selection must be preceded by a focused representation study. Its purpose is to learn from existing decisions and failure modes, not merely to collect syntax examples.

Initial result: [Research 0003](0003-representation-prior-art.md) compares the selected projects, standards, carriers, and file-granularity choices. Its revised slate makes Markdown one candidate rather than a shared assumption and compares the same purpose-built source grammar at two file granularities. The study records dated evidence and design inferences separately; it does not adopt a dependency or final syntax.

The initial project set should include at least:

- StrictDoc;
- Doorstop;
- Sphinx-Needs;
- OpenFastTrace;
- T-Reqs or another Git-centered industrial or research approach.

The standards and carrier-format set should include at least:

- ReqIF, as an interchange and conceptual-model reference rather than a presumed authoring format;
- SpecIF, for its separation of resources, statements, and hierarchy views;
- Markdown or CommonMark and reStructuredText directive approaches;
- YAML and TOML as visibly structured text carriers;
- LaTeX-style mathematical notation as possible field content and a separately rendered artifact;
- any requirements-language standard or notation that materially constrains statement form, identity, relationships, or document composition.

For each project or standard, record:

- authoritative purpose, maintenance status, license, and provenance;
- source syntax and the amount of non-requirement notation;
- requirement and relationship identity;
- file and record granularity;
- treatment of narrative context and document composition;
- relationship vocabulary and direction;
- ordinary diff, move, rename, and merge behavior;
- dependence on formatters, renderers, databases, or custom editors;
- extension strategy and likely failure modes;
- ideas worth testing, ideas that conflict with mundane-req, and unresolved questions.

Implementation code must not be copied merely because a project is open source. Ideas, source compatibility, dependencies, and code reuse are separate decisions.

## Candidate representations

At least three materially different combinations of source syntax and file granularity should encode the same corpus and history.

The initial candidate slate selected in Research 0003 is:

1. a purpose-built, line-oriented plain-text language with several requirements per module and a separate non-Markdown view;
2. the exact same language and view notation with one requirement per file;
3. document-embedded requirement blocks in Markdown.

The first and second candidates hold syntax, field-content notation, view notation, and semantics constant so file granularity is tested directly. The third tests the credible alternative of using a familiar document markup as the host and authored view.

Initial record-syntax result: [Research 0005](0005-purpose-built-record-syntax-sketches.md) selects explicit keyword records with indented multiline fields for the Candidate A/B experiment and records the agreed prose-folding and mathematical-payload rules.

Initial view-notation result: [Research 0006](0006-non-markdown-view-notation.md) supplies a deliberately disposable flat view fixture with ID-only requirement references. Its value and friction will be observed during full-corpus use rather than treated as a language-design prerequisite.

Initial encoding result: [Experiment 0001](../experiments/0001-source-representations/README.md) encodes Baselines A and B in all three candidates. Candidates A and B contain byte-identical record bodies at different file granularities; Candidate C places those same bodies in explicit fenced blocks within a Markdown-authored specification.

The comparison should include coherent candidates representing:

- purpose-built modules containing multiple requirements;
- purpose-built single-requirement files using the same grammar;
- prose-oriented Markdown documents containing multiple explicit requirement blocks.

File granularity is an explicit experimental variable. Candidates may differ in requirement-per-file boundaries, relationship placement, and whether specification views are colocated with or separated from requirement records.

The experiment should not mechanically pair every syntax with every possible layout. The first two candidates provide the necessary granularity control. If the purpose-built and Markdown candidates leave a narrower syntax question unresolved, use a focused crossover rather than adding another full corpus automatically.

The candidates must not differ only cosmetically. Each should represent a genuine tradeoff in record boundaries, metadata placement, relationship notation, composition, navigation, diff locality, and merge behavior.

No candidate is privileged because it resembles the eventual parser an implementer might prefer.

## Evaluation protocol

For each candidate:

1. encode the same Baseline A corpus;
2. record the file count, requirements per file, relationship placement, and view-file structure;
3. read and navigate it in an ordinary text editor without rendering;
4. inspect the mathematical requirement in raw source and, separately, in an optional rendered form;
5. search for IDs, terms, relationships, rationales, and mathematical notation with ordinary text search;
6. apply the same proposed change on a branch, including one move or reorganization;
7. inspect the raw Git diff and Git's rename or move presentation;
8. make concurrent edits to different requirements and to the same requirement;
9. test at least one clean merge and one realistic conflict;
10. resolve the conflict using only the source;
11. compare the two tagged baselines;
12. follow selected decomposition and rationale paths manually;
13. record friction, ambiguity, file-operation overhead, notation-rendering assumptions, and representation-specific workarounds.

A parser is not initially required. If deterministic interpretation cannot be judged confidently by inspection, the smallest possible parsing probe may be justified as a later experiment.

Initial merge result: [Experiment 0001's concurrent-edit review](../experiments/0001-source-representations/concurrent-edit-merge-review.md) records clean independent edits, readable competing-value conflicts, and a move-versus-edit conflict caused by Candidate C's coupling of authoritative records to Markdown document position.

## Evaluation rubric

| Dimension | Question |
| --- | --- |
| Standalone readability | Can an unfamiliar engineer understand requirements and context without specialized tools? |
| Diff clarity | Does a meaningful change produce a small, obvious diff with little incidental noise? |
| Merge locality | Do independent edits remain local, and are conflicts understandable? |
| File granularity | Does the chosen object-to-file boundary improve change isolation without making navigation or repository operations burdensome? |
| Repository navigation | Can readers understand grouping and context without opening an unreasonable number of files? |
| Move behavior | Are identity-preserving moves and reorganizations clear in both source and ordinary Git presentation? |
| Identity stability | Can movement, reordering, and statement changes occur without accidental identity loss? |
| Reference legibility | Can humans read and author references without excessive ceremony? |
| Relationship visibility | Are added, removed, and retargeted relationships evident in ordinary diffs? |
| Context preservation | Can an object remain independent of document position without becoming semantically isolated? |
| Authoring effort | Is routine editing plausible in an ordinary editor? |
| Syntactic noise | How much notation competes with the engineering content? |
| Embedded notation | Can mathematical source be identified, read, diffed, searched, and rendered without hidden state? |
| Parse determinism | Can a conforming tool interpret the source without heuristics or hidden state? |
| Validation potential | Can duplicates, missing fields, and dangling references be diagnosed clearly? |
| Searchability | Do ordinary line-oriented search tools remain useful? |
| Composition | Can one requirement appear in a deliberate specification view without duplication? |
| Durability | Would the repository remain intelligible if all mundane-req tools disappeared? |

Observations should include concrete examples rather than a single aggregate score. Basic measures such as changed lines, incidental lines, conflict size, and lookup steps may supplement qualitative notes.

## Decision rule

A representation should be rejected or substantially revised if:

- simple normative changes are obscured in raw diffs;
- ordinary editing routinely requires a formatter or custom editor;
- movement or view changes destroy identity or create broad diff noise;
- file boundaries create excessive navigation or file-operation overhead;
- unrelated requirements share unnecessarily large conflict surfaces;
- relationship edits are difficult to recognize;
- parsing depends on ambiguous prose conventions;
- the representation requires concepts not exercised by the workflows;
- a custom renderer is necessary for basic review of ordinary prose, identity, rationale, or relationships;
- embedded mathematical notation depends on hidden macros, packages, or renderer configuration.

The experiment may conclude that no candidate is yet adequate.

## Deferred from this experiment

- production parser architecture;
- renderer or web application;
- semantic diff implementation;
- complete Dronology conversion;
- large-corpus performance;
- ReqIF import or export;
- verification evidence storage;
- variants and reuse;
- electronic signatures or certification claims;
- cross-repository configuration management.

## Questions to settle before encoding

Research 0003 resolves the prior-art set, revised candidate slate, and direct granularity comparison. Research 0004 adopts the provisional decomposition, source, and allocation meanings and applies them to the semantic corpus. Research 0005 selects the shared Candidate A/B record form for the experiment. Research 0006 supplies a working view fixture without making it a product decision. The remaining questions are:

1. Which constrained LaTeX-style mathematical profile should all candidates use?
2. Which concurrent edits and moves will provide the smallest realistic merge experiment?
3. Which additional navigation observations are necessary before comparing the candidates fairly?
