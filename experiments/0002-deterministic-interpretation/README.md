# Experiment 0002: Deterministic Interpretation

Status: Planned; implementation requires explicit agreement

## Question

Can the minimum source language in [Specification 0002](../../specification/0002-minimum-source-language-and-model.md) be parsed and validated reproducibly by a small independent tool without weakening its standalone readability?

## Why this experiment is next

Experiment 0001 established that the selected record form works plausibly as source and that module and one-record-per-file layouts can share one model. Inspection alone cannot establish that field boundaries, prose folding, math payloads, source-set selection, identity, and relationships have deterministic interpretations.

The next uncertainty is therefore language behavior, not rendering, views, interchange, or production architecture.

## Inputs

- Candidate A's Baseline B module files.
- Candidate B's byte-identical Baseline B records stored one per file.
- The rules and field cardinalities in Specification 0002.
- Small invalid fixtures, each designed to violate one named rule.

Candidate C and `.mview` files are outside the parser input because the provisional decision did not select either as part of the minimum source language.

## Smallest useful implementation

If authorized, build one disposable parsing and validation probe that:

1. accepts explicit file or directory inputs and selects `.mreq` files by the specified rule;
2. parses complete requirement records and their fixed fields;
3. folds prose and preserves opaque `math latex` payloads as specified;
4. builds an in-memory ID index and outgoing decomposition references;
5. validates the minimum syntax and source-set rules;
6. emits plain, source-positioned diagnostics;
7. can emit a disposable normalized inventory for test comparison.

It should not include a database, server, plugin system, renderer, view parser, formatter, semantic merge, ReqIF support, or production compiler framework.

The implementation language is deliberately not selected by this plan. That choice should be based on the smallest clear probe and should not become a product-language commitment.

## Positive tests

### Equivalent layouts

Parse Candidate A and Candidate B independently. After excluding source locations and record traversal order, both must produce the same:

- 20 requirement IDs;
- scalar values;
- semantic prose paragraphs;
- mathematical payload;
- 22 decomposition relationships.

This directly tests the decision that file boundaries are non-semantic.

### Relevant source behavior

The positive fixtures must also demonstrate:

- a source file containing several records;
- a source file containing one record;
- an optional `source` omission;
- zero, one, and several `decomposes` relationships;
- a self-derived requirement with a source and no parent;
- manually wrapped prose with unchanged semantic paragraph content;
- an opaque multiline `math latex` block whose remaining bytes and line breaks are preserved.

## Negative tests

Use one small fixture per failure where practical. Cover at least:

- duplicate IDs across two files;
- a dangling decomposition target;
- a repeated decomposition target;
- a missing required field;
- a duplicate singleton field;
- an unknown or out-of-order field;
- an invalid ID;
- malformed record boundaries;
- nonblank content outside a record;
- invalid body or math indentation;
- an unterminated or empty math block;
- a tab, byte-order mark, NUL byte, invalid UTF-8, and missing final line ending.

Each diagnostic must identify the file and source position and say what rule failed without requiring specialized UI.

## Observations to record

- Whether the two file layouts yield the same semantic inventory.
- Whether any valid fixture admits more than one plausible interpretation.
- Whether fixed field order reduces or creates authoring mistakes.
- Whether prose folding is understandable when inspecting both source and normalized output.
- Whether math de-indentation preserves the intended payload exactly.
- Whether source discovery selects surprising files.
- Whether diagnostics are sufficient to repair invalid source in an ordinary editor.
- Which specification rules needed clarification during implementation.

## Success criteria

The experiment succeeds if:

1. Candidate A and Candidate B produce equal semantic content despite different file boundaries;
2. every required negative case is rejected at a useful location;
3. valid input needs no hidden state, repository database, or renderer to interpret;
4. the implementation remains small enough that language behavior, rather than framework design, dominates the work;
5. the resulting evidence identifies whether the draft should be confirmed or narrowly revised.

## Stop conditions

Stop and revise the specification rather than adding general machinery if the probe appears to require:

- configurable grammars or schemas;
- recovery rules that make malformed input silently ambiguous;
- path-derived semantics;
- a document or view model;
- LaTeX parsing;
- a persistent database;
- special treatment for a single file-granularity convention.

## Deliverables

Once implementation is authorized:

- the smallest parser and validator needed for the tests;
- positive and negative fixtures;
- automated checks of expected interpretation and diagnostics;
- a short result record describing confirmed rules, revisions, and remaining questions.

No production architecture decision should be inferred merely from the probe's implementation language or directory structure.
