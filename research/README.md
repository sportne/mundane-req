# Research and Decision Index

This directory contains surveys, design explorations, verification records,
and decision records. Research explains evidence and tradeoffs; it is not
normative source-language text unless a specification explicitly incorporates
the result.

For the current project position, begin with the [source 1.0 readiness
audit](0031-source-1.0-readiness-audit.md), then follow its evidence links.

## Foundation and representation

| Record | Purpose or outcome |
| --- | --- |
| [0001 — Requirements-management practice survey](0001-requirements-management-practice-survey.md) | Separates industry capabilities, workflows, engineering needs, and infrastructure responsibilities. |
| [0002 — Dronology-informed source experiment](0002-dronology-source-experiment.md) | Evaluates an available requirements corpus and its suitability for representation work. |
| [0003 — Representation prior art](0003-representation-prior-art.md) | Compares StrictDoc, Doorstop, ReqIF, markup, and language-tooling lessons. |
| [0004 — UAS semantic corpus](0004-uas-semantic-corpus.md) | Defines the bounded semantic corpus used by early representation trials. |
| [0005 — Record-syntax sketches](0005-purpose-built-record-syntax-sketches.md) | Explores purpose-built non-Markdown record forms. |
| [0006 — Non-Markdown view notation](0006-non-markdown-view-notation.md) | Records the early view experiment without making it authoritative source. |
| [0007 — Source-representation decision](0007-provisional-source-representation-decision.md) | Selects complete keyword records, IDs carried inside records, and nonsemantic file boundaries. |
| [0008 — Language formalization review](0008-source-language-formalization-review.md) | Identifies requirements for a complete implementation-independent standard. |
| [0009 — Nonsemantic source comments](0009-nonsemantic-source-comments.md) | Selects full-line author comments as the sole 0.2 language addition. |
| [0010 — 0.2 conformance audit](0010-source-0.2-conformance-audit.md) | Audits normative rules, fixtures, and implementation behavior. |

## Maintained implementation and tools

| Record | Purpose or outcome |
| --- | --- |
| [0011 — Maintained implementation lineage](0011-maintained-implementation-lineage.md) | Selects dependency-free Java 21 and keeps Java APIs internal. |
| [0012 — Shared source representation](0012-shared-source-representation.md) | Separates lossless concrete source from semantic requirement values. |
| [0013 — Interpreter migration closeout](0013-maintained-interpreter-migration-closeout.md) | Proves maintained and historical interpretations agree on the selected corpus. |
| [0014 — Validator verification](0014-validator-verification.md) | Records JVM/native, conformance, diagnostic, and repair evidence. |
| [0015 — Formatter verification](0015-formatter-verification.md) | Records preservation and idempotence over all 29 maintained source sets and 60 files. |
| [0016 — First trace interface](0016-first-trace-interface.md) | Selects four bounded decomposition-navigation questions. |
| [0017 — Native suite packaging](0017-native-suite-packaging.md) | Defines independent executable packaging and the Linux ABI boundary. |
| [0018 — Clean-checkout CI workflow](0018-clean-checkout-ci-workflow.md) | Defines reproducible independent CI steps for the tool suite. |

## Operational and independent evidence

| Record | Purpose or outcome |
| --- | --- |
| [0019 — Operational corpus selection](0019-operational-corpus-selection.md) | Selects and freezes the larger controlled corpus and protocol. |
| [0020 — Multi-author layout decision](0020-multi-author-layout-decision.md) | Retains nonsemantic file granularity after concurrent-change trials. |
| [0021 — Operational scale decision](0021-operational-scale-decision.md) | Records bounded scale observations without inventing support thresholds. |
| [0022 — Independent conformance decision](0022-independent-conformance-decision.md) | Records agreement by an independent implementation and allowed diagnostic differences. |
| [0026 — Diagnostic presentation decision](0026-diagnostic-presentation-decision.md) | Keeps unlimited human diagnostics and defers bounded or machine output pending a consumer. |

## Model-pressure decisions

| Record | Current disposition |
| --- | --- |
| [0023 — Identity continuity](0023-identity-continuity-decision.md) | Retain human ID as sole identity until an external consumer needs pre-exchanged continuity. |
| [0024 — Verification companion](0024-verification-companion-decision.md) | Separate activity, plan, coverage, execution, evidence, and result; defer stable carrier and satisfaction policy. |
| [0025 — Safety classification ownership](0025-safety-classification-ownership-decision.md) | Treat contextual classification as a baseline-bound assessment assertion. |
| [0027 — Allocation model](0027-allocation-model-decision.md) | Retain an opaque optional label and project-policy vocabulary. |
| [0028 — Glossary and formal symbols](0028-glossary-and-symbol-decision.md) | Retain prose, search, and adjacent local definitions; defer companions. |
| [0029 — Trace policy](0029-trace-policy-decision.md) | Keep coverage and cycle policy separate from source conformance. |

## Roadmap decisions

| Record | Outcome |
| --- | --- |
| [0030 — Next ecosystem tool prioritization](0030-next-ecosystem-tool-prioritization.md) | Selects no fourth tool without a demonstrated consumer or stable companion model. |
| [0031 — Source 1.0 readiness audit](0031-source-1.0-readiness-audit.md) | Recommends a conditional source-only stability decision, identifies evidence gaps, and records the resolved verification blocker. |
