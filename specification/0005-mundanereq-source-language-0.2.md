# mundanereq Source Language Specification

Version: 0.2

Contract identifier: `mundanereq-source-0.2`

Status: Provisional language standard

Language-behavior baseline: annotated Git tag `provisional-0.2`

Document relation to baseline: successor contract adding nonsemantic source comments

## Foreword

This document is the normative specification of the mundanereq source
language identified by `mundanereq-source-0.2`.

This document succeeds version 0.1 and adds one facility: nonsemantic,
full-line author comments. It otherwise retains the language and semantic
model formalized by Specification 0004 and originally developed in
[Specification 0002](0002-minimum-source-language-and-model.md).
Specification 0002 remains the design record and rationale; this document
states the complete resulting rules in a form intended to be implemented
independently.

The contract remains provisional. Provisional status concerns compatibility
and maturity, not the force of the requirements in this document when an
implementation claims conformance to version 0.2.

Paragraphs marked **Note** or **Example** are informative. All other text is
normative unless explicitly identified otherwise.

## 1. Scope [scope]

### 1.1 General [scope.general]

This document specifies:

1. the physical representation of a mundanereq source file;
2. the selection of a source set from explicit file and directory inputs;
3. the lexical and syntactic form of requirement records;
4. the syntax and placement of nonsemantic source comments;
5. the semantic model denoted by a conforming source set;
6. source-file and source-set validity constraints;
7. minimum diagnostic and implementation-conformance requirements.

This document specifies requirements source. It does not specify a renderer,
editor, database, repository layout, review system, verification language,
interchange format, or requirements-management process.

### 1.2 Normative language [scope.normative]

In this document:

- **shall** expresses a requirement for conformance;
- **shall not** expresses a prohibition;
- **should** expresses a recommendation;
- **may** expresses permission;
- **need not** expresses absence of a requirement.

### 1.3 No normative external references [scope.references]

This version has no normative external references. Terms such as UTF-8 and
Unicode scalar value are defined sufficiently for this language by this
document; conformance does not depend on a particular programming language,
parser library, operating system, Git implementation, or ReqIF implementation.

## 2. Conformance [conformance]

### 2.1 Conforming source file [conformance.file]

A source file conforms to this specification if its physical representation
conforms to Clause 6 and its contents consist of one or more requirement
records conforming to Clauses 8 through 10.

### 2.2 Conforming source set [conformance.set]

A source set conforms to this specification if:

1. it is a nonempty finite set of conforming source files;
2. every requirement identifier is unique within the complete set;
3. every decomposition target identifies a requirement in the complete set;
4. every other source-set constraint in Clause 11 is satisfied.

Conformance does not assert that the requirements are correct, complete,
approved, allocated to valid components, adequately decomposed, traceably
sourced, or covered by verification.

### 2.3 Conforming interpreter [conformance.interpreter]

A conforming interpreter shall provide a strict 0.2 mode that:

1. accepts every conforming 0.2 source set;
2. rejects every nonconforming source set;
3. produces the semantic values specified by Clauses 9 and 10;
4. produces semantic values independently of source-file traversal order;
5. treats LF and CRLF source files equivalently as specified in Clause 6;
6. diagnoses rejected input as specified in Clause 12;
7. does not require repository state, a database, a renderer, or hidden
   configuration to interpret an explicitly supplied source set.

An implementation may provide extensions or permissive modes. It shall not
accept an extension as conforming `mundanereq-source-0.2` input in its strict
0.2 mode.

### 2.4 Source-selection conformance [conformance.selection]

A tool that claims 0.2 source-selection conformance shall implement Clause 7.
An interpreter embedded in another system may instead receive an already
selected abstract source set. Such an interpreter need not expose filesystem
discovery, but the supplied files shall still be interpreted as one complete
source set.

## 3. Terms and definitions [terms]

For this document, the following terms apply.

### 3.1 source file

A named finite sequence of bytes interpreted as one mundanereq source file.
The name is retained for diagnostics but has no requirements semantics.

### 3.2 source set

The complete nonempty set of source files interpreted together. Identity and
referential-integrity constraints apply across this boundary.

### 3.3 physical line

A sequence of decoded Unicode scalar values terminated by a line ending. The
line ending is not part of the line's content.

### 3.4 blank line

A physical line containing zero characters. A line containing spaces is not a
blank physical line, even where structural de-indentation later produces an
empty semantic body line.

### 3.5 requirement

An independently identified model object denoted by one complete requirement
record.

### 3.6 requirement identifier

The human-facing, case-sensitive identity written in a requirement opener.

### 3.7 scalar field

A field whose value occurs on the same physical line as its field name.

### 3.8 prose field

A field whose value is formed from a structurally indented sequence of body
lines.

### 3.9 content block

Either a prose paragraph or an opaque mathematical payload. Content-block
order is semantic.

### 3.10 decomposition relationship

A directed relationship from the requirement containing a `decomposes` field
to the requirement named by that field.

### 3.11 requirement revision

The semantic state of a requirement in a particular source-set snapshot. A
revision is not a separately numbered source-language object.

### 3.12 diagnostic

Information reporting why source could not be accepted as conforming input.

### 3.13 source comment

A complete physical line beginning with `#` at column 1 whose contents are
retained in source but do not contribute to the semantic model.

## 4. Specification notation [notation]

### 4.1 Grammar notation [notation.grammar]

Grammar productions use the following notation:

```text
name          ::= production
"characters"  exact characters
X?            zero or one X
X*            zero or more X
X+            one or more X
X | Y         X or Y
```

`SP` denotes U+0020 SPACE. `LF` denotes U+000A. `CR` denotes U+000D.
`EOL` denotes a logical line ending after the normalization specified in
Clause 6. Square-bracketed clause labels are stable document references and
are not source-language notation.

Where a production is followed by a semantic restriction in prose, both the
production and the restriction apply. The prose rule takes precedence if the
compact grammar cannot express the restriction by itself.

### 4.2 Character comparison [notation.comparison]

Unless this document states otherwise, characters and strings are compared by
exact Unicode scalar-value sequence. Comparison is case-sensitive and performs
no case folding, locale transformation, or Unicode normalization.

## 5. Abstract semantic model [model]

### 5.1 Source-set value [model.set]

A source set denotes:

1. a finite unordered collection of requirements; and
2. a finite set of directed decomposition relationships among those
   requirements.

Source-file order, directory traversal order, record order, filenames,
directories, and line numbers are not members of this semantic value.

Source comments are concrete source text and are not members of this semantic
value. They have no attachment to a requirement, field, or relationship.

### 5.2 Requirement value [model.requirement]

A requirement has the following semantic members:

| Member | Cardinality | Value |
| --- | --- | --- |
| identifier | exactly one | requirement identifier |
| title | exactly one | scalar string |
| allocation | zero or one | scalar string |
| statement | exactly one | nonempty ordered sequence of content blocks |
| rationale | zero or one | nonempty ordered sequence of prose paragraphs |
| source | zero or one | scalar string |
| decomposition targets | zero or more | unordered set of requirement identifiers |

An implementation may retain source locations, original line wrapping, field
order, and other concrete-syntax information for diagnostics or lossless
editing. Such information is not part of the 0.2 requirement value.

### 5.3 Identity [model.identity]

The identifier in the requirement opener is the sole identity of a
requirement in version 0.2. Identity does not derive from any other field or
from the requirement's physical location.

Changing the identifier denotes removal of the old requirement and creation
of a different requirement when comparing semantic snapshots. Git history or
an external change record may provide human evidence of continuity, but this
language does not represent that evidence as identity.

### 5.4 Scalar semantics [model.scalar]

The semantic value of a scalar field is the exact sequence of characters
following its required prefix. Quotes, backslashes, colons, number signs,
Markdown punctuation, and LaTeX punctuation have no special scalar meaning.
The language defines no escape processing or entity expansion.

`allocation` is an opaque label. It does not create or reference a modeled
component.

`source` is an opaque external-origin reference. The language does not parse
it as a URI, Git locator, document reference, or supplier identifier.

### 5.5 Decomposition semantics [model.decomposition]

For each `decomposes: T` field in a requirement `R`, the semantic model
contains one directed relationship `R -> T`.

The relationship means that `R` participates in making `T` more specific at a
lower level. It does not assert that `R` alone completely satisfies `T`.

The textual order of decomposition fields is not semantic. Cycles, including
self-reference, are not prohibited by the 0.2 language. A project policy may
reject them separately.

### 5.6 Content semantics [model.content]

A prose content block contains one nonempty semantic paragraph string. A math
content block contains the language label `latex` and an opaque payload
string. Math payload characters are not parsed, validated, normalized, or
assigned mathematical meaning by this language after the transformations
specified in Clause 10.3.

## 6. Physical source representation [physical]

### 6.1 Encoding [physical.encoding]

A source file shall be well-formed UTF-8. Each decoded character shall be a
Unicode scalar value. Overlong encodings, encoded surrogate code points,
truncated sequences, and otherwise malformed UTF-8 are invalid.

The initial three-byte sequence `EF BB BF`, commonly used as a UTF-8
byte-order mark, shall not occur at the beginning of a source file. U+FEFF
occurring elsewhere is an ordinary source character.

### 6.2 Line endings [physical.lines]

Each physical line shall end with either LF or CRLF. A CR not immediately
followed by LF is invalid. LF and CRLF have identical language meaning. Before
syntax and semantic interpretation, each CRLF shall be normalized to one LF.

Every source file shall end with a line ending. Consequently, the final
physical line is always terminated.

### 6.3 Prohibited characters [physical.characters]

The following characters shall not occur in decoded source:

- U+0000 through U+0008;
- U+0009 CHARACTER TABULATION;
- U+000B and U+000C;
- U+000E through U+001F;
- U+007F through U+009F.

U+000A and U+000D may occur only as line endings conforming to Clause 6.2.

**Note:** The prohibition is stated as an exact set rather than by reference
to a language runtime's notion of a control character.

### 6.4 Source coordinates [physical.coordinates]

Lines and columns are one-based. Line 1 is the first physical line. Column 1
is the first Unicode scalar value of a line. Each subsequent Unicode scalar
value advances the column by one. A diagnostic for an end-of-line or
end-of-file condition may designate the position immediately following the
last character.

Coordinates refer to the physical source after CRLF-to-LF equivalence but
before structural indentation is removed.

When malformed UTF-8 prevents decoding a complete physical line, the line and
column of the decoding diagnostic shall be computed from the raw bytes before
the malformed sequence: LF bytes advance the line and reset the column, and
each other preceding byte advances the column. This exception exists only to
give a reproducible location for text that has no decoded character sequence.

## 7. Source-set selection [selection]

### 7.1 Explicit inputs [selection.inputs]

The default selection operation receives one or more explicit filesystem
inputs. Each input is treated as follows:

1. An explicit regular file is selected regardless of its filename extension.
2. An explicit directory is traversed recursively as specified in Clause 7.2.
3. A symbolic link is not followed and is not selected.
4. An unavailable input produces an input diagnostic.
5. An input that is neither a regular file nor a directory contributes no
   source file.

Selection is based only on the supplied inputs. The operation shall not read a
manifest, repository configuration, index, database, or environment-specific
requirements root unless a separate non-0.2 mode requests that behavior.

### 7.2 Directory traversal [selection.directories]

When traversing an explicit directory, the selector shall recursively select
each regular file whose filename ends exactly with `.mreq`. The suffix match
is case-sensitive. It shall not follow symbolic links.

A descendant directory whose filename is exactly `.git` shall not be
traversed. If a `.git` directory is itself supplied as an explicit input, it
is the traversal root and is processed as an explicit directory; `.git`
descendants below it remain excluded.

Traversal order shall not affect the selected source set or its semantic
interpretation.

### 7.3 Duplicate paths and empty selection [selection.result]

The same normalized filesystem path selected through more than one input
shall contribute one source file. Filesystem path normalization and path
identity are properties of the host environment and have no requirements
semantics.

If selection produces no source files, the operation shall fail with a
diagnostic. An empty collection is not a conforming source set.

### 7.4 File granularity [selection.granularity]

Each selected file shall contain one or more complete requirement records.
One file may contain one record or multiple records. A record shall not span
files. File boundaries do not create modules, namespaces, hierarchy,
allocation, ownership, or authored order.

## 8. Lexical and syntactic structure [syntax]

### 8.1 General [syntax.general]

The language is line-oriented. Structural keywords, field names, punctuation,
and spaces shall appear exactly as specified. Keywords and field names are
lowercase and case-sensitive.

Except for source comment lines conforming to Clause 8.1.1, nonblank text
outside a requirement record is invalid.

#### 8.1.1 Source comments [syntax.comment]

```text
comment-line ::= "#" comment-text EOL
```

`comment-text` is any sequence, including the empty sequence, of source
characters permitted on one physical line. The initial `#` shall occur at
column 1. No space is required after it.

A comment line may occur:

1. before the first requirement record or after the last record in a file;
2. between requirement records;
3. immediately after a requirement opener;
4. between two complete fields in a requirement record;
5. after the final field and before `end requirement`.

A comment line shall not occur between a `statement:` or `rationale:` label
and its first body line, between two lines of a prose body, or within a math
block. A comment at column 1 following a prose body terminates that body and is
permitted only if the next non-comment line is a valid following field or the
record closer.

There are no inline or block comments. A `#` occurring anywhere other than
column 1 is an ordinary content character. In particular, `#` in a scalar
value, an indented prose line, or an indented math payload is semantic content
and does not begin a comment.

Changing or removing a source comment shall not change the semantic value of
a conforming source set. A comment is not semantically attached to the record
or field that follows or precedes it.

### 8.2 Requirement identifiers [syntax.identifier]

```text
identifier       ::= id-initial id-continuation*
id-initial       ::= ASCII-LETTER | ASCII-DIGIT
id-continuation  ::= ASCII-LETTER | ASCII-DIGIT | "-" | "_" | "."
ASCII-LETTER     ::= "A" ... "Z" | "a" ... "z"
ASCII-DIGIT      ::= "0" ... "9"
```

An identifier contains no whitespace. Hyphen, underscore, and period carry no
namespace, hierarchy, allocation, or type meaning.

### 8.3 Scalar lines [syntax.scalar]

```text
title-line       ::= "title:" SP scalar-value EOL
allocation-line  ::= "allocation:" SP scalar-value EOL
source-line      ::= "source:" SP scalar-value EOL
decomposes-line  ::= "decomposes:" SP identifier EOL
```

`scalar-value` shall contain one or more permitted source characters on one
physical line. Its first and last characters shall not be any of the following
whitespace characters:

- U+0009 through U+000D;
- U+0020;
- U+0085;
- U+00A0;
- U+1680;
- U+2000 through U+200A;
- U+2028, U+2029, or U+202F;
- U+205F;
- U+3000.

Some members of this set are already excluded by the physical-source rules.
The complete list makes the prohibition of leading and trailing whitespace
independent of a programming-language runtime's whitespace predicate.
Because exactly one SP belongs to the field prefix, a value beginning with SP
would also visually contain more than the required separator and is invalid.

Other non-prohibited Unicode characters are value characters. Tools may
diagnose visually confusing characters as a separate style policy, but shall
not trim them or change the 0.2 scalar value.

Colons and internal runs of spaces are ordinary scalar characters. Values are
not quoted and undergo no unescaping or trimming.

### 8.4 Record form [syntax.record]

```text
requirement-record ::= opener-line
                       comment-line*
                       title-line
                       comment-line*
                       (allocation-line comment-line*)?
                       statement-field
                       comment-line*
                       (rationale-field comment-line*)?
                       (source-line comment-line*)?
                       (decomposes-line comment-line*)*
                       closer-line

opener-line        ::= "requirement" SP identifier EOL
closer-line        ::= "end requirement" EOL
statement-field    ::= "statement:" EOL body
rationale-field    ::= "rationale:" EOL body
```

The opener, closer, and field labels begin at column 1. The field order shown
is mandatory. Required fields shall be present exactly once. Optional scalar
and prose fields shall occur at most once. `decomposes` may occur zero or more
times only in its indicated position.

An unknown field, a duplicate singleton field, or an out-of-order field is
invalid. A requirement record shall not contain another requirement opener.

### 8.5 Record separation [syntax.separation]

Two adjacent requirement records in the same file shall be separated by one
or more blank physical lines, comment lines, or a combination of the two. Zero
or more blank physical lines and comment lines may precede the first record
and follow the last record. A source file containing comments but no
requirement record is invalid.

### 8.6 Prose bodies [syntax.body]

A body is the maximal sequence of physical lines immediately following a
`statement:` or `rationale:` line for which each line is either:

1. a blank physical line; or
2. a line beginning with exactly two structural SP characters.

For a line in case 2, the two leading SP characters are removed to form one
body-content line. Any additional leading characters, including additional
spaces, are content. A blank physical line and a source line containing
exactly two SP characters both form an empty body-content line.

A nonblank line beginning with whitespace other than the required two SP
characters is invalid rather than a body terminator. A column-1 structural
line, including a comment line, terminates the body.

The body shall contain at least one body-content line whose length after the
two-space structural removal is greater than zero. This is a syntactic
nonemptiness rule; the language does not judge whether the resulting prose is
substantive.

## 9. Requirement interpretation [interpretation]

### 9.1 Record interpretation [interpretation.record]

Each requirement record denotes one requirement. Its opener identifier and
fields populate the corresponding members in Clause 5.2. An omitted optional
field denotes absence, which is distinct from an empty string. The syntax has
no representation for a present empty scalar field.

### 9.2 Prose folding [interpretation.prose]

Except while interpreting a statement math block under Clause 9.3, prose body
lines are folded as follows:

1. Each maximal sequence of consecutive nonempty body-content lines denotes
   one prose paragraph.
2. The paragraph value is those lines joined in order by one U+0020 SPACE.
3. Empty body-content lines separate paragraphs.
4. Leading, trailing, and repeated empty body-content lines do not create
   empty paragraphs.

Characters already present in a nonempty body-content line are preserved.
The joining SP is added even if an adjacent content line already begins or
ends with a space.

Manual wrapping is therefore semantically invisible only when the author does
not introduce meaningful leading or trailing content spaces.

### 9.3 Statement math blocks [interpretation.math]

Math-block recognition applies only to a `statement` body.

A body-content line exactly equal to `math latex` begins a math block. The
next body-content line exactly equal to `end math` terminates it. A statement
line exactly equal to `end math` outside a math block is invalid. A math block
without a terminating marker is invalid.

Each line between the markers is interpreted as follows:

1. An empty body-content line contributes an empty payload line.
2. Every nonempty body-content line shall begin with two additional SP
   characters.
3. Those two additional SP characters are removed.
4. All remaining characters are preserved as one payload line.

The payload value is the payload lines joined by LF. Leading, internal, and
trailing empty payload lines are preserved. At least one de-indented payload
line shall have length greater than zero.

The begin and end markers do not contribute content blocks. The payload
denotes one math content block with language label `latex`. Encountering a
math block terminates any preceding prose paragraph; following prose begins a
new prose paragraph even if no empty body line separates it from the block.

The language defines no escape for a statement line exactly equal to
`math latex` or `end math`. In a rationale body these strings are ordinary
prose because rationale does not recognize math blocks.

**Note:** Payload preservation applies after CRLF normalization and the two
specified structural de-indentations. It is character preservation, not
preservation of the original encoded bytes.

### 9.4 Statement and rationale values [interpretation.fields]

The statement value is the ordered sequence of prose and math blocks produced
by Clauses 9.2 and 9.3. It shall contain at least one block.

The rationale value, when present, is the ordered sequence of prose paragraphs
produced by Clause 9.2. It shall contain at least one paragraph.

### 9.5 Concrete order [interpretation.order]

Statement and rationale content-block order is semantic. Requirement record
order, source-file order, and decomposition-line order are not semantic.

An implementation may preserve nonsemantic order for presentation, but no
language operation may infer requirement priority, hierarchy, or document
composition from it.

## 10. Source-file validity [validity.file]

A conforming source file shall satisfy all of the following:

1. It satisfies the physical representation rules in Clause 6.
2. It contains one or more complete requirement records.
3. All nonblank content is either inside a requirement record or is a
   permitted source comment.
4. Records are separated as required by Clause 8.5.
5. Every opener and closer is matched; records are not nested.
6. Every identifier satisfies Clause 8.2.
7. Every record has exactly the field cardinalities and order in Clause 8.4.
8. Every scalar and prose field has the required form and is nonempty.
9. Every math block satisfies Clause 9.3.
10. No requirement repeats the same decomposition target.
11. Every source comment occurs at a position permitted by Clause 8.1.1.

Repeated decomposition targets are compared by exact identifier equality.
The restriction applies within one requirement record. A requirement may
share a target with any number of other requirements.

## 11. Source-set validity [validity.set]

### 11.1 Identifier uniqueness [validity.identity]

No two records in a source set shall have the same identifier. This rule
applies across all selected files and also rejects byte-identical duplicate
records.

### 11.2 Referential integrity [validity.reference]

Every decomposition target shall equal the identifier of exactly one
requirement in the same source set.

References to requirements outside the selected source set are dangling and
invalid. Version 0.2 has no external-reference, import, include, namespace, or
package mechanism.

### 11.3 No additional universal policy [validity.policy]

The following are not source-language validity conditions:

- decomposition completeness;
- decomposition acyclicity;
- the presence or resolvability of `source`;
- an allowed allocation vocabulary;
- prose style or requirement quality;
- approval or review state;
- verification planning or coverage;
- baseline naming or authority.

An implementation may evaluate such rules as separately identified project
policy. It shall distinguish policy failure from failure to conform to this
source-language standard.

## 12. Diagnostics [diagnostics]

### 12.1 Required information [diagnostics.required]

For each diagnostic it reports for source-language nonconformance, an
interpreter shall identify:

1. the source-file name;
2. a one-based line;
3. a one-based column;
4. an understandable category of failure;
5. a plain-language description sufficient to locate the violated rule.

Exact diagnostic codes, wording, punctuation, ordering, and recovery behavior
are not standardized by version 0.2.

### 12.2 Diagnostic coverage [diagnostics.coverage]

A conforming interpreter shall be capable of rejecting at least these classes
of violation:

- invalid UTF-8, a leading byte-order mark, prohibited characters, invalid
  line endings, and a missing final line ending;
- an empty source file or nonblank content outside a record;
- malformed, unmatched, nested, or unclosed record boundaries;
- invalid identifiers;
- missing, duplicate, unknown, out-of-order, or malformed fields;
- empty or incorrectly indented bodies;
- unexpected, empty, incorrectly indented, or unterminated math blocks;
- source comments that replace or interrupt required prose or math content;
- duplicate requirement identifiers;
- duplicate decomposition targets;
- invalid or dangling decomposition references.

An interpreter may stop after the first syntax diagnostic in a source file.
It need not recover a partial semantic requirement from an invalid record.
Diagnostics for whole-set identity and referential-integrity failures shall
not depend on hidden repository state.

### 12.3 Input diagnostics [diagnostics.input]

A source-selection tool shall report unavailable explicit inputs, failures
encountered while traversing selected directories, and selection of no source
files. The exact mapping of filesystem failures to diagnostics is
implementation-defined.

## 13. Semantic equivalence [equivalence]

Two conforming source sets are semantically equivalent in version 0.2 if and
only if they denote equal source-set values under Clause 5. In particular,
semantic equivalence requires:

1. the same set of requirement identifiers;
2. equal scalar and optional-field values for each identifier;
3. equal ordered statement and rationale content blocks;
4. equal unordered decomposition-target sets for each identifier.

The following differences alone do not affect semantic equivalence:

- LF versus CRLF;
- source filenames and directory paths;
- distribution of complete records among files;
- file traversal order;
- record order;
- decomposition-line order;
- physical wrapping of prose lines when folding produces the same paragraph
  strings;
- source coordinates;
- source-comment presence, placement, and text.

The language does not define a canonical source serialization. A normalized
inventory or formatter output is derived and need not reproduce original
source bytes.

## 14. Revisions, snapshots, and baselines [environment]

### 14.1 Language boundary [environment.boundary]

The language interprets a source set as supplied. It does not read Git
history, branches, tags, commits, authorship, review state, or timestamps.

### 14.2 Requirement revisions [environment.revision]

A requirement revision is the value of a requirement in a particular
source-set snapshot. No revision number or timestamp is intrinsic to the
requirement record.

### 14.3 Baselines [environment.baseline]

A project may identify a repository snapshot as a baseline using a Git commit
and may assign a durable name using an annotated tag or another documented
convention. Baseline scope, approval, authority, and certification meaning are
project-governance facts. They do not follow merely from the existence of a
Git tag and are not represented by this source language.

## 15. Version and compatibility [version]

### 15.1 Contract identification [version.id]

This specification defines `mundanereq-source-0.2`. Source files contain no
embedded language-version directive. A tool claiming conformance shall state
that it implements this contract through its interface or documentation.

Every source set conforming to version 0.1 also conforms to version 0.2 and
has the same semantic value. Version 0.2 additionally accepts source comments
as specified in Clause 8.1.1. A strict 0.1 interpreter is required to reject
such comments; a strict 0.2 interpreter is required to accept them.

### 15.2 Change classification [version.changes]

Under the provisional compatibility policy:

1. A clarification that changes neither accepted source nor semantic
   interpretation may retain the same contract identifier.
2. A change that accepts additional syntax requires a later provisional
   contract.
3. A change that rejects previously conforming source or changes its semantic
   interpretation requires a new contract identifier.

No compatibility promise applies to an experimental CLI, implementation API,
diagnostic code string, normalized inventory format, rendered artifact, or
companion language unless its own specification says otherwise.

## 16. Excluded facilities [excluded]

Version 0.2 defines none of the following:

- partial records, includes, imports, overrides, or inheritance;
- modules, namespaces, packages, or cross-repository references;
- authored views, sections, collections, or document ordering;
- arbitrary or user-defined attributes;
- controlled allocation objects or vocabularies;
- glossary or formal-symbol definitions;
- approval, signatures, access control, or review workflow;
- intrinsic status, retirement, revision, timestamp, or change-justification
  fields;
- verification activities, coverage, executions, evidence, or results;
- relationship identity or relationship metadata;
- variants or reuse mechanisms;
- a canonical formatter;
- semantic diff or semantic merge;
- persistent indexes, databases, or servers;
- Markdown interpretation of field content;
- formal interpretation or validation of LaTeX;
- ReqIF import, export, or round-trip behavior;
- rendering, generated documents, reports, or matrices.

The use of one of these facilities by an independent tool does not make it
part of the source language. In particular, plain field text may contain
Markdown-like, LaTeX-like, or other notation as ordinary characters without
the core language interpreting that notation. Source comments remain distinct
from durable requirement remarks or annotations, neither of which is defined.

## Annex A — Collected grammar [grammar]

This annex is normative.

The grammar is line-oriented. `body` is consumed maximally as specified in
Clause 8.6, and its semantic content is then interpreted under Clause 9.

```text
source-file          ::= file-trivia* requirement-record
                         (record-separator requirement-record)* file-trivia*

file-trivia          ::= blank-line | comment-line
record-separator     ::= file-trivia+

requirement-record   ::= opener-line
                         comment-line*
                         title-line
                         comment-line*
                         (allocation-line comment-line*)?
                         statement-field
                         comment-line*
                         (rationale-field comment-line*)?
                         (source-line comment-line*)?
                         (decomposes-line comment-line*)*
                         closer-line

opener-line          ::= "requirement" SP identifier EOL
closer-line          ::= "end requirement" EOL

title-line           ::= "title:" SP scalar-value EOL
allocation-line      ::= "allocation:" SP scalar-value EOL
source-line          ::= "source:" SP scalar-value EOL
decomposes-line      ::= "decomposes:" SP identifier EOL

statement-field      ::= "statement:" EOL body
rationale-field      ::= "rationale:" EOL body

body                 ::= body-line*
body-line            ::= blank-line | SP SP body-content EOL
blank-line           ::= EOL
comment-line         ::= "#" comment-text EOL

identifier           ::= id-initial id-continuation*
id-initial           ::= ASCII-LETTER | ASCII-DIGIT
id-continuation      ::= ASCII-LETTER | ASCII-DIGIT | "-" | "_" | "."
ASCII-LETTER         ::= "A" ... "Z" | "a" ... "z"
ASCII-DIGIT          ::= "0" ... "9"
```

`scalar-value` is constrained by Clause 8.3. `body-content` and `comment-text`
are any sequences, including the empty sequence, of source characters
permitted on one physical line. A `body` shall satisfy the nonemptiness
condition in Clause 8.6. Comment placement is additionally constrained by
Clause 8.1.1.

The grammar is supplemented by the fixed-order and cardinality rules in
Clause 8.4, math-block rules in Clause 9.3, and file/set validity constraints
in Clauses 10 and 11.

## Annex B — Example [example]

This annex is informative.

```text
# This comment is retained for authors and ignored by semantic tools.
requirement SYS-007
title: Loss-of-link response
allocation: Mission-control coordinator
# Confirm the timing value through the normal review workflow.
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
```

The statement denotes one prose paragraph. The physical wrapping does not
appear in the semantic paragraph because the three lines are joined by one
space each. The two decomposition lines denote an unordered set of two
outgoing relationships.

## Annex C — Conformance material [conformance.material]

This annex is informative except where the provisional contract explicitly
designates an expected semantic inventory as normative.

The repository's [`conformance/0.2`](../conformance/0.2/README.md) directory
contains:

- a valid source set exercising every 0.2 field, prose folding, paragraphs,
  math, and decomposition;
- its expected normalized semantic inventory;
- representative invalid inputs.

The fixtures supplement but do not replace this specification. If an
accidental conflict is found, the specification controls until the fixture or
the contract is deliberately revised under Clause 15.2.

## Annex D — Design rationale index [rationale]

This annex is informative.

The reasons for selecting these rules, competing representations, and the
experimental evidence are recorded outside this normative standard:

- [Specification 0001](0001-project-foundation.md) — project mission and
  architectural principles;
- [Specification 0002](0002-minimum-source-language-and-model.md) — minimum
  model decisions and rationale;
- [Specification 0006](0006-provisional-0.2-contract.md) — trial audience,
  operational contract, and compatibility policy;
- [Research 0007](../research/0007-provisional-source-representation-decision.md)
  — representation decision;
- [Research 0009](../research/0009-nonsemantic-source-comments.md) — source
  comment decision and 0.1-to-0.2 change;
- [Experiments 0001 through 0007](../experiments/) — empirical evidence and
  limitations.
