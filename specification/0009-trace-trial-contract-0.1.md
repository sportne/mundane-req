# mundanereq Trace Trial Contract 0.1

Status: Maintained trial

Tool version: `trial-0.1`

Source contract: `mundanereq-source-0.2`

Reproducible source baseline: Git tag `trace-trial-0.1`

## 1. Purpose

`mundanereq-trace` answers four bounded navigation questions over authored
`decomposes` relationships in a conforming `mundanereq-source-0.2` source set.
It is an independent command-line tool for following higher-level context and
finding lower-level requirements that may warrant inspection during a change.

Trace results do not become source. Incoming links, paths, strongly connected
components, and the in-memory indexes used to calculate them are derived and
disposable. The human-readable source and its Git history remain authoritative.

## 2. Installation

The trial is distributed as source. Select a Java 21 GraalVM installation with
Native Image and build the no-fallback native executable:

    sdk use java 21.0.2-graalce
    make native-trace

The resulting standalone Linux executable is:

    build/maintained/mundanereq-trace

The executable need not be committed. `make trace-verify` rebuilds it and runs
the maintained graph, workflow, and JVM/native agreement evidence set.

## 3. Invocation and source selection

The supported forms are:

    mundanereq-trace parents ID FILE_OR_DIRECTORY...
    mundanereq-trace children ID FILE_OR_DIRECTORY...
    mundanereq-trace higher ID FILE_OR_DIRECTORY...
    mundanereq-trace impact ID FILE_OR_DIRECTORY...
    mundanereq-trace --help
    mundanereq-trace --version

There is no implicit current-directory input, repository root, manifest,
configuration file, or persistent project index. Exactly one operation and one
query identifier precede at least one explicit source input. This trial does
not define `--` option termination because source inputs occur only after the
operation and identifier.

Each explicit regular file is selected regardless of suffix. Directory inputs
are traversed recursively for `.mreq` regular files, `.git` subdirectories are
skipped, symbolic links are not followed, and normalized duplicate paths
select one file, as defined by Source Language Specification 0.2 Clause 7. All
selected files form one semantic source set and must conform before a query is
answered.

## 4. Relationship meaning

For an authored relationship `LOWER decomposes HIGHER`:

- `HIGHER` is directly higher-level than `LOWER`; and
- `LOWER` is directly lower-level than `HIGHER`.

These terms describe navigation along the source language's one relationship
type. They do not assert that `LOWER` completely satisfies `HIGHER`, that the
decomposition is complete, or that every requirement must participate in a
relationship.

The queried requirement itself is excluded from transitive results, even if a
cycle reaches it again. An authored self-relationship remains visible in both
direct operations.

## 5. Supported questions

### 5.1 `parents`

Question: Which requirements are directly higher-level than `ID` because `ID`
has a `decomposes` relationship to them?

The output begins:

    Direct higher-level requirements for ID:

It then lists each directly higher-level requirement identifier once, in
ASCII-ordinal lexical order, one per line.

### 5.2 `children`

Question: Which requirements are directly lower-level than `ID` because they
have a `decomposes` relationship to `ID`?

The output begins:

    Direct lower-level requirements for ID:

It then lists each directly lower-level requirement identifier once, in
ASCII-ordinal lexical order, one per line. Incoming relationships are derived;
no reverse relationship is authored or stored.

### 5.3 `higher`

Question: Which distinct higher-level requirements can be reached by following
one or more `decomposes` relationships from `ID`, and what is one shortest path
to each?

The output begins:

    Higher-level decomposition paths from ID:

### 5.4 `impact`

Question: Which distinct lower-level requirements have a path of one or more
`decomposes` relationships ending at `ID`, and what is one shortest path from
each lower-level requirement to `ID`?

The output begins:

    Lower-level impact paths to ID:

“Impact” identifies requirements that may warrant inspection. It does not
assert that every returned requirement must change, that an unreturned artifact
cannot be affected, or that requirement prose has been semantically analyzed.

## 6. Transitive path output

After a `higher` or `impact` header, each result line has this form:

    DISTANCE ID -> ... -> ID

`DISTANCE` is the decimal number of decomposition relationships in the path.
The path is simple and does not repeat an identifier. `higher` paths begin at
the query and end at the reported higher-level requirement; `impact` paths
begin at the reported lower-level requirement and end at the query.

Exactly one shortest path is emitted for each distinct result. If several
shortest paths exist, the lexicographically least complete identifier sequence
is selected. Identifier comparison is ordinal comparison of the allowed ASCII
identifier characters by code-point value. Sequences are compared element by
element, with a shorter prefix sorting before a longer sequence.

Result lines are ordered first by increasing distance and then by the reported
requirement identifier in that lexical order. These are output properties, not
requirements on a conforming implementation's traversal algorithm.

If an operation has no result, the line after its header is:

    (none)

## 7. Cycles

Cycles are permitted by the source language. A cycle does not make a trace
query fail, and traversal always terminates.

For a transitive operation, the reachable scope consists of the query and the
requirements reachable in that operation's direction. After result lines, or
after `(none)`, each cyclic strongly connected component in that scope is
reported as:

    Cycle observed among: ID [ID...]

A component is cyclic when it contains multiple requirements or one
requirement with an authored self-relationship. Identifiers within a component
are lexically ordered, and components are ordered by their first identifier.
The observation is structural and does not change exit status. Whether a cycle
violates project policy belongs to a separate linter or policy check.

Direct operations do not report components. They expose all authored edges at
the query boundary, including a self-relationship.

## 8. Validation, diagnostics, and precedence

The query identifier must conform to the source-language identifier grammar.
The complete selected source set is then validated for physical form, syntax,
identity uniqueness, and decomposition-target existence before query lookup or
output.

Errors have this precedence:

1. unknown operation, omitted query, or omitted source input;
2. malformed query identifier;
3. source selection and complete source-set validation; and
4. lookup of a well-formed query identifier in valid selected source.

Thus a malformed invocation is rejected before reading source, while a validly
formed but absent query never hides source diagnostics. Source and query
diagnostics use the shared human-readable form:

    file:line:column: category: message

Diagnostic categories and wording remain provisional rather than a
machine-readable protocol. A failed query emits no partial trace result.

## 9. Exit status and output streams

- `0`: a query, `--help`, or `--version` completed and its output was written.
- `2`: invocation, query identifier, input selection, source conformance,
  query lookup, or output failure prevented completion.

There is no special status for empty results, incomplete decomposition, or
cycle policy. Successful query output is written only to standard output.
Diagnostics and usage for failed invocations are written to standard error.
Failure to write successful output produces an `output-failed` diagnostic and
status `2`.

## 10. Compatibility boundaries

Three compatibility surfaces are separate:

1. Source syntax and `decomposes` semantics are defined by the normative
   [Source Language Specification 0.2](0005-mundanereq-source-language-0.2.md),
   not by this trace command.
2. This document defines the maintained `trial-0.1` trace questions and
   human-readable output. A later trial may revise them explicitly; no stable
   1.0 CLI or machine-output promise is made.
3. Java packages, classes, records, methods, and graph algorithms are
   implementation details with no published library compatibility guarantee.

The Git tag identifies source, tests, and build instructions, not a portable
binary distribution. Native behavior is supported only where the documented
build and verification complete successfully.

Although exact trial output is specified so humans can predict and compare it,
consumers must not treat the prose format or diagnostic categories as a stable
machine-readable protocol. A future machine interface requires a separate,
explicit contract.

## 11. Deliberate omissions

This trial does not:

- query relationship types other than `decomposes`;
- decide decomposition completeness, coverage, satisfaction, or correctness;
- analyze prose to infer semantic change impact;
- enforce cycle or organization-specific policy;
- author, repair, format, render, or modify source;
- provide arbitrary graph expressions, every path, visualization, or a browser;
- emit JSON or another stable machine-readable trace form;
- maintain a cache, reverse link, index, database, daemon, or server;
- discover source through hidden project configuration; or
- promise multi-platform prebuilt binaries.

## 12. Evidence

[Research 0016](../research/0016-first-trace-interface.md) records the workflow
evidence, alternatives, and bounded interface decision. [Experiment 0009](../experiments/0009-trace-workflows/README.md)
records independently expected graph results, equivalent file layouts,
argument-order invariance, direct and transitive cycle behavior, sustained and
transferred corpus results, realistic authoring changes, and exact JVM/native
status and output-byte agreement. `make trace-verify` is the executable
acceptance gate associated with this release.
