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
5. Only durable structured attributes demonstrated to belong to a requirement; review and workflow state are not presumed intrinsic.
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

### Record syntax, field content, and rendering are distinct

Plain text is a property of the authoritative source, not a commitment to Markdown or another general-purpose markup language. The syntax that identifies requirement objects, fields, and relationships may be separate from notations embedded within field content.

For example, a mathematical requirement may contain an explicitly delimited LaTeX-style expression whose formatted rendering is derived. The canonical expression, its boundaries, and its interpretation must remain visible in source; rendering must not own the requirement or depend on hidden state.

### Prefer iteration over speculative completeness

The project should establish a minimal usable source representation, exercise it in real Git workflows, and add tooling in response to observed friction rather than designing a complete ecosystem before use.

## Experiment-supported direction

Experiment 0001 provides enough evidence to narrow the next specification phase without freezing a production language. [Research 0007](../research/0007-provisional-source-representation-decision.md) records the representation evidence and rationale. Experiments 0002 and 0003 subsequently confirm deterministic interpretation and sustained use on the small UAS corpus.

The current direction is:

- Continue with the purpose-built, keyword-based requirement record form tested in Candidates A and B.
- Carry the authoritative human-facing ID inside the record.
- Permit one or more complete records per source file while treating filenames, paths, file counts, and record positions as non-semantic.
- Preserve the separation between requirement objects and authored order or composition, without yet selecting a view language.
- Do not use a Markdown document containing authoritative embedded records as the initial storage representation.
- Rely on ordinary Git merging; the experiment does not justify semantic merge machinery.
- Treat Git commits and annotated tags as the initial baseline mechanism rather than adding intrinsic baseline or per-requirement revision fields.
- Keep review status, approval, retirement state, timestamps, and change justification outside requirement records unless later workflows demonstrate durable requirement semantics.
- Treat decomposition completeness as policy analysis rather than universal syntax validity.

These are provisional language and model decisions supported by the experiment. [Specification 0002](0002-minimum-source-language-and-model.md) makes the minimum grammar, discovery, cardinality, validation, and mathematical-content rules precise enough to test; they are not yet a compatibility promise.

## Open questions

- Does correction of a human-facing ID require separate durable machine identity or an explicit continuity mechanism?
- How should verification planning and coverage relate to requirement source and particular repository revisions?
- When do allocation labels need referential identity or a controlled vocabulary?
- Does an opaque external-source value preserve enough locator and revision fidelity on another corpus?
- Which trace-completeness policies are reusable without becoming universal language rules?
- Does the first specification need an authored view at all?
- What constrained mathematical-content profile, if any, should the source language promise?
