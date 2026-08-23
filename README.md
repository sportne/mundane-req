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

The current written interface consists of the [provisional 0.1 contract](specification/0003-provisional-0.1-contract.md) and the normative [mundanereq Source Language Specification 0.1](specification/0004-mundanereq-source-language-0.1.md). Together they form an evidence-backed trial contract, not a stable release promise.

Implementation remains deliberately experimental. The first code is a small [deterministic-interpretation probe](experiments/0002-deterministic-interpretation/README.md) used to test the provisional source language; it is not yet a production architecture commitment. Later experiments [transfer the minimum model](experiments/0004-transferability/README.md) to a licensed NASA FRET case-study corpus and add one [focused incoming trace query](experiments/0005-incoming-trace-query/README.md) without changing authoritative source.

A bounded [ReqIF 1.2 interchange experiment](experiments/0006-reqif-interchange/README.md) demonstrates schema-valid semantic self-roundtrips while keeping ReqIF derived and outside the provisional 0.1 source contract.
