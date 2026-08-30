# Specification Index

This directory separates normative source-language standards, provisional
contract and compatibility policy, nonnormative design rationale, and
separately versioned tool interfaces.

The current authoritative source contract is `mundanereq-source-0.2`. It is
provisional. The [source 1.0 readiness
audit](../research/0031-source-1.0-readiness-audit.md) recommends considering a
no-feature, semantics-identical stable successor, but TC-1002 has not made that
publication decision.

## Recommended reading order

1. [Project foundation](0001-project-foundation.md) for mission, scope, and
   product philosophy.
2. [Minimum source language and model](0002-minimum-source-language-and-model.md)
   for design rationale and model-pressure dispositions.
3. [Source Language Specification 0.2](0005-mundanereq-source-language-0.2.md)
   for normative language behavior.
4. [Provisional 0.2 Contract](0006-provisional-0.2-contract.md) for version,
   compatibility, exclusions, and trial expectations.
5. The relevant tool contract for a maintained command-line interface.

## Documents

| Document | Role | Current status |
| --- | --- | --- |
| [0001 — Project Foundation](0001-project-foundation.md) | Mission, scope, principles, and current dispositions | Living, nonnormative |
| [0002 — Minimum Source Language and Model](0002-minimum-source-language-and-model.md) | Design record and rationale for the current model | Current, nonnormative |
| [0003 — Provisional 0.1 Contract](0003-provisional-0.1-contract.md) | Prior source contract and compatibility policy | Historical provisional contract |
| [0004 — Source Language Specification 0.1](0004-mundanereq-source-language-0.1.md) | Complete normative 0.1 language | Historical provisional standard |
| [0005 — Source Language Specification 0.2](0005-mundanereq-source-language-0.2.md) | Complete normative current language | Current provisional standard |
| [0006 — Provisional 0.2 Contract](0006-provisional-0.2-contract.md) | Current source contract, compatibility policy, and exclusions | Current provisional contract |
| [0007 — Validator Trial Contract 0.1](0007-validator-trial-contract-0.1.md) | `mundanereq-validate` CLI behavior | Maintained trial; not stable CLI |
| [0008 — Formatter Trial Contract 0.1](0008-formatter-trial-contract-0.1.md) | `mundanereq-format` CLI and policy | Maintained trial; not stable CLI or policy |
| [0009 — Trace Trial Contract 0.1](0009-trace-trial-contract-0.1.md) | `mundanereq-trace` CLI and output | Maintained trial; not stable CLI or output protocol |

## Authority boundaries

- Specification 0005 controls 0.2 syntax, semantics, validity, conformance,
  and semantic equivalence.
- Specification 0006 controls the provisional 0.2 compatibility promise and
  repository-level contract selection.
- Specifications 0001 and 0002 explain why the project made its decisions; they
  do not override normative language rules.
- Specifications 0007 through 0009 define independent tool trials. Conforming
  source does not depend on those CLIs, their diagnostic text, the Java
  implementation, or GraalVM.
- Research and experiments are evidence and decisions. They change a contract
  only when the applicable specification is updated explicitly.
