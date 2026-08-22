# Experiment 0004: Transferability

Status: Completed

Plan date: 2026-08-22

Result date: 2026-08-22

Reproducible result: annotated tag `experiment-0004-result`

## Question

Does the provisional minimum model remain useful when it receives requirements authored for an independent formal-requirements tool, or is it overfitted to the original UAS corpus?

## Selected corpus

The experiment uses all 19 records from the `LPC_mini` project in NASA's FRET Lift-Plus-Cruise case study. The set is small enough to inspect completely but contains state invariants, initial conditions, transitions, next-step behavior, bounded reachability, and a documented realizability conflict.

The exact upstream revision, path, license, adaptations, and exclusions are recorded in [provenance.md](provenance.md). The source language files are adaptations under the upstream Apache License 2.0; they are not relicensed by mundane-req's BSD license.

## Procedure

1. Preserve every upstream requirement ID and normative `fulltext` value in an initial encoding.
2. Add only information required by the minimum mundane-req model:
   - a human-facing title derived from the upstream ID and statement;
   - the upstream FRET component as the allocation label;
   - a pinned source locator naming the upstream commit, file, and record ID.
3. Do not manufacture rationale, decomposition, verification relationships, or hierarchy absent from the selected source.
4. Validate the encoding with the Experiment 0002 native validator and inspect it as standalone text.
5. Establish an initial annotated baseline.
6. Exercise the unresolved pressure cases without adding grammar:
   - verification planning and revision-specific coverage;
   - identifier correction versus replacement;
   - source revision and record locator fidelity;
   - allocation vocabulary;
   - relationship and coverage policy.
7. Record losses, model implications, and the transfer/revise/overfit decision.

## Initial encoding rules

- FRET `reqid` becomes the mundane-req requirement ID.
- FRET `fulltext` becomes the statement, with line wrapping only. No wording is normalized.
- FRET semantic formulas and generated fields are not copied. They are derived interpretations rather than independently authored requirements source.
- FRET `component`/`component_name` becomes the plain allocation label `vehicle`.
- Empty FRET rationale, comments, parent ID, and status values are omitted rather than represented as empty fields.
- The `source` value is an opaque but pinned locator. Its internal punctuation has no language-defined meaning.
- No `decomposes` relationship is inferred from shared variables, state transitions, analysis conflicts, or record order.

## Starting observations

The transfer is deliberately asymmetric. The minimum mundane-req model preserves the independently readable normative records and their provenance, but it does not claim to preserve FRET's temporal-logic interpretation, variable declarations, generated formulas, or realizability-analysis configuration. Those losses matter for interchange; they do not automatically belong in requirements source.

## Results

All 19 selected requirements parse successfully in two files at both annotated baselines. Baseline A preserves the upstream IDs; Baseline B contains one controlled local ID correction and a correspondingly updated verification-plan reference.

The [verification plan](verification-plan.md) exercises baseline-bound planning coverage without adding fields to requirement records. The [transferability review](transferability-review.md) records the mapping, losses, five required pressure cases, and final decisions.

The minimum requirement grammar transfers without change. Verification planning is best treated as a separate relationship model bound to requirement revisions; its syntax remains deferred. ID correction remains visible and reviewable in Git but does not preserve semantic identity across snapshots.

## Deliverables

- a licensed, provenance-pinned 19-requirement encoding;
- an annotated initial baseline;
- a revision-scoped verification-planning and coverage exercise;
- an identifier-correction history and comparison;
- allocation, source-fidelity, relationship-policy, and loss analysis;
- a final transferability disposition;
- narrow specification and roadmap updates justified by the evidence.
