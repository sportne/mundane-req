# Roadmap Task-Card Index

Status: Active backlog

Strategic source: [Roadmap 0001](0001-initial-roadmap.md)

## Purpose

This index decomposes the strategic roadmap into bounded, independently reviewable task cards. A task card describes one outcome and the evidence needed to complete it. The cards are execution aids, not additions to the source-language specification.

## Status vocabulary

- **Ready:** known dependencies are complete; work may begin.
- **Planned:** accepted work with incomplete dependencies.
- **Conditional:** perform only when its stated decision gate or prerequisite justifies it.
- **In progress:** actively being executed.
- **Complete:** acceptance evidence and the completion decision are recorded.
- **Superseded:** replaced by a later card or roadmap decision, with the replacement identified.

Only a completed card's recorded evidence, specification changes, or decision records are durable project results. Checking boxes without that evidence does not complete a card.

## Dependency shape

```text
Stage 1 exact 0.2 baseline
    -> Stage 2 shared Java foundation
    -> Stage 3 validator ---------+
    -> Stage 4 formatter ---------+-> Stage 6 integrated toolchain trial
    -> Stage 5 trace tool --------+
                                      -> Stage 7 operational evidence
                                      -> Stage 8 model-pressure experiments
                                      -> Stage 9 selected ecosystem tools
                                      -> Stage 10 1.0 decision
```

Stages 3, 4, and 5 share Stage 2 foundations but produce separate GraalVM native executables. Stage 8 is a research track: its cards may be reprioritized after Stage 6 when a concrete workflow makes one question urgent.

## Active implementation sequence

| Card | Outcome | Status | Depends on |
| --- | --- | --- | --- |
| [TC-0101](task-0101-strengthen-unicode-conformance-fixtures.md) | Unicode edge-case fixtures | Complete | — |
| [TC-0102](task-0102-correct-reference-parser-conformance.md) | Known parser deviations repaired | Complete | TC-0101 |
| [TC-0103](task-0103-audit-the-0.2-conformance-baseline.md) | Audited 0.2 implementation baseline | Complete | TC-0101, TC-0102 |
| [TC-0104](task-0104-select-the-maintained-implementation-lineage.md) | Probe-evolution decision | Complete | TC-0103 |
| [TC-0201](task-0201-establish-the-maintained-java-project.md) | Maintained Java/GraalVM project skeleton | Complete | TC-0104 |
| [TC-0202](task-0202-design-the-shared-source-representation.md) | Shared physical and concrete source model | Complete | TC-0201 |
| [TC-0203](task-0203-extract-the-semantic-parser-and-diagnostics.md) | Shared semantic parser, discovery, and diagnostics | Complete | TC-0202 |
| [TC-0204](task-0204-port-tests-and-prove-native-tool-boundaries.md) | Regression coverage and three native boundaries | Complete | TC-0203 |
| [TC-0301](task-0301-implement-the-validator-executable.md) | `mundanereq-validate` implementation | Complete | TC-0204 |
| [TC-0302](task-0302-verify-validator-behavior.md) | Validator conformance and CI evidence | Complete | TC-0301 |
| [TC-0303](task-0303-publish-the-validator-trial-contract.md) | Maintained validator trial release | Complete | TC-0302 |
| [TC-0401](task-0401-run-the-formatting-policy-experiment.md) | Selected conservative formatting policy | Complete | TC-0203 |
| [TC-0402](task-0402-implement-the-formatter-executable.md) | `mundanereq-format` implementation | Complete | TC-0401, TC-0204 |
| [TC-0403](task-0403-verify-formatter-safety-properties.md) | Semantic-preservation and idempotence evidence | Complete | TC-0402 |
| [TC-0404](task-0404-publish-the-formatter-trial-contract.md) | Maintained formatter trial release | Complete | TC-0403 |
| [TC-0501](task-0501-define-the-first-trace-interface.md) | Bounded trace questions and output semantics | Planned | TC-0203 |
| [TC-0502](task-0502-implement-the-trace-executable.md) | `mundanereq-trace` implementation | Planned | TC-0204, TC-0501 |
| [TC-0503](task-0503-verify-trace-graph-behavior.md) | Graph and workflow evidence | Planned | TC-0502 |
| [TC-0504](task-0504-publish-the-trace-trial-contract.md) | Maintained trace trial release | Planned | TC-0503 |
| [TC-0601](task-0601-package-and-document-the-native-suite.md) | Installable, independently documented native tools | Planned | TC-0303, TC-0404, TC-0504 |
| [TC-0602](task-0602-create-the-clean-checkout-ci-workflow.md) | Reproducible example Git/CI workflow | Planned | TC-0601 |
| [TC-0603](task-0603-run-the-integrated-toolchain-trial.md) | First toolchain decision record | Planned | TC-0602 |

## Operational-evidence sequence

| Card | Outcome | Status | Depends on |
| --- | --- | --- | --- |
| [TC-0701](task-0701-select-a-larger-corpus-and-trial-protocol.md) | Licensed corpus and controlled trial plan | Planned | TC-0603 |
| [TC-0702](task-0702-run-the-independent-author-trial.md) | Independent-author usability evidence | Planned | TC-0701 |
| [TC-0703](task-0703-run-the-multi-author-and-layout-trial.md) | Concurrency and file-granularity evidence | Planned | TC-0701 |
| [TC-0704](task-0704-measure-operational-scale.md) | Operational-scale measurements | Planned | TC-0702, TC-0703 |
| [TC-0705](task-0705-obtain-independent-conformance-evidence.md) | Independent interpretation evidence | Planned | TC-0103, TC-0303 |

## Model-pressure research cards

| Card | Outcome | Status | Depends on |
| --- | --- | --- | --- |
| [TC-0801](task-0801-test-identity-continuity.md) | ID-correction model decision | Conditional | TC-0603 |
| [TC-0802](task-0802-model-verification-planning-and-evidence.md) | Verification companion-artifact decision | Conditional | TC-0603 |
| [TC-0803](task-0803-test-safety-classification-ownership.md) | Safety classification ownership decision | Conditional | TC-0603 |
| [TC-0804](task-0804-test-allocation-and-controlled-vocabulary.md) | Allocation model decision | Conditional | TC-0603 |
| [TC-0805](task-0805-test-glossary-and-formal-symbol-artifacts.md) | Vocabulary and symbol model decision | Conditional | TC-0603 |
| [TC-0806](task-0806-test-reusable-trace-policies.md) | Language-versus-policy decision | Conditional | TC-0603 |
| [TC-0807](task-0807-test-authored-views-and-specifications.md) | View-language decision | Conditional | TC-0603 |

These cards are not assumed to produce syntax. Each must first determine whether the need belongs to requirement content, a relationship, a companion artifact, project policy, Git/forge workflow, or a derived tool.

## Ecosystem and stability cards

| Card | Outcome | Status | Depends on |
| --- | --- | --- | --- |
| [TC-0901](task-0901-prioritize-the-next-ecosystem-tool.md) | Evidence-backed next-tool selection | Conditional | TC-0603, TC-0704 |
| [TC-0902](task-0902-run-an-independent-reqif-roundtrip.md) | Cross-tool ReqIF fidelity evidence | Conditional | TC-0603 |
| [TC-0903](task-0903-run-a-derived-presentation-experiment.md) | Rendering/view evidence | Conditional | TC-0603, TC-0807 |
| [TC-0904](task-0904-implement-the-selected-ecosystem-tool.md) | One additional independent tool | Conditional | TC-0901 |
| [TC-1001](task-1001-audit-readiness-for-1.0.md) | 1.0 evidence and gap audit | Conditional | TC-0704, TC-0705, selected TC-08xx and TC-09xx cards |
| [TC-1002](task-1002-define-compatibility-and-publish-or-defer-1.0.md) | Explicit 1.0 publication or deferral | Conditional | TC-1001 |

## Updating cards

When beginning a card, change its status here and in the card in the same commit. When completing it:

1. record the evidence and resulting decision in the repository;
2. update affected specifications, experiments, or roadmap text;
3. mark the card complete and link its result;
4. make newly unblocked cards Ready;
5. revise dependencies rather than performing work that the evidence no longer justifies.

Use the [task-card template](task-card-template.md) when a roadmap decision creates another bounded task.
