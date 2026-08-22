# Research 0003: Representation Prior Art and Experiment Candidates

Status: Initial study

Research snapshot: 2026-08-22

## Purpose

This study examines existing text-first requirements projects, interchange standards, and generic text carriers before mundane-req commits to a source representation.

It asks two related questions:

1. Which representation ideas have already been exercised elsewhere?
2. Which syntax-and-file-granularity combinations should the first source experiment compare?

This is prior-art analysis, not a dependency or feature-selection exercise. Documented behavior is distinguished from mundane-req inference. No implementation code or source syntax has been adopted by this study.

## Evaluation lens

Each approach was examined for:

- authoritative purpose, maintenance, license, and provenance;
- source syntax and non-requirement notation;
- stable identity and references;
- file and record granularity;
- narrative context and document composition;
- relationship vocabulary and direction;
- likely ordinary diff, move, rename, and merge behavior;
- dependence on formatters, renderers, databases, or custom editors;
- extension strategy and likely failure modes;
- ideas worth testing and ideas that conflict with mundane-req.

Maintenance observations are a dated snapshot, not a durability guarantee. License observations identify provenance pressure only; compatibility, organizational approval, and code reuse would require separate review.

## High-level comparison

| Approach | Authoritative source or exchange form | Typical record boundary | Identity and relationships | Main lesson for mundane-req |
| --- | --- | --- | --- | --- |
| StrictDoc | SDoc or supported Markdown documents | Multiple objects per document file | UID-based objects; explicit trace relations | A small fielded language can keep records legible while supporting authored documents |
| Doorstop | YAML or Markdown-with-front-matter item files | One linkable item per file | Item UID is the filename; parent links and review fingerprints | File-per-object gives strong diff locality but makes file operations, navigation, and filename semantics consequential |
| Sphinx-Needs | reStructuredText directives in Sphinx documents | Multiple needs per document file | Explicit or title-derived IDs; configurable links, fields, and types | A host markup extension can combine prose and objects, but configurability and toolchain coupling grow quickly |
| OpenFastTrace | Valid Markdown containing recognizable item forms | Multiple items per document file | Type, name, and manual revision form the ID; coverage and dependency links | Valid fallback markup is valuable, while heuristic boundaries and revision-in-identity create costs |
| T-Reqs | Markdown or arbitrary text with embedded element tags | Multiple elements in any text file | Generated IDs and typed element links; YAML traceability metamodel | Git-and-forge workflows have industrial value; embedding explicit boundaries in ordinary text is viable |
| ReqIF | XML interchange package | Many typed objects in one exchange | Identifiable SpecObjects, SpecRelations, Specifications, and hierarchies | Object identity, relations, attributes, and hierarchy can be separate even though the serialization is unsuitable for direct authoring |
| SpecIF | JSON integration dataset | Many resources and statements in one dataset | Resources, statements, and hierarchy nodes have separate roles | A model can be independent of one or more document views, but a generalized self-describing metamodel exceeds the initial need |

## Text-first project findings

### StrictDoc

**Documented approach**

StrictDoc is an Apache-2.0 open-source project for technical documentation and requirements management. Its public repository and documentation were active at the time of this study. The organizational provenance constraint already recorded for mundane-req means StrictDoc remains prior art rather than an intended dependency unless that decision is revisited explicitly.

The native SDoc example is a line-oriented, fielded format:

    [DOCUMENT]
    TITLE: Example

    [REQUIREMENT]
    UID: SYS-001
    TITLE: Safe recovery
    STATEMENT: The system shall command safe recovery after loss of communication.

An SDoc file is a document unit and can contain sections, requirements, and other documentation nodes. StrictDoc also documents experimental Markdown input. Traceable nodes use UIDs, and its documentation distinguishes document structure from relations such as Parent, Child, and File. Custom grammars can add node types, fields, and relation roles.

**Mundane-req inference**

- Explicit record markers and field labels are easy to locate in raw source and make deterministic parsing plausible.
- Multiple objects per file preserve surrounding narrative and reduce file count.
- Moving a record between files is likely to appear as deletion and insertion in ordinary line diffs even when its UID is stable.
- A grammar that rejects all unrecognized text can make authoring precise but may reduce the value of the file as an ordinary document.
- Custom grammars demonstrate extensibility, but adopting that power initially would conflict with the preference for a small fixed model.

**Worth testing**

- unmistakable record starts;
- labels for the few durable fields used by the experiment;
- explicit identity independent of file and position;
- document structure that does not imply decomposition.

**Do not inherit without evidence**

- configurable grammars;
- a large predefined node vocabulary;
- StrictDoc source or implementation code.

### Doorstop

**Documented approach**

Doorstop is an LGPL-3.0 requirements-management project under active development at the time of this study. It stores each linkable item in its own file. YAML is the default, and Markdown with YAML front matter is also supported. Items from one directory form a document; documents form a hierarchy.

The item UID is the filename without the extension. A document configuration file controls its prefix, parent document, numbering, and item format. Items can include text, links, hierarchy level, reference information, active or normative flags, arbitrary extended attributes, and review fingerprints. The text is Markdown.

Doorstop records review fingerprints for items and parent links so tooling can report changes requiring renewed review. Its documentation also warns about YAML interpretation, such as an unquoted value resembling a number being loaded as a number rather than preserved as the apparent text.

**Mundane-req inference**

- One file per object minimizes the textual conflict surface when different people edit different requirements.
- A change to one requirement usually creates a small, isolated file diff.
- Creating, splitting, or removing requirements becomes visibly equivalent to file creation, movement, or deletion.
- Using the filename as the UID couples identity to rename behavior and makes a human-facing identifier part of the filesystem interface.
- A large set becomes harder to browse as a coherent specification without an index, renderer, or disciplined directory conventions.
- Moving a requirement between documents becomes a path change. Git detects renames by content similarity rather than storing a semantic move, so edits made during the move can affect its ordinary presentation.
- Source-stored hashes are effective for a particular suspect-link workflow but add opaque diff content that mundane-req currently expects an analyzer to derive.
- Full YAML offers much more syntax and implicit typing behavior than a first requirements language needs.

**Worth testing**

- file-per-requirement diff and merge locality;
- Markdown body text with a very small structured header;
- explicit measurement of file creation, movement, navigation, and bulk editing effort.

**Do not inherit without evidence**

- identity derived from the filename;
- source-stored review fingerprints;
- document hierarchy as an implicit traceability hierarchy;
- arbitrary per-project attributes.

### Sphinx-Needs

**Documented approach**

Sphinx-Needs is an MIT-licensed, actively maintained Sphinx extension. It represents requirements, specifications, implementations, tests, and configurable need types as reStructuredText directives embedded in documentation:

    .. req:: User authentication
       :id: SYS-001
       :status: open
       :links: SYS-000

       The system shall authenticate an operator.

IDs may be explicit or generated from title content. Fields, statuses, need types, links, layouts, filters, templates, reports, and dynamic functions are configurable. Multiple needs and ordinary narrative can coexist in one document file.

reStructuredText directives are a general extension mechanism with explicit markers, arguments, options, and indented content. Unknown directives require a supporting processor and produce errors in the reference parser.

**Mundane-req inference**

- Directives give unambiguous object boundaries while preserving authored narrative around them.
- A recognized host-language extension can reuse headings, lists, inline formatting, and document tooling.
- Raw directives remain readable, but indentation and option syntax impose more ceremony than ordinary Markdown.
- Generated IDs based on titles are convenient but not stable when titles change.
- The breadth of configurable types, fields, links, templates, and executable filtering illustrates how a documentation extension can become an application platform.
- The representation is durable as text, but understanding its exact semantics depends on Sphinx-Needs configuration.

**Worth testing**

- an explicit block-extension shape;
- prose and structured objects in the same file;
- source-defined query or report views only after a basic representation works.

**Do not inherit without evidence**

- title-derived identity;
- executable expressions or dynamic functions in source;
- configurable need types, fields, and link schemas;
- Sphinx as a required authoring or rendering environment.

### OpenFastTrace

**Documented approach**

OpenFastTrace is a GPL-3.0, actively maintained requirement-tracing suite. Its native specification format is Markdown, and it intentionally keeps its item syntax valid Markdown. Informative prose and multiple normative items can coexist in one file.

An item ID consists of artifact type, name, and revision, separated by tildes. The manually incremented revision is intended to invalidate existing coverage when content changes semantically. Item content can include title, description, rationale, comment, status, tags, needed coverage types, covered item IDs, and dependencies.

The minimal record begins with an inline-code ID followed by its description. Keywords and their allowed order identify additional sections. Artifact types and trace chains are project-defined.

**Mundane-req inference**

- A source that remains valid ordinary Markdown provides excellent no-tool fallback rendering and familiar editing.
- The minimal record is concise and places normative material close to informative context.
- Recognizing an inline-code token as a record start and interpreting later keywords by position can make boundaries less explicit and increase collision or ordering risk.
- Embedding manual revision in the ID conflicts with the current hypothesis that Git records revisions while stable requirement identity survives content change.
- The coverage model is useful evidence for trace analysis but is broader than the requirements-only initial scope.
- Project-defined artifact types risk importing a generalized traceability model before the first workflows require one.

**Worth testing**

- valid or nearly valid Markdown as a fallback representation;
- colocated informative context and normative records;
- clear rationale and outgoing reference sections.

**Do not inherit without evidence**

- manual revision as part of stable identity;
- status as an intrinsic requirement field;
- heuristic record starts;
- generalized artifact-type trace chains.

### T-Reqs

**Documented approach**

The 2018 T-Reqs research reported industrial use of Markdown requirements in Git with Gerrit review. The paper emphasizes parallel work, branch-based experimentation, ordinary Git merge conflicts, and trace links maintained with conventions, templates, and helper scripts.

The current T-Reqs-NG project is MIT-licensed and publicly maintained. It embeds explicit XML-like elements in Markdown or other text files:

    <treqs-element type="requirement" id="...">
    The system shall authenticate an operator.
    <treqs-link type="addresses" target="..."/>
    </treqs-element>

Its command line can generate IDs, and a YAML Type and Traceability Information Model defines element types, allowed links, and constraints. Files may contain multiple elements, and the demonstration encourages organizing elements across files and subfolders.

**Mundane-req inference**

- The industrial report is strong evidence that Git, branches, and forge review can address real coordination problems rather than merely serving as storage.
- Explicit open and close markers avoid some ambiguity of heuristic Markdown patterns.
- XML-like tags are understandable without a tool but visually compete with short requirement statements.
- Opaque generated IDs favor stability but are awkward for conversation, review, and manual trace following.
- Scanning arbitrary text and source-code comments is outside mundane-req's current requirements-only scope.
- A per-project type-and-trace metamodel supplies flexibility at the cost of concepts the initial experiment may not need.
- The research context was large-scale agile development; it does not by itself establish adequacy for a formal systems-engineering traceability workflow.

**Worth testing**

- ordinary Git and forge workflows as the default review mechanism;
- explicit record boundaries embedded in readable text;
- multiple records per file without identity depending on position.

**Do not inherit without evidence**

- a generalized metamodel;
- arbitrary artifact scanning;
- opaque machine-only IDs as the sole human reference;
- XML tag syntax.

## Interchange and integration standards

### ReqIF

**Documented approach**

OMG ReqIF 1.2 is an open, non-proprietary XML interchange format intended to exchange requirements information between organizations and tools. It is not presented as a human authoring format.

ReqIF separates several important concepts:

- SpecObject instances are individually identifiable objects;
- typed attributes carry the actual requirement information;
- SpecRelations connect objects and may have their own attributes;
- Specifications contain ordered hierarchies of references to objects;
- the same object model is exchanged as an XML document.

**Mundane-req inference**

ReqIF supports the current hypothesis that requirement identity, relationship semantics, and specification placement are distinct. It also shows that relationship attributes and repeated placement may eventually matter.

Its XML serialization, pervasive type definitions, exchange metadata, and tool-oriented structure conflict with the initial source-first readability goal. Designing the source to resemble ReqIF would optimize for an adapter before proving authoring and Git behavior.

**Implication**

Future ReqIF round-tripping belongs on the roadmap as an adapter problem. The first experiment should retain explicit identity, fields, relations, and view ordering so a future mapping is plausible, but it should not attempt lossless ReqIF coverage now.

### SpecIF

**Documented approach**

SpecIF 1.1 is a JSON-based integration format whose model contains resources, statements, properties, classes, hierarchies, and files. Resources act as graph nodes, statements as typed graph edges, and hierarchy nodes as references that create views. A resource can appear in multiple hierarchies without duplication.

SpecIF datasets carry both class definitions and instances. Its scope includes requirements and broader product-lifecycle and systems-model information.

**Mundane-req inference**

The separation between resource, statement, and hierarchy is the clearest prior-art support for treating a requirement independently from document placement. It makes multiple views conceptually straightforward.

The self-describing metamodel, inheritance, generalized graph, revision keys, multilingual values, and broad systems-integration scope are intentionally more general than mundane-req's initial requirements-only investigation.

**Implication**

Test model/view separation, but do so with fixed experiment concepts. Do not introduce a generalized metamodel merely because an interchange format demonstrates one.

## Generic carrier formats

### Plain text is a property, not a source language

Human-readable plain text is a constraint on the authoritative artifact, not a commitment to Markdown or any other markup language. Several layers can remain distinct:

1. **Record syntax** identifies requirements, fields, relationships, and object boundaries.
2. **Field-content notation** expresses prose, mathematics, code, tables, or other content inside a field.
3. **Rendering** converts some source content into HTML, PDF, formatted mathematics, or another disposable view.

A purpose-built requirement record can therefore contain ordinary prose, a Markdown fragment, or a delimited mathematical expression without making Markdown or LaTeX the container language. Conversely, a file can be valid Markdown while still being a poor requirements record format.

**Experiment implication:** Candidates A and B will use the same purpose-built non-Markdown record language and the same non-Markdown view notation. Candidate C alone will test Markdown as the host document language.

### CommonMark and Markdown

CommonMark standardizes a deliberately small Markdown core. It provides familiar prose, headings, lists, links, code, and block structure, but it does not define requirements, attributes, typed relations, front matter, or a general directive mechanism.

**Opportunity:** Excellent standalone reading and ordinary editor support.

**Risk:** Any requirements semantics must be an explicit mundane-req extension. Clever use of headings, code spans, or list patterns can become heuristic and collide with ordinary prose. A file that is valid Markdown is not necessarily semantically portable between requirements tools.

**Experiment implication:** Retain one Markdown-hosted candidate because it is a credible and familiar alternative. Do not use Markdown implicitly for the other candidates or assume it is the default notation for prose fields and views.

### reStructuredText directives

reStructuredText is designed to remain readable as plaintext and to support domain extensions through directives. Directive markers, options, and indented bodies provide deterministic boundaries.

**Opportunity:** A mature example of composable domain-specific blocks inside prose.

**Risk:** More punctuation and indentation ceremony; unknown directives require a specialized processor; adopting reStructuredText would bring a larger host language than the experiment needs.

**Experiment implication:** Borrow the explicit block pattern as design evidence. Do not select reStructuredText merely because Sphinx-Needs uses it.

### YAML

YAML 1.2.2 represents mappings, sequences, and typed scalars. It is expressive and widely implemented.

**Opportunity:** Fields and lists are visually explicit; a single requirement can map naturally to a small record.

**Risk:** Plain scalars are context-sensitive, reserved punctuation matters, multiline prose adds indentation or scalar-style rules, and values resembling booleans or numbers may not remain strings unless the schema and quoting rules are understood. Full YAML also includes aliases, tags, multiple schemas, and other machinery irrelevant to the experiment.

**Experiment implication:** YAML remains useful prior art for visibly structured records, especially Doorstop. It is not part of the revised initial candidate slate; selecting it later would require a concrete advantage over the smaller purpose-built notation.

### TOML

TOML provides explicit key-value assignments, tables, arrays, and several quoted and multiline string forms.

**Opportunity:** Scalar types are comparatively visible and key-value metadata is approachable.

**Risk:** Normative prose becomes a quoted value, multiline delimiters compete with content, and arrays or repeated tables add configuration-file ceremony. Front matter using TOML is a convention outside CommonMark.

**Experiment implication:** TOML remains a possible later carrier comparison. It is not one of the first three full encodings.

### LaTeX-style mathematical notation

LaTeX is itself plain text and is widely used to author scientific documents and complex mathematical formulas. A mathematical requirement may be clearer and more precise when its source includes a conventional LaTeX-style expression than when the same relationship is approximated in prose.

That does not imply making a complete LaTeX document the requirement source. Full LaTeX includes document structure, macros, packages, references, and an execution environment. Web renderers such as MathJax implement a documented subset of TeX and LaTeX mathematics rather than the entire LaTeX language, so an unspecified promise of “LaTeX support” would be ambiguous.

**Opportunity:** Preserve a precise textual formula in the authoritative source and derive high-quality mathematical rendering separately.

**Risk:** Renderer-specific macro subsets, project-defined macros, package dependencies, and delimiter collisions can make a formula render differently across tools or become unintelligible without hidden configuration.

**Experiment implication:** Include at least one requirement with a nontrivial mathematical statement. Keep the exact LaTeX-style math fragment identical across candidates, delimit it explicitly, and record which small notation profile it assumes. Assess both the raw formula and an optional rendered view. Do not require a renderer for identifying the requirement, its normative role, identity, rationale, or relationships.

## Cross-cutting findings

### 1. File granularity is part of the representation

The projects do not merely choose different syntax. Doorstop makes one item equal one file, while StrictDoc, Sphinx-Needs, OpenFastTrace, and T-Reqs permit multiple objects in a document-like file.

That boundary affects:

- how many unrelated edits share a merge surface;
- whether adding or removing an object is a line edit or file operation;
- whether movement is a block edit or path change;
- how much context a reader sees in one editor buffer;
- whether folder layout acquires accidental semantics;
- how many files bulk edits and reviews touch;
- whether ordinary file history approximates object history.

It must therefore be measured rather than selected by intuition.

### 2. Git does not make file moves semantically free

Git diff can detect renames by content similarity, with a configurable threshold. Git log has separate behavior for following a single file beyond renames. A file move combined with substantial edits may therefore be shown differently from a pure rename.

For a multi-record file, moving one requirement to another file is normally a deletion and insertion rather than a record-level rename. Stable requirement identity must remain visible in source in either case.

### 3. Explicit identity should not depend on the filename

Filename-derived IDs make lookup simple but bind semantic identity to repository organization. Title-derived hashes make early authoring easy but bind identity to content.

For the experiment, every requirement will carry the same explicit human-usable ID inside its authoritative record. Filenames may repeat that ID for convenience, but changing a path or title will not change identity.

This is an experiment control, not yet a final language decision about machine identity versus display ID.

### 4. Model and view separation is valuable but not free

ReqIF and SpecIF show that independent objects can be placed in ordered hierarchies or views. This supports mundane-req's current conceptual preference.

The text-first projects also show the benefit of embedding requirements directly in coherent prose. Separate views can introduce reference lists, extra navigation, duplicated headings, and synchronization concerns.

The experiment must therefore compare the conceptual cleanliness of separate views against actual source-reading cost.

### 5. Plain text can carry multiple notations

Plain text can contain prose, a fielded requirements language, Markdown fragments, LaTeX-style mathematics, or another explicitly delimited notation. The container grammar and the content grammar do not have to be the same language.

This separation prevents a useful rendering notation from determining the requirement object model. It also creates a responsibility: embedded notation must have visible boundaries and a defined interpretation. Hidden renderer configuration or unrestricted macros would undermine durability.

### 6. Valid host markup helps, but semantic fallback has limits

A requirement embedded in valid Markdown remains renderable if its specialized tool disappears. That is valuable. However, a generic Markdown renderer does not know which paragraph is normative, whether a link is typed, or whether an ID is unique.

The durability criterion is that a human can understand the authoritative source without mundane-req, not that an unrelated renderer preserves all semantics automatically.

### 7. Extensibility repeatedly becomes metamodeling

StrictDoc custom grammars, Sphinx-Needs configuration, T-Reqs traceability models, ReqIF types, and SpecIF classes all demonstrate legitimate extensibility needs. They also demonstrate how quickly a representation can acquire configurable types, fields, relations, constraints, and rendering behavior.

The first experiment should use a fixed, documented set of fields and relation meanings. Extension strategy remains an open question until a concrete workflow requires it.

### 8. Revision and review state should not be copied into source by default

OpenFastTrace puts a manual revision in item identity. Doorstop writes review fingerprints. Sphinx-Needs and OpenFastTrace can store workflow status.

Each solves a real workflow. None proves that mundane-req should make revision numbers, opaque hashes, or approval status intrinsic source fields. Git commits, forge review, and future analyzers should be tested first.

### 9. Relationship semantics deserve visible source

Parent links, coverage links, dependency links, and generalized statements appear throughout the prior art. A single generic parent field is unlikely to explain formal decomposition and derivation adequately.

For the experiment, relationships will use a small fixed vocabulary and appear as outgoing references near the source requirement. Separate relationship identities and attributes remain deferred unless the rationale workflow demonstrates that they are necessary.

## File-granularity tradeoff matrix

| Boundary | Expected strength | Expected cost | Move or reorganization behavior to test |
| --- | --- | --- | --- |
| One requirement per file | Maximum edit and merge locality; easy file-level addition and deletion; path can aid lookup | High file count; fragmented context; more file operations; pressure to encode identity or hierarchy in paths | Pure rename, rename plus statement edit, move between subject folders, and file-history lookup |
| Multiple requirements per authored document | Coherent reading and colocated narrative; few files; familiar document editing | Unrelated objects share a file and conflict surface; document may appear to own object identity; reuse across views is awkward | Reorder within a file, move a record between files, and concurrent edits to different records |
| Multiple requirements per small module plus separate views | Middle-sized files; model organization can differ from presentation; fewer files than file-per-object | Additional view references; two places to navigate; module boundaries may become arbitrary | Move between modules without changing view, reorder only the view, and edit view context independently |

These are hypotheses to test, not predicted winners.

## Recommended experiment candidates

The same UAS corpus, relationship set, and synthetic history should be encoded in all three candidates. The syntax descriptions below are deliberately constrained sketches, not proposed language specifications.

### Candidate A — structured requirement modules

- Carrier: a small purpose-built, line-oriented plain-text language.
- Granularity: a few related requirements per module, with module boundaries based on subject area rather than document sections.
- Record shape: explicit start markers and fixed field labels for ID, statement, rationale, and outgoing relationships.
- View: a separate purpose-built plain-text view file supplies contextual prose and ordered requirement references.
- Embedded content: ordinary prose by default, with explicitly delimited notation such as a constrained LaTeX-style mathematical fragment where needed.
- Primary question: Does a medium-grained purpose-built source remain compact, deterministic, and understandable without inheriting a general markup language?
- Prior-art basis: StrictDoc's explicit records and the ReqIF/SpecIF distinction between objects and presentation.

The notation must stay smaller than the concepts exercised by the corpus. It will not have a configurable grammar or metamodel.

### Candidate B — the same language, one requirement per file

- Carrier: exactly the same purpose-built record language and field-content rules as Candidate A.
- Granularity: exactly one authoritative requirement record per file.
- Record shape: unchanged from Candidate A; only the object-to-file boundary changes.
- View: exactly the same purpose-built plain-text view notation as Candidate A.
- Filename rule: filenames repeat IDs for convenience but are not authoritative identity.
- Embedded content: identical to Candidate A.
- Primary question: Does file-level locality outweigh navigation, file-operation, and context fragmentation when syntax and view notation are held constant?
- Prior-art basis: Doorstop's file granularity, with its YAML, filename identity, and review fingerprints intentionally not inherited.

Candidate A versus Candidate B is the direct file-granularity comparison.

### Candidate C — document-embedded Markdown blocks

- Carrier: Markdown-compatible prose files.
- Granularity: several requirements in each authored specification document or major section file.
- Record shape: an unmistakable requirement block with explicit ID, statement, rationale, and outgoing relationships.
- View: file order and surrounding Markdown form the authored specification view.
- Embedded content: Markdown plus the same explicitly delimited LaTeX-style mathematical fragment used by the other candidates.
- Primary question: Is a familiar document host and direct prose context worth the larger merge surface, extended-Markdown semantics, and coupling between canonical storage and presentation?
- Prior-art basis: OpenFastTrace, Sphinx-Needs, StrictDoc, and T-Reqs.

This candidate deliberately challenges the model-oriented preference. It may show that a simple document-oriented source is adequate for the initial workflows, or expose why independent views and a purpose-built container are necessary.

## Controlled comparisons

The revised slate does not make Markdown a shared hidden assumption:

- **Candidate A versus Candidate B** holds record grammar, field-content notation, view notation, and corpus semantics constant. The intended variable is file granularity.
- **Candidate A versus Candidate C** compares a purpose-built model-oriented representation with a credible document-oriented host-markup alternative. This is a product-model comparison as well as a syntax comparison; observations must not pretend otherwise.

If Candidate A versus Candidate C leaves a specific syntax question unresolved, use a small focused crossover rather than adding a fourth full corpus automatically.

## Experiment controls derived from this study

All candidates will:

- carry explicit stable IDs inside requirement records;
- use the same human-facing IDs;
- encode the same normative statements, rationales, and relationships;
- contain the same explicitly delimited mathematical expression, variable definitions, units, and assumptions;
- use a fixed provisional relationship vocabulary;
- keep requirement-to-requirement relationships as outgoing references near the source object;
- omit approval status, review fingerprints, independent item revision numbers, and generated timestamps;
- avoid requiring a formatter or renderer for ordinary review, while permitting optional mathematical rendering;
- use the same baseline states and proposed change;
- record view-only changes separately from requirement-content changes where the candidate permits it;
- be inspected first with an ordinary editor, text search, and Git;
- avoid parser implementation unless manual inspection exposes a deterministic-parsing question that cannot otherwise be answered.

## Candidate rejection signals

In addition to the rubric in Research 0002, evidence from prior art suggests rejecting or revising a candidate if:

- identity changes when a file, title, or view position changes;
- a reader must infer record boundaries from ordinary prose patterns;
- a generic carrier introduces surprising scalar or whitespace behavior during routine edits;
- source contains opaque hashes or generated churn unrelated to engineering meaning;
- adding one durable field requires a configurable metamodel;
- understanding a requirement requires repeatedly opening so many files that source readability becomes nominal;
- moving a requirement produces a diff that obscures both the move and the content change;
- ordinary prose, identity, or relationships are only understandable after rendering;
- embedded notation depends on hidden macros, packages, or renderer state;
- the rendered formula cannot be reconciled easily with the canonical textual expression;
- view maintenance duplicates requirement content;
- requirements in the same file create conflicts despite independent edits often enough to outweigh coherent reading.

## Questions intentionally left open

1. What is the smallest useful non-Markdown view notation for Candidates A and B?
2. Should separate views reference only requirement IDs or support transclusion of authored fragments?
3. Does Candidate C's direct narrative make a separate view mechanism unnecessary for the initial scope?
4. Which exact LaTeX-style mathematical profile and delimiters are durable across likely renderers?
5. May a field declare other embedded content notations, or would that be premature extensibility?
6. Does rationale belong entirely on a requirement, or does decomposition rationale require relationship-local text?
7. Is one outgoing relationship vocabulary adequate for both refinement and self-derived requirements?
8. Are human-facing IDs sufficient stable identities, or will the split-and-replace workflow require a distinct immutable key?
9. How much surrounding context must travel with a requirement moved between modules?
10. Which minimum structures are necessary for credible future ReqIF round-tripping?

## Immediate next action

Draft the original 12–18 requirement UAS corpus once, independent of any candidate syntax. Define:

- requirement IDs, titles, statements, and rationales;
- at least one mathematical requirement with an identical LaTeX-style source fragment in every candidate;
- the fixed provisional relationship vocabulary;
- the authored specification ordering and context;
- Baseline A;
- the exact safety-driven change leading to Baseline B.

Then encode that frozen semantic content in Candidates A, B, and C. Representation-specific conveniences must not change the corpus semantics.

Initial result: [Research 0004](0004-uas-semantic-corpus.md) freezes the semantic corpus and change. [Research 0005](0005-purpose-built-record-syntax-sketches.md) selects keyword records with indented fields for the Candidate A/B experiment. [Research 0006](0006-non-markdown-view-notation.md) supplies a disposable flat view fixture. [Experiment 0001](../experiments/0001-source-representations/README.md) now encodes Baseline A in all three representation candidates so those choices can be evaluated through use.

## Sources consulted

### Text-first projects

- StrictDoc, [project repository](https://github.com/strictdoc-project/strictdoc).
- StrictDoc, [user guide](https://strictdoc.readthedocs.io/en/stable/stable/docs/strictdoc_01_user_guide.html).
- StrictDoc, [traceability guide](https://strictdoc.readthedocs.io/en/stable/stable/docs/strictdoc_01_user_guide-TRACE.html).
- Doorstop, [project repository](https://github.com/doorstop-dev/doorstop).
- Doorstop, [item documentation](https://doorstop.readthedocs.io/en/latest/reference/item.html).
- Doorstop, [document documentation](https://doorstop.readthedocs.io/en/latest/reference/document.html).
- Sphinx-Needs, [project repository](https://github.com/useblocks/sphinx-needs).
- Sphinx-Needs, [need directive](https://sphinx-needs.readthedocs.io/en/stable/directives/need.html).
- OpenFastTrace, [project repository](https://github.com/itsallcode/openfasttrace).
- OpenFastTrace, [user guide](https://github.com/itsallcode/openfasttrace/blob/main/doc/user_guide/user_guide.md).
- Knauss et al., [T-Reqs: Tool Support for Managing Requirements in Large-Scale Agile System Development](https://arxiv.org/abs/1805.02769).
- T-Reqs-NG, [project repository](https://gitlab.com/treqs-on-git/treqs-ng).
- T-Reqs-NG, [demonstration](https://gitlab.com/treqs-on-git/treqs-ng/-/raw/master/documentation/demo.md).

### Standards and carriers

- OMG, [Requirements Interchange Format 1.2](https://www.omg.org/spec/ReqIF/1.2/).
- SpecIF, [concepts](https://specif.de/Documentation/02_Concepts.html).
- SpecIF, [JSON schema](https://specif.de/Documentation/31_SpecIF_JSON-Schema.html).
- CommonMark, [specification](https://spec.commonmark.org/0.31.2/).
- Docutils, [reStructuredText markup specification](https://docutils.sourceforge.io/docs/ref/rst/restructuredtext.html).
- Docutils, [reStructuredText directives](https://docutils.sourceforge.io/docs/ref/rst/directives.html).
- YAML, [version 1.2.2 specification](https://yaml.org/spec/1.2.2/).
- TOML, [version 1.1.0 language specification](https://toml.io/en/v1.1.0).
- LaTeX Project, [LaTeX documentation and mathematical typesetting guidance](https://www.latex-project.org/help/documentation/).
- MathJax, [TeX and LaTeX input support](https://docs.mathjax.org/en/stable/input/tex/index.html).
- Git, [git-diff documentation](https://git-scm.com/docs/git-diff).
- Git, [git-log documentation](https://git-scm.com/docs/git-log).
