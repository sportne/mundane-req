# Research 0009: Nonsemantic Source Comments

Status: Selected for provisional 0.2

Decision date: 2026-08-22

## Question

Should authors be able to leave durable source-level comments without making
those comments part of a requirement's semantic value?

## Decision

Add one full-line comment form in `mundanereq-source-0.2`:

```text
# This is an author comment.
```

The initial `#` occurs at column 1. The entire line is retained in source but
ignored by semantic interpretation. Comments may occur around records and at
defined boundaries between structural fields. They cannot interrupt prose or
math bodies.

This is deliberately a source comment, not a `remark` or `note` field. It has
no semantic attachment to a requirement, revision, relationship, or field.
Changing only comments leaves the semantic inventory unchanged.

## Engineering need

Authors sometimes need to record local editing context, cautions, or temporary
coordination information close to the text under discussion. Requiring such
notes to become requirement data would make every casual author annotation a
modeling, rendering, query, and interchange concern.

A source comment supplies the ordinary text-language behavior without
expanding the requirement object.

## Alternatives considered

### Durable `remark:` field

Rejected for this need. A field would become part of the requirement's
semantic value and create unresolved questions about cardinality, rendering,
ReqIF mapping, lifecycle, and whether it differs from `rationale`.

### Inline comments

Rejected initially. An inline delimiter can unexpectedly reinterpret
otherwise ordinary requirement text and requires escape or quoting rules. It
also makes it less obvious whether a textual suffix is normative content.

### Block comments

Rejected initially. Block delimiters introduce multiline termination,
nesting, recovery, and merge-conflict questions without a demonstrated need.

### Indented comments in prose or math

Rejected. Within prose and mathematical payloads, `#` remains ordinary
content. Treating it as a comment would make standalone field text harder to
read and would conflict with notations that use `#`.

### Comments only between records

Too restrictive. Authoring context frequently concerns a particular field.
Allowing column-one comments between complete structural fields addresses
that need while retaining unambiguous prose and math boundaries.

## Placement model

A comment is permitted:

- before or after records in a file;
- between records, where it also supplies record separation;
- after a record opener;
- between complete fields;
- before a record closer.

A comment is not permitted:

- between a prose-field label and its first body line;
- between prose body lines;
- inside a math block.

There is no attachment rule based on proximity. Moving a requirement record
does not imply that a tool knows which nearby comments should move with it.
That remains an ordinary source-editing responsibility.

## Compatibility consequence

Adding comments expands accepted syntax, so the 0.1 compatibility policy
requires a new contract identifier. Version 0.2 is a strict semantic superset:
all conforming 0.1 source is conforming 0.2 source with the same semantic
value, while a source file using comments is not conforming 0.1 source.

No embedded version directive is added. The current repository contract is
declared at repository level.

## Disposition

Specify, implement, and test the comment form as the sole source-language
change in provisional 0.2. Do not add durable remarks, inline comments, block
comments, comment attachment, or tool directives.
