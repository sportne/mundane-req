# Experiment 0033: Complete workflow regressions

The maintained gate is `make workflow-corpus-verify` and is included in `make verify`.
It runs two bounded checks: the complete public-tool workflow and six isolated
implementation mutations. No generated artifact becomes an authoring format.

## Inventory and missing coverage

[corpus.tsv](corpus.tsv) explicitly selects five existing source sets: the
0.1-compatible and 0.2 conformance examples, their YAML counterpart, and both
representations of the 57-requirement vaccine-monitoring pilot. There are 123
requirement instances across these alternate representations, not 123 new or
independently authored requirements. The source sets retain their existing project
BSD license/provenance; the pilot's original citations and rationale remain intact.
The runner adds a synthetic review activity for regression purposes. It is not a
new engineering verification plan or a claim that the requirements are satisfied.

Earlier owning suites already test parser/formatter rules, compiled bytes, isolated
linking, and the YAML pilot analyzer. The additional gap here is rebuilding every
stage through a report from both custom and YAML authored source, including
failure propagation and coordinated identity edits. The retained gates are:

| Concern | Owning checks retained by make verify | Added here |
| --- | --- | --- |
| Source, syntax and diagnostics | Maintained JVM suite, conformance comparisons, recovery-verify | Generated model-to-source equivalence in both source modes |
| Formatting | formatter-verify, yaml-verify, snapshot safety tests | CRLF normalization, idempotence and semantic equivalence inside the complete workflow |
| Compiled contracts | compiled-verify and its golden/invalid corpora | Compilation from clean committed source through plan compilation and reporting |
| Imports and revisions | link-verify, verification-verify | End-to-end refusal of pin, scope, format and completeness failures; context filtering through reports |
| Diagnostic interchange | sarif-verify | Existing gate remains required; no copied SARIF corpus |
| Reports | report-verify | Five source-rebuilt report hashes, delete/rebuild equivalence and source/provenance checks |
| Mutations | Owning tests above | Six compiled mutations checked through public CLI observations |

## Reproduction

With the documented Java/GraalVM environment:

```sh
make workflow-corpus-verify
python3 experiments/0033-workflow-regressions/run.py --seed 150100
```

The corpus runner extracts explicit inputs from `git archive HEAD` into a clean
temporary directory. Installed tools and this runner are outside that input
archive. It compiles requirements and a synthetic TSV plan, selects exact baseline
pins/current imports, analyzes, renders, deletes all derived output, and rebuilds.
`build/experiment-0033/input-revision.txt` records the actual input commit.
`build/experiment-0033/*.html` are disposable reports; the golden summary records
reviewed complete-report byte digests and independently expected counts. Digests
identify generated revisions, never requirement identities. No timestamp or machine
path enters the report. A source/comment move changes provenance while preserving
semantic results. The same checks run across the five selected corpora.

Seeds 150100–150111 each create 3–15 synthetic requirements in three files in both
source modes. Expected semantic values are constructed independently of the parser,
including Unicode, optional values, prose, opaque math and explicit references.
Each case checks compile/format/compile equivalence, formatter idempotence, a
normative title edit, ID correction with coordinated source references, stale
plan references, explicit new baseline selection, and invalid-source propagation
through the analyzer and renderer. Invalid input cannot be written back or yield
a complete report. Generated IDs are test fixtures, not a product identity system.

Each subprocess is limited to 30 seconds and the corpus loop checks a 180-second
budget before every invocation. A failing seed is printed with an exact replay
command and its generated inputs/artifacts are retained under `build/experiment-0033/`.
There were no unexpected source-model failures to minimize in the recorded run;
small targeted mutation witnesses are retained in mutations.py. If later generation
finds a bug, reduce the saved source while preserving the failed public assertion
and add that minimized example to its owning regression suite.

## Mutation evidence

`mutations.py` copies Java source into temporary directories and compiles six
separate mutants: accept padded scalars, omit absent-target validation, skip exact
pins, accept incomplete imports, publish partial compiled requirements, and ignore
statement changes in the review basis. Every unmodified witness must pass first.
Noncompiling mutants, timeouts and crashes are failures of the experiment, not
successful mutation detections. Each compiled mutant must change the expected
public CLI status, diagnostics, publication contents or stale bindings.

The mutation loop has a 180-second budget and 45-second subprocess timeouts.
[Recorded outcomes](results/mutations.json) show all six were killed by observable
behavior. Mutation operator locations are maintenance details; the acceptance
assertions do not inspect class/package layout. No tracked Java file is modified.
This bounded set does not claim a comprehensive mutation score or performance profile.
