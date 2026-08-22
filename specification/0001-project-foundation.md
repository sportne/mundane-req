# Specification 0001: Project Foundation

Status: Draft

## Purpose

`mundanereq` is an experiment in applying a software-toolchain model to requirements engineering.

The central premise is that requirements should have a durable, human-readable textual source representation that is suitable for normal Git version control and ordinary code-review workflows. Requirements-specific tools may parse, validate, analyze, render, transform, export, or otherwise operate on that source, but the source repository remains authoritative.

## Core model

The project should treat requirements as independent model objects rather than as paragraphs whose identity depends on a document, section, file, or line number.

Documents, specifications, matrices, reports, and other presentations should be views over the requirements model or generated artifacts derived from it.

## Initial design goals

The requirements source format should eventually support, at minimum:

1. Stable requirement identity.
2. Human-facing requirement identifiers.
3. Requirement title and normative statement.
4. Optional rationale.
5. Structured attributes such as verification method or status-like engineering metadata where appropriate.
6. Explicit typed relationships between requirements.
7. Human readability without specialized tooling.
8. Clean and comprehensible ordinary Git diffs.
9. Deterministic parsing into a structured semantic representation.
10. Validation by independent tooling.

## Non-goals for the initial investigation

The first phase does not attempt to provide:

- SysML or general systems-modeling support.
- CAD, drawing, or other geometric artifact management.
- A hosted requirements database.
- A custom review queue.
- A mandatory graphical editor.
- A mandatory custom diff renderer.
- Formal tool qualification or certification support.
- A complete requirements-management product suite.

## Architectural principles

### Source is authoritative

The human-readable source files and their Git history are the durable project artifacts. Tool-specific databases and indexes must be treated as derived state.

### Git is version control, not requirements analysis

Git is responsible for recording versions, branches, commits, merges, and history. It is not responsible for determining whether requirements are syntactically valid, semantically consistent, well written, properly traced, or verified.

Those responsibilities belong to separate tools, analogous to parsers, compilers, linters, static analyzers, test frameworks, and documentation generators in software development.

### Specialized tooling is optional enhancement

A project-specific renderer, semantic diff viewer, IDE integration, or traceability browser may substantially improve usability, but raw source must remain useful without them.

### Prefer iteration over speculative completeness

The project should establish a minimal usable source representation, exercise it in real Git workflows, and add tooling in response to observed friction rather than designing a complete ecosystem before use.

## Open questions

- What syntax best balances readability, structure, and clean Git diffs?
- Should machine identity and human-facing IDs be distinct?
- Which metadata belongs inside a requirement versus in relationships or external evidence?
- How should typed relationships be represented?
- How should collections or document views be represented without making document position part of identity?
- Which existing open-source projects provide useful prior art without becoming dependencies?
- What is the smallest parser and validator needed to test the source-language concept?
