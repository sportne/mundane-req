# mundanereq Formatter Trial Contract 0.1

Status: Maintained trial

Tool version: `trial-0.1`

Source contract: `mundanereq-source-0.2`

Reproducible source baseline: Git tag `formatter-trial-0.1`

## 1. Purpose

`mundanereq-format` applies a small canonical physical representation to a
conforming `mundanereq-source-0.2` source set. It is an independent command
line tool for reducing line-ending and inter-record blank-line noise in
ordinary Git diffs.

Formatting does not change the requirement model. The source remains
authoritative; the executable and compiled classes are derived and disposable.

## 2. Installation

The trial is distributed as source. Select a Java 21 GraalVM installation with
Native Image and build the no-fallback native executable:

    sdk use java 21.0.2-graalce
    make native-formatter

The resulting Linux executable is:

    build/maintained/mundanereq-format

`make formatter-verify` rebuilds it and runs the maintained safety and
JVM/native agreement evidence set.

## 3. Invocation

The supported forms are:

    mundanereq-format [--] FILE [CONTEXT...]
    mundanereq-format --check [--] FILE_OR_DIRECTORY...
    mundanereq-format --write [--] FILE_OR_DIRECTORY...
    mundanereq-format --help
    mundanereq-format --version

`--` ends option recognition. There is no implicit current-directory input,
repository root, manifest, or configuration file.

In standard-output mode, `FILE` shall name an explicit regular file and is the
only file emitted. `FILE` and every optional `CONTEXT` input together select
the complete source set used for validation. Context may therefore supply
requirements referenced from `FILE` without being emitted or modified.

Check and write modes format every file selected from their inputs. Explicit
regular files are selected regardless of suffix. Directories are traversed
recursively for `.mreq` regular files, `.git` subdirectories are skipped,
symbolic links are not followed, and normalized duplicate paths select one
file, as defined by Source Language Specification 0.2 Clause 7.

## 4. Preconditions

The complete selected source set shall conform to `mundanereq-source-0.2`
before formatted bytes are emitted or any destination is replaced. Validation
includes physical source, syntax, identity uniqueness, and decomposition
target existence across file boundaries.

The formatter does not repair invalid source. A diagnostic selection produces
no formatted standard output, and write mode performs no replacement if
prevalidation finds any diagnostic.

## 5. Canonical formatting

For every conforming selected file, the formatter performs exactly two kinds
of rewrite:

1. every physical line is emitted with an LF line ending; and
2. when an `end requirement` line is followed by one or more blank physical
   lines and then the next `requirement` opener, with no comment or other line
   intervening, exactly one blank physical line is emitted between the two
   records.

All nonblank physical-line text and order are preserved exactly. Blank lines
outside the comment-free inter-record case are preserved exactly. In
particular, the formatter does not:

- reflow prose or alter its indentation;
- change scalar spelling or field order;
- sort requirements or repeated `decomposes` fields;
- change, move, attach, or remove source comments; or
- parse or alter opaque LaTeX payloads.

**Example:** Before formatting, this source has three blank lines between
records:

```text
end requirement



requirement NEXT
```

After formatting it has one:

```text
end requirement

requirement NEXT
```

If any comment occurs between those records, every surrounding blank line and
the comment line remain exactly where authored. This rule deliberately avoids
implying comment attachment.

Applying the formatter to its own output produces the same byte sequence.

## 6. Standard-output mode

Standard-output mode emits the formatted bytes of `FILE` to standard output
and does not modify any selected file. It writes no success summary or other
text around those bytes.

It returns `0` after complete output. It returns `2` if invocation, source-set
validation, file selection, or output fails.

## 7. Check mode

Check mode compares every selected file's bytes with its formatted bytes and
does not modify source. For each file that differs, it writes this line to
standard output in deterministic selected-path order:

    Needs formatting: ABSOLUTE_PATH

It returns `0` when every selected file is already formatted, `1` when one or
more conforming files would change, and `2` when invocation, source-set, or
check-report output failure prevents completion.

## 8. Write mode

Write mode validates the complete selected source set before replacing any
file. Each changed file is written completely to a same-directory temporary
file and then moved over that one destination. Unchanged files are not
replaced. On success it writes:

    Formatted N file.

for one replacement, or:

    Formatted N files.

otherwise, including zero replacements. It returns `0` after all required
replacements and successful summary output. It returns `2` for invocation,
source-set, replacement, or summary-output failure.

Replacement safety is per file, not transactional across the selected set. A
write failure may occur after earlier files were replaced. A failed individual
replacement does not incrementally populate its destination and its temporary
file is removed when cleanup is possible.

On filesystems supporting the POSIX permission view, the destination's read,
write, and execute bits are preserved. This contract does not promise to
preserve ownership, ACLs, extended attributes, or hard-link identity.

## 9. Diagnostics

Source and input diagnostics use the shared human-readable form:

    file:line:column: category: message

Source coordinates follow Source Language Specification 0.2. Category values
and explanatory wording remain provisional rather than a machine-readable
compatibility interface. Output and replacement failures use an analogous
`output-failed` or `write-failed` diagnostic.

Failure to write formatted source, a check report, a write summary, help, or
version output produces `output-failed` and returns `2`. In write mode this can
be detected only after source replacements have completed and the summary is
attempted; the replacements are not rolled back.

`--help` and `--version` return `0` after writing successfully to standard
output. Unknown options, missing inputs, and invalid paths return `2` and write
explanatory text to standard error.

## 10. Compatibility boundaries

Three compatibility surfaces are separate:

1. Source syntax and semantics are defined by the normative
   [Source Language Specification 0.2](0005-mundanereq-source-language-0.2.md),
   not by this formatter.
2. This document defines the maintained `trial-0.1` formatter command and
   policy. A later trial may revise them explicitly; no stable 1.0 CLI or
   formatting promise is made.
3. Java packages, classes, records, and methods are implementation details with
   no published library compatibility guarantee.

Formatting policy is not part of source conformance. A conforming 0.2 file may
need formatting, and a tool can implement the 0.2 language without implementing
this formatter.

The Git tag identifies source, tests, and build instructions, not a portable
binary distribution. Native behavior is supported only where the documented
build and verification complete successfully.

## 11. Deliberate omissions

This trial does not:

- repair or partially format invalid source;
- reflow, rewrite, or semantically edit requirement content;
- infer comment attachment or document/view order;
- provide editor integration or automatic formatting on merge;
- select source from hidden project configuration;
- maintain a cache, index, database, daemon, or server; or
- promise set-wide transactional writes or multi-platform prebuilt binaries.

## 12. Evidence

[Experiment 0008](../experiments/0008-formatting-policy/README.md) records the
policy comparison and selection. [Research 0015](../research/0015-formatter-verification.md)
records semantic preservation, idempotence, comment/math preservation,
ordinary-diff review, failure behavior, complete maintained-corpus coverage,
and JVM/native agreement. `make formatter-verify` is the executable acceptance
gate associated with this release.
