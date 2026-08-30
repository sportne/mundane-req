# Task TC-0905: Define the Verification Analyzer Contract

Status: Ready

Roadmap stage: 9

Type: Experiment and decision

Depends on: TC-0802, TC-1003

Unlocks: TC-0904

## Question

What is the smallest stable-enough companion input and analysis contract for
checking verification-plan references, exact revision binding, coverage, and
staleness without defining requirement satisfaction?

## Outcome

A second-generation verification companion experiment compares baseline-binding
granularities, fixes one bounded trial contract, and supplies fixtures and
expected analyses sufficient to implement a separate native analyzer.

## Work

- Reuse the conceptual separation from Experiment 0017 and the complete plan
  workflow from Experiment 0024.
- Compare whole-source-set binding, per-requirement semantic binding, and Git
  snapshot plus requirement-ID binding against comment-only, unrelated,
  normative, identity, and file-move changes.
- Decide the minimum carrier fields and file-selection behavior needed for
  activity definitions, plans, and coverage.
- Specify diagnostics for unknown requirement/activity references, duplicate
  rows, digest mismatch, stale plans, and uncovered in-scope requirements.
- Keep execution, evidence storage, results, safety assessment, generalized
  policy, and satisfaction semantics outside the contract.
- Freeze fixtures and expected human-readable queries before implementation.

## Acceptance evidence

- The selected binding detects changed covered requirement revisions without
  invalidating unrelated assertions merely because another source record or
  nonsemantic comment changed.
- Baseline A, Baseline B, and at least one negative fixture have reproducible
  expected results.
- The contract identifies authoritative inputs and disposable outputs without a
  database, daemon, or hidden repository state.
- The future executable has one focused responsibility and can reuse the shared
  source interpreter without depending on another executable.

## Out of scope

- Implementing the analyzer.
- Declaring requirements satisfied or certified.
- Executing tests or storing product evidence.
- A generalized relationship, metadata, or policy language.

## Completion decision

Unlock TC-0904 only if one small contract survives the compared change cases.
Otherwise retain the manual companion workflow and record why the carrier is
not ready.

## References

- [Experiment 0017](../experiments/0017-verification-evidence/README.md)
- [Experiment 0024](../experiments/0024-vaccine-monitoring-pilot/assessment.md)
- [Verification companion decision](../research/0024-verification-companion-decision.md)
- [Pilot decision](../research/0032-end-to-end-pilot-decision.md)
- [TC-0904](task-0904-implement-the-selected-ecosystem-tool.md)
