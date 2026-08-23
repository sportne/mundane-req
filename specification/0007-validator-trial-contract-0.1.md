# mundanereq Validator Trial Contract 0.1

Status: Maintained trial

Tool version: `trial-0.1`

Source contract: `mundanereq-source-0.2`

Reproducible source baseline: Git tag `validator-trial-0.1`

## 1. Purpose

`mundanereq-validate` determines whether explicitly selected source files form
a conforming `mundanereq-source-0.2` source set. It is an independent command
line tool intended for ordinary editor repair loops and CI jobs.

The source remains authoritative. The executable, compiled classes, and any CI
logs are derived and disposable.

## 2. Installation

The trial is distributed as source. Select a Java 21 GraalVM installation with
Native Image and build the no-fallback native executable:

    sdk use java 21.0.2-graalce
    make native-validator

The resulting Linux executable is:

    build/maintained/mundanereq-validate

The executable need not be committed. `make validator-verify` rebuilds it and
runs the maintained JVM/native evidence set.

## 3. Invocation

The supported forms are:

    mundanereq-validate [--] FILE_OR_DIRECTORY...
    mundanereq-validate --help
    mundanereq-validate --version

`--` ends option recognition. There is no implicit current-directory input.
At least one file or directory must be named.

Each explicit regular file is selected regardless of its suffix. Directory
inputs are traversed recursively; regular files ending in `.mreq` are selected
and `.git` subdirectories are skipped. Symbolic links are not followed.
Repeated paths that normalize to the same absolute path select one file.

The selected files together form one semantic source set. Identity uniqueness
and relationship-target existence are therefore checked across file
boundaries.

## 4. Successful output

A conforming selection writes one summary line to standard output. For one
selected file the form is:

    Validated R requirements and D decomposition relationships from 1 file as mundanereq-source-0.2.

For any other file count the form is:

    Validated R requirements and D decomposition relationships from F files as mundanereq-source-0.2.

`R`, `D`, and `F` are decimal counts. No diagnostics are written to standard
error.

`--help` writes usage to standard output. `--version` writes the tool trial
version and source contract to standard output.

## 5. Diagnostics

Source and input diagnostics are written one per line to standard error in
deterministic source order:

    file:line:column: category: message

Line and column are the one-based coordinates defined by Source Language
Specification 0.2 Clause 6.4: they count Unicode scalar values except for its
raw-byte rule when malformed UTF-8 prevents decoding. `category` is a concise
diagnostic classification intended for humans. Category values and explanatory
message wording remain provisional and are not a machine-readable
compatibility interface.

When any diagnostic is present, the tool writes no success summary. Authors
should edit the ordinary source at the reported location and invoke the tool
again; no repair or repository mutation is performed automatically.

## 6. Exit status

- `0`: the selected source conforms, or `--help` or `--version` completed.
- `1`: selected source was readable but did not conform to the source contract.
- `2`: invocation or input failure prevented validation, including no selected
  source files or an unavailable input.

If source and operational diagnostics occur together, status `2` takes
precedence.

## 7. Compatibility boundaries

Three compatibility surfaces are intentionally separate:

1. The normative source syntax and semantics are defined by the
   [Source Language Specification 0.2](0005-mundanereq-source-language-0.2.md),
   not by this command.
2. This document defines the maintained `trial-0.1` command-line behavior. A
   later trial may revise it explicitly; no stable 1.0 CLI promise is made.
3. Java packages, classes, records, and methods are implementation details.
   They have no compatibility guarantee and are not a published library API.

The Git tag identifies source, tests, and build instructions, not a portable
binary distribution. Native behavior is supported only where the documented
build and verification complete successfully.

## 8. Deliberate omissions

This trial does not:

- discover source implicitly from repository configuration;
- format, rewrite, repair, or render source;
- enforce organization-specific policy or approval state;
- answer traceability queries;
- read or write ReqIF;
- emit JSON, SARIF, or another machine-readable diagnostic form;
- maintain an index, cache, database, daemon, or server; or
- promise multi-platform prebuilt binaries.

## 9. Evidence

[Research 0014](../research/0014-validator-verification.md) records the
conformance, corpus, invocation, repair, diagnostic-volume, and JVM/native
agreement evidence for this release. `make validator-verify` is the executable
acceptance gate associated with that record.
