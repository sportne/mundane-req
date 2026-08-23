# Task TC-0805: Test Glossary and Formal-Symbol Artifacts

Status: Conditional

Roadmap stage: 8

Type: Model-pressure experiment

Depends on: TC-0603 and demonstrated vocabulary ambiguity

Unlocks: A vocabulary and symbol model decision

## Question

Do shared domain terms or mathematical symbols need separately identified human-readable definitions for authoring and analysis?

## Outcome

A bounded corpus compares ordinary prose definitions, a companion glossary or symbol table, and tool-only conventions.

## Work

- Select repeated terms and symbols whose ambiguity affects requirement interpretation.
- Create standalone candidate definitions with stable references only where needed.
- Exercise rename, changed definition, undefined use, and symbol reuse.
- Keep LaTeX payloads opaque while testing whether symbol metadata adds value.
- Compare raw readability, Git diffs, and possible lint queries.

## Acceptance evidence

- Every structured definition answers a concrete ambiguity or analysis need.
- The candidate remains readable without rendering.
- No executable mathematical semantics are implied.
- The result distinguishes author guidance from normative shared definition.

## Out of scope

- A theorem prover.
- Macro expansion.
- A general ontology or terminology server.

## Completion decision

Adopt a companion artifact only if explicit shared identity materially improves the tested workflow over prose and project convention.

## References

- [Roadmap glossary study](0001-initial-roadmap.md#glossary-and-formal-symbols)
- [Mathematical content model](../specification/0002-minimum-source-language-and-model.md#mathematical-content)
