# Task TC-1501: Extend Artifact Workflow Regression Corpora

Status: Planned

Roadmap stage: 15

Type: Verification

Depends on: TC-0903

Unlocks: follow-up work selected by the completion decision

## Question

Does the first complete artifact workflow remain reproducible under realistic
source changes and adversarial inputs beyond its selecting experiment?

## Outcome

A maintained corpus and bounded automated checks exercise requirements compilation,
linking, verification analysis, and reporting through an edit/rebuild cycle.

## Work

- Reuse the pilot and existing small/medium corpora; add only missing cases with
  recorded provenance, source-set boundaries, licenses, and expected outcomes.
- Extend golden syntax, semantics, diagnostics, formatting, linked-reference, and
  report fixtures across both existing source contracts and selected artifact versions.
- Add bounded seeded generation of valid/invalid inputs and targeted parser,
  validator, and resolver mutation checks, with replayable seeds and minimized cases.
- Extend existing formatter idempotence and parse-format-parse coverage; include
  schema-free projects and later schema fixtures only after a schema implementation.
- Exercise pinned/current imports, unrelated/comment/normative edits, file moves,
  ID corrections, ambiguous scopes, invalid imports, and context-specific plans.
- Add optional bounded specification dogfooding where the prose maps faithfully to
  requirements; retain original normative documents as authority.

## Acceptance evidence

- A corpus inventory identifies each source set, expected outputs, and commands;
  a clean build reproduces the selected integration reports and change results.
- Seeded tests have documented runtime limits and reproduce failures from recorded
  seeds; targeted non-equivalent mutations are detected or explained with follow-up.
- Every cross-artifact result links to source assertions and input provenance.
- Existing 0.1/0.2 semantic and formatting fixtures remain covered; unknown artifact
  versions and intentionally incompatible changes have explicit expectations.
- Regression tests from TC-1401 through TC-1403 remain in their owning suites.
  The new matrix checks observable behavior rather than internal layout.

## Out of scope

- Replacing tests required within implementation cards, open-ended field trials,
  commissioning another parser, or claiming independent-tool ReqIF compatibility.

## Compatibility and affected components

Components: maintained tests, corpus inventory, conformance and integration
fixtures, bounded generators, and build targets. TC-0902 retains external ReqIF
evidence; existing self-roundtrip evidence is not relabeled.

## Completion decision

Complete when the bounded inventory and deterministic replay commands pass.
Reduce corpus or generator breadth if checks cannot stay repeatable; record
unsupported cases rather than inventing compatibility or participant evidence.

## References

- [Roadmap](0001-initial-roadmap.md)
- [TC-0903](task-0903-run-a-derived-presentation-experiment.md)
- [TC-0902](task-0902-run-an-independent-reqif-roundtrip.md)
- [Formatter verification](../src/test/java/mundanereq/cli/FormatterVerificationTest.java)
