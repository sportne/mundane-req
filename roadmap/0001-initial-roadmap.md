# Roadmap 0001: Initial Investigation

Status: Draft

## Objective

Determine whether a purpose-built, human-readable requirements source format stored in Git provides a useful foundation for requirements engineering before investing in a broader tool ecosystem.

## Phase 1 — Prior art and constraints

Study existing text-first requirements approaches such as StrictDoc and Doorstop for language, data-model, traceability, and workflow ideas without assuming either must be adopted as a dependency.

Capture organizational constraints that may affect dependency selection, licensing, provenance, or distribution.

## Phase 2 — Source-language experiment

Create a small representative corpus of requirements and try a few candidate textual representations.

Evaluate them primarily through ordinary tools:

- text editors,
- `git diff`,
- `git log`,
- branches and merges,
- Bitbucket or GitLab pull/merge requests,
- simple command-line searching.

The source format should be judged first on readability and reviewability without custom rendering.

## Phase 3 — Minimal language definition

Select a minimal syntax and define only the concepts required by the experiment, likely including:

- stable identity,
- human-facing ID,
- title,
- statement,
- rationale,
- a small attribute set,
- typed requirement relationships.

Avoid macros, general metamodeling, execution semantics, plugin systems, and other features without demonstrated need.

## Phase 4 — Parser and validator

Implement the smallest useful toolchain around the source language:

- tokenizer / lexer if needed,
- parser,
- semantic model,
- basic validation,
- clear diagnostics.

Initial validation should focus on structural correctness such as duplicate IDs, missing required fields, dangling references, and invalid relationship forms.

## Phase 5 — Use and learn

Use the format for realistic requirements changes and reviews.

Record where ordinary source and Git workflows work well and where they become awkward. Add tooling only where repeated use demonstrates value.

Likely candidates include:

- formatter,
- renderer,
- traceability queries,
- linter,
- export formats,
- editor integration,
- semantic diff rendering.

## Near-term success criterion

A small requirements set can be authored as plain text, committed to Git, changed on a branch, reviewed using an ordinary Git forge diff, merged, and understood later from the repository alone.
