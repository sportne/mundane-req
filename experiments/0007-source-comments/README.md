# Experiment 0007: Nonsemantic Source Comments

Status: Completed

Result date: 2026-08-22

## Question

Can the language support useful author comments without changing requirement
semantics, making `#` ambiguous inside requirement content, or adding a
durable remark field?

## Input decision

[Research 0009](../../research/0009-nonsemantic-source-comments.md) selects a
column-one, full-line `#` comment at structural boundaries. The comment is
source text only and has no semantic attachment.

## Experiment

Extend the reference parser and conformance material to exercise:

1. file-header and file-trailer comments;
2. comments after an opener and between fields;
3. a comment as the only separator between records;
4. comments between repeated decomposition fields;
5. `#` retained as ordinary scalar, prose, rationale, and math content;
6. rejection when a comment replaces, interrupts, or occurs within a required
   prose or math body;
7. semantic equality between the commented 0.2 conformance fixture and its
   comment-free 0.1 counterpart;
8. unchanged derived ReqIF behavior.

## Implementation

The Experiment 0002 Java probe recognizes a comment only when the first source
character is `#`. It skips comments while parsing file-level trivia and the
defined boundaries between record fields. The body parser does not skip
comments, so a comment cannot silently divide prose or a math payload.

No comment is stored in `Requirement`, `ContentBlock`, relationship, or
normalized-inventory values. The parser retains original line numbers for
diagnostics because it skips comment lines without preprocessing or rewriting
the input.

No database, comment attachment model, inline lexer, block-comment recovery,
or ReqIF mapping is introduced.

## Results

The dependency-free parser test harness passes 13 grouped tests, including the
new source-comment group. It confirms all eight experiment cases.

The 0.2 valid conformance fixture contains comments at every permitted kind of
structural location and produces an inventory byte-identical to the 0.1 valid
fixture. Four invalid fixtures demonstrate that comments cannot replace or
interrupt required prose and math content.

The bounded ReqIF harness continues to pass all five grouped tests. Comments
do not enter the semantic model and therefore require no ReqIF profile change.

Both JVM tests and the GraalVM native build succeed.

## Findings

1. Full-line comments add no requirement-model member.
2. Column-one recognition leaves `#` unambiguous in scalars and indented
   content.
3. Restricting comments to structural boundaries avoids paragraph-folding and
   math-payload ambiguities.
4. A comment line naturally provides visible separation between records.
5. Semantic inventory equality is a direct, implementation-independent test
   of nonsemantic behavior.
6. ReqIF and incoming-trace behavior remain unchanged because both consume the
   semantic model rather than source trivia.

## Limitations

- Comment preservation by future formatters or lossless editors is not tested.
- There is no syntax for inline or multiline comments.
- Comments cannot be queried or rendered through the semantic model.
- Moving a record and its nearby comments remains an ordinary source edit.
- The experiment does not define tool directives or structured annotations.

## Disposition

The experiment succeeds. Publish `mundanereq-source-0.2` with nonsemantic
source comments as its only language addition. Retain the 0.1 standard and
fixtures unchanged as the prior contract.
