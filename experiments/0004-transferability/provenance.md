# Experiment 0004 Corpus Provenance

## Upstream work

- Project: NASA FRET (Formal Requirements Elicitation Tool)
- Repository: <https://github.com/NASA-SW-VnV/fret>
- Upstream revision: `cf0f913cc15c41705e803593297e89cd6056b839`
- Selected file: `caseStudies/LiftPlusCruise/LPC_mini_reqts_and_vars.json`
- Pinned source: <https://github.com/NASA-SW-VnV/fret/blob/cf0f913cc15c41705e803593297e89cd6056b839/caseStudies/LiftPlusCruise/LPC_mini_reqts_and_vars.json>
- Case-study instructions and references: <https://github.com/NASA-SW-VnV/fret/blob/cf0f913cc15c41705e803593297e89cd6056b839/caseStudies/LiftPlusCruise/LPC_instructions.md>
- Authors named by the upstream case-study references: Thomas Pressburger, Andreas Katis, Aaron Dutle, and Anastasia Mavridou
- Upstream license: Apache License 2.0
- Local license copy: [LICENSES/Apache-2.0.txt](LICENSES/Apache-2.0.txt)

The upstream repository has no root `NOTICE` file at the selected revision, and no separate license or notice file appears beneath `caseStudies/`. This experiment therefore applies the repository's Apache License 2.0 to the adapted requirement records while retaining this attribution and a copy of the license.

## Selection

The selected source contains 19 requirements and four variable declarations for the FRET project `LPC_mini`. All 19 requirements are included; this avoids selecting only records that happen to fit the mundane-req model.

The larger `LPC_full` project is excluded to keep the study bounded. Other FRET case studies are excluded because one independently authored corpus is sufficient for this transfer decision and because some draw on third-party challenge material with less direct provenance.

## Changes made by mundane-req

The `.mreq` files are modified/adapted files, not verbatim copies of the upstream JSON:

1. JSON and FRET-generated semantic fields were removed.
2. The upstream `reqid` and `fulltext` values were retained in the initial encoding.
3. Human-facing titles were added because the upstream records have no separate title field.
4. The FRET component name `vehicle` was represented as the allocation label.
5. A pinned source locator was added to every record.
6. Records were grouped into two subject files; file placement has no model semantics.
7. Statements were line-wrapped to fit the experiment source form. Mundane-req prose folding makes those wraps non-semantic.

No claim is made that the adaptation can be imported back into FRET without loss. Generated temporal-logic formulas, variable types, analysis settings, and FRET-specific semantic decomposition remain available only in the pinned upstream artifact.

## Reproduction

Retrieve the pinned JSON and select `.requirements[]`. Compare each upstream pair `(.reqid, .fulltext)` with the initial tagged mundane-req encoding after applying the prose-folding rule. Titles, allocation, source locator, file grouping, and line wrapping are declared adaptations.
