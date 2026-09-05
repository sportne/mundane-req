# Research 0036: Component boundaries for the first compiled workflow

Date: 2026-09-05

Decision: keep the current physical layout and establish a serialized consumer
boundary. Requirements are the only maintained domain component today. Verification
planning and reporting are the first bounded integration; their current TSV inputs
and experiment scripts are evidence, not a maintained source language.

## Responsibilities and permitted dependencies

```text
requirements source -> interpreter -> requirement compiler -> artifact file
                           |                                   |
                     validate/format/trace                     v
plan source -> experimental plan adapter -> plan artifact -> explicit resolver
                                                               |
                                                       verification analysis
                                                               |
                                                         derived report
```

Requirements commands may share interpretation, concrete source, diagnostics and
output handling. A verification consumer reads the published artifact contract;
it must not load Interpreter, SourceDocument, or parser classes. The resolver
establishes reference existence/scope, not safety judgments or satisfaction.
Analysis owns planned coverage and review staleness. Reports present those findings
and their provenance. They cannot change source facts.

| Current paths | Logical owner / proposed path | Contract owner |
| --- | --- | --- |
| src/main/java/mundanereq/Interpreter.java, YamlRequirements.java, SourceFormat.java, source/ | Requirements; retain paths | Requirements source/model/diagnostics |
| src/main/java/mundanereq/cli/, format/, trace/ | Requirements commands; retain paths | Each command's independent CLI contract |
| specification/ source specifications and schema/ | Requirements; retain paths | Source profile and structural schema |
| Future focused compiler and output specification | Requirements, alongside existing commands | Requirements semantic output |
| experiments/0017-verification-evidence and 0024-vaccine-monitoring-pilot | Historical verification evidence; retain paths | Experimental carriers only |
| New compilation experiment | Integration evidence under experiments/ | Provisional reference/provenance meanings |
| src/test/, conformance/, examples/, Makefile, scripts/ | Requirements checks and repository orchestration; retain paths | Verification entry points and fixtures |
| distribution/, dependencies/ | Existing command packaging/dependency notices; retain paths | Package inventory and build assumptions |
| Future maintained verification/view components | Decide concrete paths with TC-0904/0903 | Their own domain and presentation contracts |

No physical move currently improves a demonstrated boundary: every maintained
production class belongs to requirements. Splitting empty integration, safety,
BOM or CAD directories would create structure without an independently usable
capability. TC-1104 remains conditional, to be reconsidered when maintained
verification code exists. Shared test infrastructure may invoke commands and compare
artifacts; it must not become production domain policy.

## Observable independence checks

Requirements-only scenario: build and run `make native-validator` against
`examples/yaml/vaccine-monitoring` with `--source=yaml-0.3`. Its dependency closure
is Java/GraalVM, the pinned YAML parser, and requirements sources/tests; no plan,
view runtime, server or registry is required. Existing native-boundary isolation
checks demonstrate command independence (Research 0035).

Consumer scenario for TC-1103/1202: launch a Python standard-library process with
only serialized requirement/plan inputs. It resolves IDs and reports coverage
without Java on its command line or imports of parser code. Run from a fresh
output directory, remove outputs, rebuild and compare. Reject incomplete inputs
and unknown format identifiers before domain analysis. A consumer that accesses
Java model classes or reads original requirement syntax fails this boundary test.

Before any later physical move, reproduce requirements-only builds, isolated
consumer checks, full `make verify`, package paths and historical experiment
commands. Compare the dependency closure before/after; a move without observable
independence or ownership benefit does not justify TC-1104.

## Source, version and repository boundaries

YAML is selected only for requirement source (custom 0.2 remains supported).
Verification plan notation is unresolved; the existing TSV is suitable for the
experiment. Safety/BOM/code/CAD can use existing or native representations when
those workflows are investigated. Shared compiled references do not mandate shared
source syntax, implementation language, schema, or a universal metamodel.

This repository builds tools. Engineering users may keep explicitly selected inputs
in one repository or several local checkouts. Imports will use explicit scope and
revision selection, not implicit directory-wide discovery of all engineering facts.
The source contract, each CLI, package and compiled format have independent version
domains; TC-1502 supplies their declaration mechanism.

Alternatives rejected: immediate directory migration (no current second maintained
component); direct parser API integration (couples every consumer to Java internals);
a universal graph model/registry (no evidence this first workflow needs one).

This decision enables TC-1102 and the bounded compilation experiment. Stop expansion
if requirements-only use needs another domain tool, or if integrating one plan
requires universal inheritance, approval semantics or a server.
