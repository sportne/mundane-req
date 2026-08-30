# Pilot Protocol

Status: Fixed before requirements authoring

Date: 2026-08-30

## Trial question

Can the existing requirement object—identity, title, statement, optional
allocation, rationale, source, and decomposition relationships—carry a realistic
formal-traceability slice while Git and separate companion artifacts own the
remaining workflow facts?

## Procedure

1. Identify exact authoritative public inputs and separate normative product
   constraints from operational guidance and pilot assumptions.
2. Define stakeholders, system boundary, operational contexts, and external
   interfaces before authoring product requirements.
3. Author stakeholder needs, system requirements, and allocated lower-level
   requirements in several nonsemantic files.
4. Use the maintained formatter only in explicit write mode; inspect its diff.
5. Validate the complete declared source set.
6. Exercise direct and transitive trace queries, including change-impact
   navigation.
7. Create a baseline-bound verification plan and contextual safety assessment
   as separate, human-readable artifacts.
8. Record a self-review against the source documents and the pilot's quality
   criteria, then create an annotated Git baseline.
9. Introduce one bounded stakeholder change. Record impact before editing,
   modify source and companion artifacts, inspect the ordinary diff, rerun the
   tools, review, and create a second annotated baseline.
10. Classify observed information by its proper owner: requirement, authored
    relationship, derived relationship, companion artifact, project policy,
    Git/forge workflow, or disposable result.

## Evaluation questions

- Can a reader understand requirement intent, origin, and allocation without a
  specialized renderer?
- Does decomposition express the needed many-to-many higher/lower-level
  reasoning without treating file layout as hierarchy?
- Are one opaque `source` reference and one opaque `allocation` label
  sufficient in this workflow?
- Can verification and contextual safety facts remain outside requirement
  records without making change review obscure?
- Can source-set and baseline scope be stated durably without introducing a
  repository manifest into source-language semantics?
- Does the controlled change produce a useful ordinary Git diff and a
  tractable impact query?
- Which failures are language validity, project policy, requirement quality,
  or workflow completeness?

## Success threshold

The pilot succeeds if both source sets conform, representative traces and
coverage are reproducible, the baseline/change workflow is understandable in
ordinary Git, and any missing capability can be described as a concrete
engineering need rather than a generic request for more metadata.

Success does not imply 1.0 readiness. The experiment lacks independent-human
use and product implementation evidence by design.
