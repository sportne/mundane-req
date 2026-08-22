# Lift-Plus-Cruise Verification Plan Exercise

Status: Planned activities only; no execution evidence

Applies to requirements baseline: `experiment-0004-baseline-a`

## Purpose

This adjacent artifact tests whether useful verification planning can remain revision-scoped without adding workflow state or verification fields to each requirement. It is part of the experiment, not part of the `.mreq` language.

Coverage here means that a requirement is assigned to at least one planned activity. It does not mean that the requirement, design, or implementation has passed verification.

## Planned activities

| Activity | Method | Objective | Expected evidence |
| --- | --- | --- | --- |
| `AN-LPC-REALIZABILITY` | Requirements analysis | Check consistency/realizability of the selected requirement set and diagnose conflicting subsets | Tool version, configuration, selected IDs, result, and diagnostic trace bound to the baseline |
| `AN-LPC-STATE-MODEL` | Formal analysis | Check initial conditions, invariants, and rate constraints against a controlled vehicle state model | Model revision, property mapping, tool version, result, and counterexamples |
| `TST-LPC-INITIALIZATION` | Test | Exercise initial lift mode, airspeed, and hover-control conditions at threshold boundaries | Procedure revision, test configuration, measured results, and pass/fail disposition |
| `TST-LPC-TRANSITIONS` | Test | Exercise transition and stay conditions immediately below, at, and above their airspeed/mode boundaries | Procedure revision, test configuration, time-series results, and pass/fail disposition |
| `TST-LPC-REACHABILITY` | Test | Demonstrate the six- and twelve-tick thrust-borne reachability constraints under declared initial conditions | Procedure revision, tick definition, time-series results, and pass/fail disposition |

`AN-LPC-REALIZABILITY` analyzes the requirements themselves. It is not evidence that a vehicle implementation satisfies them. The other activities would require controlled design or implementation artifacts that are intentionally absent from this corpus.

## Requirement coverage

| Requirement | Planned activities |
| --- | --- |
| `LPC_INIT_LIFT_MODE` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-INITIALIZATION` |
| `LPC_LIFT_MODE` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL` |
| `LPC_INIT_KIAS` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-INITIALIZATION` |
| `LPC_KIAS_KGS` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL` |
| `LPC_KIAS_DERIVATIVE` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL` |
| `LPC_INIT_HOVER_MODE` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-INITIALIZATION` |
| `LPC_KIAS_0` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL` |
| `LPC_REACH_HOVER_06` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-REACHABILITY` |
| `LPC_REACH_HOVER_12` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-REACHABILITY` |
| `LPC_TB_STAY_ON_NEXT` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-TRANSITIONS` |
| `LPC_STB_STAY_ON_NEXT` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-TRANSITIONS` |
| `LPC_SWB_TO_STB` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-TRANSITIONS` |
| `LPC_STB_TO_SWB` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-TRANSITIONS` |
| `LPC_STB_TO_TB` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-TRANSITIONS` |
| `LPC_SWB_TO_WB` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-TRANSITIONS` |
| `LPC_TB_TO_STB` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-TRANSITIONS` |
| `LPC_WB_TO_SWB` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-TRANSITIONS` |
| `LPC_SWB_STAY_ON_NEXT` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-TRANSITIONS` |
| `LPC_WB_STAY_ON_NEXT` | `AN-LPC-REALIZABILITY`, `AN-LPC-STATE-MODEL`, `TST-LPC-TRANSITIONS` |

## Baseline-A coverage result

- Requirements in baseline: 19
- Requirements assigned to at least one planned activity: 19
- Requirements without a planned activity: 0
- Planned activities with no requirement: 0
- Executed activities: 0
- Passed requirements: not determined

The table makes coverage reviewable with ordinary text and Git, but it also reveals a distinct model: verification activities have identity, method, objectives, evidence expectations, and many-to-many relationships to requirement revisions. Treating all of that as scalar fields on each requirement would duplicate activity information and confuse planning with results.
