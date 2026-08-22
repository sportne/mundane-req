# mundanereq

**Mundane requirements management.**

`mundanereq` explores a simple idea: requirements should be human-readable source files stored in ordinary Git repositories, with requirements-specific tooling layered on top in the same way compilers, linters, renderers, IDEs, and CI systems are layered on top of software source code.

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
- `research/` — surveys, experiments, evidence, and unresolved design questions.
- `experiments/` — concrete source fixtures and recorded experimental results.
- `roadmap/` — development strategy, experiments, and sequencing.

Implementation remains deliberately experimental. The first code is a small [deterministic-interpretation probe](experiments/0002-deterministic-interpretation/README.md) used to test the provisional source language; it is not yet a production tool or architecture commitment. The latest completed study [transfers the minimum model](experiments/0004-transferability/README.md) to a licensed NASA FRET case-study corpus and records where requirements source, formal-tool semantics, and verification planning separate.
