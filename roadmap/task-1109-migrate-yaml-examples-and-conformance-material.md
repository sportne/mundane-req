# Task TC-1109: Migrate YAML Examples and Conformance Material

Status: Planned

Roadmap stage: 11

Type: Tooling and testing

Depends on: TC-1107, TC-1108

Unlocks: documented YAML authoring and reproducible contributor workflows

## Question

A parser alone leaves authors with custom examples and no safe way to carry their
requirements into the selected YAML profile.

## Outcome

A bounded migration tool, representative converted examples and conformance/docs
make the selected authoring workflow reviewable and reproducible.

## Work

- Implement dry-run/separate-output conversion under TC-1106's migration contract,
  preserving IDs, optional values, paragraphs/math, references and documented
  comment placement; reject unsupported cases rather than guessing.
- Compare whole-source-set semantics before/after conversion and exercise output
  collisions, interruption and invalid input without overwriting originals blindly.
- Convert selected maintained examples and dogfooding inputs only after reviewing
  their paired semantic/comment evidence; update selection manifests deliberately.
- Publish language-reference examples, migration notes and explicit selectors.
  Preserve historical versioned fixtures and reproduction instructions.
- Add golden and bounded generated/mutated edge cases for conversion, formatting,
  no-schema projects and temporary coexistence only if selected.

## Acceptance evidence

- Reproducible conversion records show unchanged authored IDs, semantic values and
  links; every supported comment position has a checked expectation.
- One/many-record and split/combined-file projects pass source-set equivalence and
  formatter checks; deliberate conversion failures leave original input intact.
- Current documentation commands execute against maintained tools. Conformance
  inventories include new source sets without disguising invalid inputs.
- Historical 0.1/0.2 evidence remains reproducible; migration/coexistence behavior
  and any retirement conditions match TC-1106. Authoritative source is unambiguous.
- The repository verification command passes with recorded environment and results.

## Out of scope

No automatic migration of external user projects, new attribute semantics,
permanent second authoring system or release work.

## Compatibility, affected components and completion decision

Likely components: migration executable/experiment promoted by decision, examples,
conformance fixtures/manifests, source reference and contributor documentation.
Stop on altered normative text, lost comments or unresolved source authority; keep
conversion reviewable and preserve originals until verified.

## References

- [Roadmap](0001-initial-roadmap.md)
- [Representation decision](../research/0033-yaml-source-representation-decision.md)
- [Comparison and specification outline](../experiments/0025-yaml-source-comparison/README.md)
- [TC-1107](task-1107-interpret-and-validate-yaml-requirement-source.md)
- [TC-1108](task-1108-format-yaml-source-without-content-loss.md)
