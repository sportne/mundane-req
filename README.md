# mundanereq

**Mundane requirements management.**

`mundanereq` explores a simple idea: requirements should be human-readable source files stored in ordinary Git repositories, with requirements-specific tooling layered on top in the same way compilers, linters, renderers, IDEs, and CI systems are layered on top of software source code.

Its mission is to make requirements durable, human-readable source artifacts that work naturally with ordinary Git workflows and independent, composable tools.

The source representation is the foundation. A requirements file should be understandable and reviewable directly in a text editor or standard Git forge diff without requiring a proprietary application or custom renderer.

## Project principles

- Requirements source is human-readable text.
- Git provides version control, history, branches, immutable snapshots, and merge workflows. Those snapshots can supply baseline mechanics; baseline scope and authority may need conventions or tooling.
- Pull requests / merge requests are valid review mechanisms without requiring a requirements-specific review system.
- Requirements-specific tooling should be additive: parsers, validators, formatters, renderers, exporters, IDE integrations, and semantic diff tools can improve the experience without becoming the authoritative store.
- Requirements are model objects; documents and reports are views or generated artifacts.
- Derived indexes, databases, and rendered outputs should be rebuildable from the source repository.
- Early development should favor small working prototypes and iteration over speculative completeness.

## Repository layout

- `specification/` — project and language specifications.
- `conformance/` — independently runnable examples for provisional source contracts.
- `research/` — surveys, experiments, evidence, and unresolved design questions.
- `experiments/` — concrete source fixtures and recorded experimental results.
- `roadmap/` — development strategy, experiments, and sequencing.
- `src/main/java/` — maintained shared implementation and later tool entry points.
- `src/test/java/` — maintained dependency-free tests.

The current written interface consists of the [provisional 0.2 contract](specification/0006-provisional-0.2-contract.md) and the normative [mundanereq Source Language Specification 0.2](specification/0005-mundanereq-source-language-0.2.md). Version 0.2 adds only [nonsemantic full-line author comments](research/0009-nonsemantic-source-comments.md) to the 0.1 language. The 0.1 contract and standard remain available as the prior version. These are evidence-backed trial contracts, not stable release promises.

The first code was a small [deterministic-interpretation probe](experiments/0002-deterministic-interpretation/README.md) used to test the provisional source language. It remains historical evidence rather than production architecture. The [maintained implementation lineage](research/0011-maintained-implementation-lineage.md) now provides a dependency-free Java 21 source area containing a lossless concrete source representation, shared semantic interpreter, and the first focused product tool. Earlier experiments [transfer the minimum model](experiments/0004-transferability/README.md) to a licensed NASA FRET case-study corpus and add one [focused incoming trace query](experiments/0005-incoming-trace-query/README.md) without changing authoritative source.

A bounded [ReqIF 1.2 interchange experiment](experiments/0006-reqif-interchange/README.md) demonstrates schema-valid semantic self-roundtrips while keeping ReqIF derived and outside the source-language contract. [Experiment 0007](experiments/0007-source-comments/README.md) confirms that 0.2 author comments do not enter that semantic interchange model.

## Maintained build skeleton

Select a Java 21 GraalVM distribution with Native Image, such as the installed
SDKMAN candidate:

    sdk use java 21.0.2-graalce

Then verify the maintained JVM and native build boundary from a clean checkout
with one command:

    make verify

`make test` runs the maintained JVM regression suite. `make native-smoke` builds and runs a
test-only executable with `--no-fallback`. Generated classes and native output
are disposable under `build/maintained`. `make boundary-isolation` builds three
temporary standalone native boundaries and proves that none requires either of
the other executables at runtime. These are architecture checks; product
formatter and trace behavior remains on the roadmap; the validator boundary is
now implemented as the first product tool.

## Validator

`mundanereq-validate` is the first focused maintained tool. Build its standalone
Linux native executable after selecting the GraalVM Java 21 SDKMAN candidate:

    make native-validator

Validate one or more explicit files or directories:

    build/maintained/mundanereq-validate requirements/

The validator implements `mundanereq-source-0.2`, reports diagnostics as
`file:line:column: category: message`, and uses exit status `0` for conforming
source, `1` for source nonconformance, and `2` when invocation or input failure
prevents validation. `--help` and `--version` describe the small current
interface. The command performs no project-policy, formatting, trace, or ReqIF
work.

The [validator trial contract](specification/0007-validator-trial-contract-0.1.md)
defines the complete `trial-0.1` interface, installation procedure,
compatibility boundaries, and known omissions. The Git tag
`validator-trial-0.1` identifies its reproducible source baseline. This is a
maintained trial interface, not a stable 1.0 compatibility promise.

`make validator-verify` runs the complete JVM/native validator evidence set,
including all conformance selections, maintained corpora, source-selection
cases, and representative editor repairs.
