# Specification 0002: Minimum Source Language and Model

Status: Provisional; deterministic interpretation, sustained use, and transferability confirmed by Experiments 0002 through 0004

Provisional contract: [mundanereq source 0.1](0003-provisional-0.1-contract.md)

Normative language standard: [mundanereq Source Language Specification 0.1](0004-mundanereq-source-language-0.1.md)

## Purpose

This specification records the design and rationale for the smallest source language and semantic model justified by Experiment 0001. The resulting normative language rules are consolidated in Specification 0004. Neither document is a production language release or a stability promise beyond the provisional 0.1 contract.

The design has three priorities:

1. a requirement remains understandable as standalone plain text;
2. ordinary Git diffs expose meaningful changes with little structural noise;
3. independent tools can interpret the same source deterministically.

The evidence and candidate disposition are recorded in [Research 0007](../research/0007-provisional-source-representation-decision.md).

[Experiment 0002](../experiments/0002-deterministic-interpretation/README.md) implements these rules in a dependency-free GraalVM native Java probe. Its module and one-record-per-file fixtures produce identical semantic inventories, and its focused invalid fixtures produce source-positioned diagnostics. This confirms deterministic interpretation for the tested corpus; it does not establish a compatibility-stable language version.

[Experiment 0003](../experiments/0003-sustained-authoring/README.md) exercises addition, splitting, relationship retargeting, coordinated normative change, reallocation, file movement, retirement, and formal model-pressure workflows. All committed states remain valid without grammar changes. The result supports this minimum model for continued experiment while identifying verification planning and identity continuity across ID correction as unresolved questions.

[Experiment 0004](../experiments/0004-transferability/README.md) transfers 19 requirements from NASA's independently structured FRET Lift-Plus-Cruise case study. The records validate without grammar changes. A precise upstream commit/path/record locator fits in the opaque `source` scalar, while a separate baseline-bound verification plan demonstrates why activity identity, coverage, execution, and results should not be collapsed into requirement fields.

## Status of decisions

This draft distinguishes four kinds of statement:

- **Selected for experiment:** the next parser and validator should implement and test it.
- **Model decision:** it expresses current project semantics independently of grammar details.
- **Hypothesis:** the experiment should test whether it remains usable.
- **Deferred:** it is deliberately outside this version.

Nothing in this draft requires a production implementation architecture.

## Source set and files

### Source-set selection

**Selected for experiment:** A tool receives an explicit set of file and directory inputs. For each directory input, it recursively includes regular files whose names end in `.mreq`. It does not follow symbolic links and ignores `.git` directories. Supplying the repository root therefore provides the ordinary repository-wide validation operation.

This rule avoids a manifest, configuration language, or mandatory `requirements/` directory. A tool must report the files it selected when requested so that an invocation is inspectable.

The selection mechanism is not part of requirement identity or the semantic model. A future repository profile may constrain source roots without changing the language.

### File granularity

**Model decision:** A source file contains one or more complete requirement records. File boundaries do not create modules, namespaces, hierarchy, ownership, allocation, or authored order.

Small subject files and one-requirement-per-file layouts are both valid. A repository may adopt a consistent local convention. Moving an unchanged record between files does not change the requirement.

Experiment 0003 confirms this rule directly: moving SYS-008 from a larger subject module into its own timing module preserved the normalized semantic inventory exactly.

### Text encoding

**Selected for experiment:** Source is UTF-8 text without a byte-order mark. LF and CRLF line endings have the same interpretation. NUL bytes and tabs are invalid. A final line ending is required.

Tabs are prohibited because indentation is structural and their visual width is editor-dependent. This restriction can be revisited if use demonstrates a need.

## Requirement model

A source set denotes a collection of requirement objects and directed decomposition relationships.

### Requirement identity

**Model decision:** The ID in the `requirement` opener is the requirement's sole identity in this version. It is human-facing, case-sensitive, unique across the source set, and independent of title, filename, path, file position, and line number.

Changing the ID denotes replacement of one identity with another unless later tooling or explicit change records provide additional evidence. A separate hidden or machine identity is deferred.

Experiment 0004 confirms this behavior across an adjacent consumer: correcting `LPC_KIAS_0` to `LPC_KIAS_NONNEGATIVE` and updating its verification-plan reference is clear in an atomic Git diff, but semantic snapshot comparison still sees removal and addition. The unchanged upstream source locator supplies provenance, not an identity-continuity assertion.

For this experiment an ID:

- begins with an ASCII letter or digit;
- continues with zero or more ASCII letters, digits, hyphens, underscores, or periods;
- contains no whitespace.

Punctuation carries no namespace or hierarchy meaning.

### Fields

| Field | Cardinality | Semantic value |
| --- | --- | --- |
| ID | exactly one | Requirement identity from the opener |
| `title` | exactly one | Nonempty human-facing name; not identity |
| `allocation` | zero or one | Nonempty label naming the allocated party; not a modeled object |
| `statement` | exactly one | Nonempty normative content |
| `rationale` | zero or one | Nonempty durable reasoning for the requirement's existence or form |
| `source` | zero or one | Nonempty opaque reference to an origin outside this requirements model |
| `decomposes` | zero or more | Outgoing references to higher-level requirement IDs |

Neither `source` nor `decomposes` is universally required. Traceability completeness is an engineering-policy question rather than a condition for parsing a requirement. A validator may later support a separately identified policy check.

### Decomposition relationship

**Model decision:** Each `decomposes: PARENT-ID` line creates a directed relationship from the containing requirement to the referenced requirement. It means that the source requirement participates in making the target requirement more specific at a lower level.

The relationship does not claim that the source requirement independently and completely satisfies its target. Repeating the same target within one record is invalid. The order of decomposition lines carries no semantic meaning.

The minimum validator checks that every target exists in the selected source set. Cycle analysis and completeness claims are deferred pending a concrete workflow.

### External source

**Selected for experiment:** `source` is an opaque scalar. This language does not parse document revisions, fragment locators, URIs, or supplier identifiers from it.

Keeping it distinct from `decomposes` preserves the difference between origins outside the requirement model and relationships among managed requirements.

Experiment 0004 demonstrates that an opaque value can preserve a precise Git commit, file path, and upstream record ID without language-defined locator structure. The repetition is visible but does not yet justify aliases, resolver configuration, or a universal provenance schema.

### Allocation

**Hypothesis:** `allocation` remains a plain label. Its presence is optional so that an otherwise valid requirement can exist before allocation. Modeling components or allocations as relationship objects is deferred.

Experiment 0003 found that a reallocation is a clear one-line change, but also confirmed that a label cannot provide referential component identity, controlled vocabulary, or rename semantics. The hypothesis remains unchanged pending another corpus.

Experiment 0004 maps FRET's single `vehicle` component label directly and readably, but the one-value vocabulary provides no evidence about heterogeneous target types or rename behavior. A repository policy may constrain allowed labels without changing the core model.

### Revisions and baselines

**Model decision:** A requirement revision is the state of a requirement object in a repository snapshot. It has no source field or independently incremented revision number.

A baseline is initially a repository snapshot identified by an ordinary Git commit, usually given a durable name and scope through an annotated tag or project convention. Approval, authority, and certification meaning are not inferred from the tag merely existing.

Experiment 0003 confirms that addition, editing, movement, and deletion are recoverable between annotated baselines without intrinsic revision, status, or retirement fields. Required change justification and baseline authority remain review or project-policy concerns.

### Verification planning and evidence

**Model decision:** Planned verification is not an intrinsic scalar property of a timeless requirement object. Planning coverage relates a particular requirement revision, usually through a declared baseline, to a separately identified verification activity. An execution of that activity may later produce configuration, evidence, and a result; planning coverage, execution, and passing are distinct facts.

Experiment 0004 represents five planned activities and 19-of-19 planning coverage in a separate baseline-bound text artifact while recording zero executions. Repeating activity definitions or pass/fail state in requirement records would duplicate information and bind revision-specific workflow state to requirement identity.

**Deferred:** The source syntax and semantic model for verification activities, coverage links, executions, and evidence. The experiment establishes conceptual ownership, not a second language.

## Record syntax

The following record is representative:

    requirement SYS-007
    title: Loss-of-link response
    allocation: Mission-control coordinator
    statement:
      Within 100 ms after the command link is declared unavailable, the mission-control system
      shall cause the ground-control adapter to begin the first transmission attempt of a
      safe-recovery command for the active vehicle.
    rationale:
      Prompt initiation of safe recovery limits continued operation without a usable command link.
    source: SRC-SAFETY-001
    decomposes: OPS-001
    decomposes: OPS-004
    end requirement

### Record boundaries

**Selected for experiment:** A record begins with `requirement ` followed by its ID and ends with `end requirement`. Both markers begin at column zero. Records are separated by one or more blank lines. Nonblank content outside a record is invalid.

The record opener, field labels, and closer are lowercase and case-sensitive.

### Field order

**Selected for experiment:** Fields occur in this fixed order:

1. `title`;
2. optional `allocation`;
3. `statement`;
4. optional `rationale`;
5. optional `source`;
6. zero or more `decomposes` lines.

Fixed order keeps records visually consistent and avoids needing a generalized field map. A missing required field, duplicate singleton field, unknown field, or out-of-order field is invalid.

### Scalar fields

`title`, `allocation`, `source`, and `decomposes` use this shape at column zero:

    field-name: nonempty value

Exactly one ASCII space follows the colon. Leading or trailing whitespace is not part of the value and is invalid. Values are not quoted. Colons and internal runs of spaces are ordinary value characters.

`decomposes` values use the ID syntax. Other scalar fields contain any non-control Unicode characters allowed by UTF-8.

### Prose fields

`statement:` and `rationale:` occur alone at column zero. Their body lines are either blank or begin with exactly two structural spaces. At least one nonblank body line is required.

After removing the two structural spaces:

- consecutive nonblank prose lines form one paragraph and are joined with one semantic space;
- a blank line separates paragraphs;
- manual line wrapping therefore does not change prose meaning.

Additional leading spaces after the structural indentation are preserved as content. A column-zero structural line ends the body; a field-looking line that remains indented is prose.

This folding rule remains a **hypothesis** beyond the current corpus. Experiments 0002 and 0003 found deterministic interpretation and no misleading semantic change during manual edits, but broader authoring evidence is still needed.

## Mathematical content

Mathematics is field content, not record structure. The initial experiment recognizes an explicitly labeled block inside `statement`:

    statement:
      The system shall determine the boundary from:

      math latex
        t_{detect} = t_0 + k T_{eval}
      end math

After removing the statement's two-space indentation, `math latex` and `end math` delimit the block. Every nonblank payload line has two additional structural spaces; a blank payload line may be physically empty. The interpreter removes those two spaces from nonblank lines and preserves all remaining payload characters and line breaks. An empty payload is invalid.

**Selected for experiment:** The semantic model records this as a math content block labeled `latex` with an opaque payload. It does not interpret or validate LaTeX commands.

**Deferred:** A promised LaTeX subset, macro policy, renderer behavior, mathematical equivalence, and math blocks in fields other than `statement`.

## Semantic interpretation

An interpreter produces, at minimum:

- each requirement's ID and source location;
- scalar field values;
- statement and rationale content as paragraphs, with opaque math blocks where present;
- outgoing decomposition targets;
- a source-set index from ID to requirement.

Source order may be retained for diagnostics and lossless tooling, but it has no requirements semantics. The semantic model does not infer document order from file traversal order.

Two records with the same ID are invalid even when their contents are byte-identical. The language has no partial-record, include, inheritance, override, or merge mechanism.

## Required diagnostics for the experiment

The first parser and validator must reject and locate at least:

- invalid UTF-8, byte-order marks, NUL bytes, tabs, or a missing final line ending;
- content outside a requirement record;
- malformed or nested record boundaries;
- invalid IDs;
- missing, duplicate, unknown, or out-of-order fields;
- empty scalar or prose fields;
- invalid prose indentation;
- malformed or empty math blocks;
- duplicate requirement IDs across files;
- duplicate `decomposes` targets within a record;
- dangling `decomposes` targets.

Diagnostics should name the file, line, and column; identify the violated rule in plain language; and avoid depending on a renderer or database.

The first validator does not need to judge requirement prose quality, decomposition completeness, allocation validity, external-source resolvability, approval, or verification coverage.

## Views and authored order

**Deferred:** This language version does not define a view, collection, section, include, or document-composition syntax.

Experiment 0001 showed that requirement storage should remain independent of authored order; it did not show that the `.mview` fixture belongs in the minimum language. A renderer may initially sort by ID or accept an experiment-specific list without making that list part of this specification.

This omission is deliberate. It can be revisited when a concrete workflow requires authored composition rather than merely a convenient demonstration document.

## Explicitly deferred capabilities

- arbitrary attributes or user-defined schemas;
- status, approval, signatures, and review workflow;
- per-requirement revision counters;
- relationship objects with their own identity or metadata;
- comments, imports, includes, namespaces, and packages;
- Markdown as the containing authoritative record format;
- formatting and canonical rewrite rules;
- semantic diff or merge;
- generated documents and reports;
- ReqIF import, export, and round-trip preservation;
- access control, variants, reuse, and cross-repository references.

Experiment 0006 demonstrates a schema-valid bounded ReqIF 1.2 semantic self-roundtrip without source-language changes. General and cross-tool ReqIF interoperability remains outside this specification. The source language should not imitate ReqIF's storage model merely because a derived adapter can map to it.

## Open questions after initial interpretation and use

1. What minimum source model should identify verification activities, coverage links, executions, and evidence without coupling them to requirement storage?
2. Is Git history sufficient for rare human-facing ID corrections, or does cross-baseline and interchange continuity justify an explicit continuity record or separate identity concept?
3. When do allocation labels need referential identity or a controlled vocabulary?
4. How should project vocabulary and formal symbol definitions remain independently readable without turning the requirement language into an executable expression framework?
5. Do prose folding, fixed field order, and source discovery remain comfortable at larger scale and with independent authors?
6. Which trace-completeness rules are common enough for reusable policy analysis without becoming universal language validity rules?

The next experiment should add one focused, evidence-driven query over the existing model rather than expanding the grammar speculatively. ID continuity should receive another focused test before ReqIF round-trip work.
