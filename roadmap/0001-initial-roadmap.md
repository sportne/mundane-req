# Roadmap 0001: A Composable Engineering Tooling Monorepo

Status: Draft living roadmap

Last reconciled: 2026-09-04 for the compiled-artifact ecosystem direction

Execution is decomposed into the [task-card index](0002-task-card-index.md).
This remains the single strategic roadmap; cards describe bounded work and
acceptance evidence. Stage numbers group work and preserve existing task IDs;
the execution order identifies dependencies and opportunities for parallel work.

## Purpose

Develop a text-oriented engineering tooling ecosystem in this monorepo, with
Mundane-Req as its first independently usable language and tool component.
Human-authored requirements, assertions, and project declarations remain
authoritative. Compiled artifacts, indexes, analyses, and views are derived.

The first integration should prove one complete loop:

```text
requirements source -> requirement compilation ----+
                                                   +-> explicit linking
verification plan source -> plan adapter/compiler -+       |
                                                           v
                                              coverage/staleness analysis
                                                           |
                                                           v
                                              reproducible verification report
                                                           |
                                              source edit and recorded rebuild
```

"Compile" means interpret source into a documented semantic representation with
diagnostics and provenance. "Link" means resolve explicit references and check
their selected contracts. Domain tools interpret relationship consequences;
reference reachability alone establishes neither invalidity nor satisfaction.

## Product direction

One repository can coordinate contracts and consumers without requiring one
language, executable, shared version number, or universal engineering metamodel.
The initial responsibilities to investigate are:

| Responsibility | Boundary to preserve |
| --- | --- |
| Requirements | Source language, validation, formatting, decomposition analysis, and compiled semantic output |
| Artifact integration | Only demonstrated import, qualification, revision, provenance, and linking contracts |
| Verification | The pilot-selected plan, coverage, and staleness workflow |
| Views | Reproducible presentation over selected artifacts and explicit analysis results |
| Examples and integration checks | Bounded workflows that expose accidental coupling and incomplete results |

TC-1101 selects logical boundaries and justified physical moves. TC-1104 performs
only those moves that improve a checkable boundary. The repository name and
existing file layout are unchanged by this planning update. Requirements-only
use must remain possible without verification or view tools.

This is the tooling monorepo. Users may author engineering information in one
repository or several; the first import contract tests both with local fixtures.
Existing formats may be adapted, and binary/native engineering files may remain
authoritative where practical. Additional safety, BOM, source-code, and CAD
integrations need concrete workflows before acquiring implementations.

## Current evidence and gaps

The maintained source contract is provisional 0.2, with prior 0.1 conformance
fixtures. Java 21/GraalVM supplies three independent native tools. The existing
lossless source representation preserves comments; normalizedInventory is a
testing utility rather than a public compiled-artifact contract.

- [The pilot](../experiments/0024-vaccine-monitoring-pilot/assessment.md)
  exercised 57 requirements and two baselines. Its
  [decision](../research/0032-end-to-end-pilot-decision.md) selected verification
  planning because manual coverage and coarse revision binding caused friction.
- The former formatter-inventory gap was repaired in commit 42e5082. The latest
  pilot closeout extended documented coverage to 30 source sets and 64 files.
  The old audit finding is not an outstanding formatter-corpus task.
- Existing tests already cover formatter idempotence, semantic preservation,
  comment retention, and selected output/replacement failures. New work extends
  those checks rather than reopening completed cards.
- Formatter write-back currently replaces a selected snapshot without checking
  intervening edits; later-file failures can follow earlier successful writes.
  Validator output lacks the completion checks already used by formatter/trace.
- Interpreter still combines decoding, parsing, semantic construction, and
  source-set validation. One parse failure can discard a file's parsed records;
  source decoding is repeated in the formatter path.
- CI runs the native suite and example workflow, not the complete Makefile
  verification gate. Tool/source identifiers and package versions are duplicated.
- Linux packaging already supplies checksums, notices, environment information,
  and isolation checks. Existing evidence is source-reproducible, not a claim of
  byte-identical native binaries.
- ReqIF remains a bounded experimental self-roundtrip. External-tool evidence is
  conditional on availability. Human usability and broader operational evidence
  remain unestablished; their absence is not a dependency for this backlog.

These are findings from source and recorded evidence, not new test results.
The current compilation, linking, schema, and monorepo-layout decisions remain
unresolved. This roadmap does not change normative language or CLI contracts.

## Stage 11 — Establish monorepo and ownership boundaries

[TC-1101](task-1101-define-monorepo-component-boundaries.md) identifies component
ownership, permitted dependencies, and the smallest useful layout.
[TC-1102](task-1102-define-requirement-and-assertion-ownership.md) separates
obligations, descriptive attributes, relationships, and contextual assertions.
[TC-1103](task-1103-test-compilation-linking-and-rebuilds.md) exercises the
requirements/verification loop with experimental artifacts before selecting
public contracts. [TC-1104](task-1104-establish-monorepo-component-layout.md)
conditionally implements justified layout changes.

The experiment reuses the existing pilot and plan carrier. It tests a serialized
consumer boundary, missing/ambiguous references, context, revisions, partial
information, and rebuilds. It is not a mandate to standardize every artifact.

## Stage 12 — Publish bounded compiler and import interfaces

[TC-1201](task-1201-define-requirement-semantic-output.md) defines versioned
requirement semantics, source ranges, diagnostic meanings, provenance, ordering,
and completeness; [TC-1202](task-1202-emit-compiled-requirement-artifacts.md)
implements the selected interface.

[TC-1203](task-1203-define-import-and-reference-contracts.md) decides explicit
import selection, qualification, target kinds, revision binding, and failure
behavior. Qualification preserves human-authored requirement IDs. The design
distinguishes relationship cycles from build cycles and describes partial input
without presenting it as fully analyzed.

[TC-0905](task-0905-define-verification-analyzer-contract.md) supplies the
verification-plan contract and staleness decisions after those designs.
[TC-1204](task-1204-implement-bounded-artifact-linking.md) implements only the
shared resolution capability justified by that workflow.
[TC-0904](task-0904-implement-the-selected-ecosystem-tool.md) implements the
focused verification consumer.

A digest has a concrete potential consumer in revision binding. Compare it with
Git binding before selecting it, document canonicalization and relevant edit
behavior, and keep it outside authored requirement records. It identifies content,
not a replacement identity.

## Stage 13 — Decide project-defined attributes

[TC-1301](task-1301-classify-project-attribute-use-cases.md) applies the ownership
decision to representative metadata and selects a bounded initial type set.
[TC-1302](task-1302-decide-project-attribute-schemas.md) then decides checked-in
schema source, naming, types, cardinality, defaults, discovery, standalone-file
behavior, and compatibility.

Text and enumeration are first candidates. Lists, references, and other types
must earn their place. Descriptive classification may belong on a requirement;
assessment criticality may require context, revision, scheme, rationale, evidence,
and authority. Current built-in allocation remains valid while richer variant
allocation is studied as a distinct assertion.

Attribute implementation is deliberately conditional on these decisions. The
schema card must propose bounded successors for grammar/model/validation,
comment-preserving formatting, compiled/view/trace/ReqIF propagation, fixtures,
documentation, and individual editor capabilities. It must distinguish lossless
support, visible best-effort mapping, and unsupported cases. No syntax is selected
by this roadmap.

## Stage 14 — Address current correctness independently

Begin these alongside architecture design:

- [TC-1401](task-1401-protect-formatter-write-back.md): detect intervening edits
  and document recoverable multi-file outcomes, including remaining filesystem races.
- [TC-1402](task-1402-report-cli-output-failures.md): avoid false success on
  stdout/stderr failures and retain focused existing command behavior.
- [TC-1403](task-1403-recover-parser-diagnostics-safely.md): recover independent
  errors without inventing complete valid models or misleading cross-file findings.

Separate decoding, syntax, semantics, and validation only where it produces an
observable improvement. TC-1202 inspects repeated concrete-source decoding while
retaining one interpretation. Further performance work requires a profile and a
concrete consumer, not a general cleanup objective.

## Stage 15 — Extend repeatable checks and contributor integration

[TC-1502](task-1502-centralize-version-declarations.md) addresses current version
drift while preserving independent source/tool/package/format identifiers and
experimental migration policy.
[TC-1503](task-1503-align-ci-with-authoritative-verification.md) closes today's
gap between hosted checks and make verify. Both can begin now.

After the first report loop,
[TC-1501](task-1501-extend-artifact-workflow-regression-corpora.md) broadens its
bounded golden/change corpus, seeded generation, targeted mutation checks, and
compatibility fixtures. Every implementation card still supplies its own tests.
[TC-1504](task-1504-emit-sarif-validation-diagnostics.md) provides a concrete
code-review/editor diagnostic interface after diagnostic contracts and recovery.

Reproduce existing package checks rather than commissioning new packaging work
without a demonstrated need. Document tested Java, GraalVM, native-toolchain,
OS and architecture assumptions. Future checksums/provenance/platform additions
must name a current missing capability and measured evidence.

## Views and specifications

[TC-0903](task-0903-run-a-derived-presentation-experiment.md) generates the
verification report after the analyzer. It uses deterministic presentation first,
records input revisions and analysis completeness, and links findings to source.

[TC-0807](task-0807-test-authored-views-and-specifications.md) is a conditional
successor if the report demonstrates a need for authored composition. Source
definitions can own selection and ordering; generated report content remains
derived. Issued reports may be retained as delivery records with separate
approval provenance, never as an editable competing source of requirement facts.

[TC-0902](task-0902-run-an-independent-reqif-roundtrip.md) retains its existing
conditional external-tool experiment. Editor highlighting, inline diagnostics,
navigation, formatter integration, attribute hover/completion, and Unicode
confusable/invisible-character diagnostics remain candidates for bounded follow-up
cards after their contracts and authoring cases exist. These are individual
capabilities; the roadmap does not require a single large language-server task.

## Immediate execution order

```text
TC-1101 -> TC-1102 -> TC-1103 -> TC-1201 -> TC-1203 -> TC-0905
   |            |                 |                       |
   v            v                 v                       |
TC-1104?     TC-1301           TC-1202 ---------------------+
                |                                         v
                +-- (+ TC-1203) -> TC-1302             TC-1204?
                                                          |
                                                          v
                                        TC-0904 -> TC-0903 -> TC-1501
                                                       |
                                                       v
                                                   TC-0807?

Start independently: TC-1401, TC-1402, TC-1403, TC-1502, TC-1503
TC-1502 -> TC-1201
TC-1201 + TC-1402 + TC-1403 -> TC-1504
TC-0902? remains independent of the main chain
```

A question mark marks a conditional card. All dependencies are enumerated in
the index, including completed evidence prerequisites. If a conditional design
selects a smaller boundary, revise downstream dependencies explicitly rather than
performing unjustified work. TC-1104's optional moves need coordination with
active code changes but do not block fixes in the existing layout.

## Cross-cutting rules for every stage

1. Authoritative source and human requirement IDs remain sufficient for understanding.
2. Compiled semantics, indexes, and reports have documented provenance and regeneration.
3. Independent components are tested through published artifact interfaces as well
   as any justified shared implementation.
4. Parsing, linking, domain analysis, and reporting have distinct completeness and
   failure rules; generation is not approval or satisfaction.
5. Source/CLI/package/compiled-format versions remain independently identified.
6. Decisions record valid/invalid examples, alternatives, and stop conditions.
7. Verification uses checked-in corpora, golden/adversarial fixtures, seeded
   generation, mutation, existing compatibility cases, focused dogfooding, and
   reproducible clean builds. Optional contributor sessions supplement that evidence.
8. New capabilities enter through demonstrated workflows and small reviewable cards.

## Historical evidence and superseded planning

Completed cards retain their IDs, paths, and recorded outcomes in the index.
TC-1001 and TC-1002 are completed historical decisions; their former future
decision sequence is superseded by Stages 11–15. TC-1003 remains completed pilot
evidence for TC-0905. No completed evidence card is relabeled as unfinished work.

The former Stage 7 field-use and separately implemented-parser expectations are
replaced in active planning by the bounded checks above. Historical evidence
limitations remain facts about those experiments.

The following anchors preserve links from completed cards and research. Their
historical work is summarized here; the current task dependencies are above.

<a id="first-implementation-hypothesis"></a>
<a id="stage-1--establish-an-exact-02-implementation-baseline"></a>
<a id="stage-2--extract-the-smallest-shared-foundation"></a>
<a id="stage-3--deliver-mundanereq-validate"></a>
<a id="stage-4--define-and-deliver-mundanereq-format"></a>
<a id="stage-5--define-and-deliver-mundanereq-trace"></a>
<a id="stage-6--exercise-the-tools-in-concert"></a>
<a id="stage-7--test-operational-use-and-scale"></a>

Stages 1–7 supplied conformance repair, the maintained Java/source foundation,
independent native commands, formatting/trace evidence, packaging, controlled
workflows, and bounded scale. See the completed-card inventory for exact results.

<a id="identity-continuity"></a>
<a id="verification-planning-and-evidence"></a>
<a id="safety-classification-and-other-assessments"></a>
<a id="allocation-and-controlled-vocabulary"></a>
<a id="glossary-and-formal-symbols"></a>
<a id="trace-policy"></a>

Stage 8's studies retain human ID identity, independently owned verification and
safety assertions, an opaque allocation label, and project-specific trace policy.
Their recorded decisions inform TC-1102 and TC-1301.

<a id="stage-9--add-focused-ecosystem-tools-only-where-use-justifies-them"></a>
<a id="derived-presentation-milestone"></a>
<a id="stage-10--decide-whether-to-stabilize-10"></a>

The initial ecosystem prioritization was followed by the completed audit and
decision records and the two-baseline pilot. This roadmap replaces their
prospective gates with the incremental workflow above while preserving the
[closed cards](0002-task-card-index.md#completed-evidence).
