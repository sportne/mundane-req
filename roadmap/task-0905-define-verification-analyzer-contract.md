# Task TC-0905: Define the Verification Analyzer Contract

Status: Planned

Roadmap stage: 9

Type: Experiment and decision

Depends on: TC-0802, TC-1003, TC-1103, TC-1203

Unlocks: TC-1204, TC-0904

## Question

What is the smallest stable-enough companion input and analysis contract for
checking verification-plan references, exact revision binding, coverage, and
staleness without defining requirement satisfaction?

## Outcome

A second-generation verification companion experiment compares baseline-binding
granularities, fixes one bounded trial contract, and supplies fixtures and
expected analyses sufficient to implement a separate native analyzer.

## Work

- Use TC-1103's compiled-artifact experiment and TC-1203's import/reference
  contract; define the plan carrier's published fields and compiled fixture
  representation before the maintained linker/analyzer implementations begin.
- Reuse the conceptual separation from Experiment 0017 and the complete plan
  workflow from Experiment 0024.
- Compare whole-source-set binding, per-requirement semantic binding, and Git
  snapshot plus requirement-ID binding against comment-only, unrelated,
  normative, identity, and file-move changes.
- Decide the minimum carrier fields and file-selection behavior needed for
  activity definitions, plans, and coverage. Select the plan authoring format on
  its own evidence: assess the existing experimental TSV carrier and alternatives
  only where authoring or tooling needs justify them. Record that decision
  separately from its compiled interface; requirements YAML supplies no default.
- Specify diagnostics for unknown requirement/activity references, duplicate
  rows, digest mismatch, stale plans, and uncovered in-scope requirements.
- Keep execution, evidence storage, results, safety assessment, generalized
  policy, and satisfaction semantics outside the contract.
- Freeze fixtures and expected human-readable queries before implementation.

## Acceptance evidence

- The contract defines planned coverage, possible impact, and review staleness
  separately. Missing or partially linked inputs cannot yield a complete coverage
  result, and every result identifies its authored assertion and resolved inputs.
- Any selected digest describes a particular compiled form or revision, changes
  with relevant content edits, and has documented inputs/canonicalization. The
  human-authored requirement ID remains identity; no digest enters authored .mreq
  files. Reuse this binding decision rather than creating a machine-identity card.
- The selected binding detects changed covered requirement revisions without
  invalidating unrelated assertions merely because another source record or
  nonsemantic comment changed.
- Baseline A, Baseline B, and at least one negative fixture have reproducible
  expected results.
- The contract identifies authoritative inputs and disposable outputs without a
  database, daemon, or hidden repository state.
- The plan-format decision explains its authoring/workflow evidence and maps its
  selected source through an adapter to the published compiled interface. It leaves
  safety, test-evidence and other artifact authoring formats unresolved.
- The future executable has one focused responsibility and consumes the published
  compiled interfaces without requiring another artifact's source parser. Shared
  implementation is justified separately by the selected contracts.

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

- [TC-1103](closed/task-1103-test-compilation-linking-and-rebuilds.md)
- [TC-1203](task-1203-define-import-and-reference-contracts.md)
- [TC-1204](task-1204-implement-bounded-artifact-linking.md)
- [Experiment 0017](../experiments/0017-verification-evidence/README.md)
- [Experiment 0024](../experiments/0024-vaccine-monitoring-pilot/assessment.md)
- [Verification companion decision](../research/0024-verification-companion-decision.md)
- [Pilot decision](../research/0032-end-to-end-pilot-decision.md)
- [TC-0904](task-0904-implement-the-selected-ecosystem-tool.md)

## Compatibility and affected components

The experimental TSV carrier remains evidence, not an adopted source contract.
Likely components are a verification-plan adapter/compiler, published plan
fixtures, binding rules, and analysis diagnostics. Existing requirement fields
and tools remain usable independently.

## Planning refinement

Reconciled on 2026-09-04: retain the pilot-selected verification scope, but make
the compilation experiment and reference decision prerequisites. Status changes
from Ready to Planned because those decisions are unresolved. The plan contract
now supplies the linker's first companion fixture interface; its implementation
remains TC-0904. Stop or narrow if the selected carrier needs generalized policy
or satisfaction semantics to answer coverage and staleness.

Keep verification-plan notation an independent decision. Replace the ambiguous
shared-interpreter allowance with consumption through compiled interfaces and a
separately selected plan adapter. Status and dependencies remain unchanged.
