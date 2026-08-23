# Research 0008: Source Language Formalization Review

Status: Completed

Result date: 2026-08-22

## Question

Can the provisional `mundanereq-source-0.1` language be stated as a complete,
implementation-independent standard without adding features or changing its
semantic model?

## Result

Yes. [Specification 0004](../specification/0004-mundanereq-source-language-0.1.md)
consolidates the existing 0.1 rules into a normative language standard with:

- defined conformance categories and normative vocabulary;
- an abstract requirement and relationship model;
- exact physical-source and source-selection rules;
- collected lexical and line-oriented grammar;
- deterministic prose and math interpretation;
- file-level and source-set validity constraints;
- diagnostic requirements;
- semantic-equivalence rules;
- explicit language boundaries and excluded facilities.

Specification 0002 remains the design rationale. Specification 0003 remains
the operational trial contract. Specification 0004 is now the normative
language definition.

## Clarifications made during formalization

The formal standard resolves details that were implicit or distributed across
the earlier specification, reference implementation, tests, and conformance
fixtures:

1. A blank physical line contains no characters; a two-space prose body line
   becomes an empty semantic body line only after structural de-indentation.
2. Leading and trailing file blank lines are permitted, while adjacent records
   require at least one blank physical line.
3. Scalar values have no quoting, escaping, trimming, or markup semantics.
4. The exact Unicode whitespace set prohibited at scalar boundaries is stated
   independently of runtime-library predicates.
5. The exact prohibited control-character ranges are stated rather than using
   the ambiguous phrase "control character."
6. Statement math markers are exact and unescaped; the same strings in a
   rationale are prose.
7. Math preservation means character and line-break preservation after CRLF
   normalization and structural de-indentation, not original byte
   preservation.
8. Semantic and nonsemantic order are distinguished explicitly.
9. Self-decomposition and other cycles are language-valid but may be rejected
   by separate policy.
10. Explicit non-`.mreq` files, descendant `.git` exclusion, symlink behavior,
    input deduplication, and empty selection are stated as source-selection
    rules.
11. Source coordinates count Unicode scalar values, with a byte-based exception
    for malformed UTF-8 that cannot be decoded.
12. Extensions may exist only outside a strict 0.1 conformance mode.

These are formal resolutions of existing rules, not new requirement fields,
relationships, syntax forms, or tool capabilities.

## Reference-probe discrepancies exposed

Comparison with the Experiment 0002 Java probe found three edge cases where
the implementation does not yet satisfy the formal rule completely:

1. The probe rejects the ASCII C0 controls and DEL but does not reject all C1
   control characters U+0080 through U+009F.
2. The probe's scalar padding checks combine an ASCII-only leading-space test
   with Java's `stripTrailing` behavior. It therefore does not apply the
   standard's explicit Unicode whitespace set symmetrically.
3. Java string offsets count UTF-16 code units, so a diagnostic column after a
   supplementary Unicode scalar value can be one greater than the standard's
   scalar-value column.

The existing source corpora and conformance fixtures contain none of these
edge cases. The 12 grouped Experiment 0002/0005 tests continue to pass. These
are narrow conformance repairs for a later implementation step, not evidence
for changing the language or its model.

## No feature expansion

The formalization does not introduce comments, arbitrary attributes, views,
new relationships, version directives, verification syntax, glossary syntax,
ReqIF behavior, or any other deferred facility.

It also does not elevate experimental CLI behavior, diagnostic code strings,
normalized inventory syntax, or Java implementation details into the language
standard.

## Disposition

Use Specification 0004 as the normative reference for 0.1 source and
interpreter conformance. Use Specifications 0001 through 0003 and the research
and experiment records to understand rationale, product scope, and evidence.

Before calling the reference probe a conforming implementation of the formal
standard, add conformance cases for the three Unicode edge conditions and make
the narrow implementation corrections they expose.
