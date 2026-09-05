# Verification planning and review analysis 0.1

Status: Selected experimental contract

Source: `mundane-plan-source-0.1`; compiled plan: `mundane-plan-0.1`;
analysis: `mundane-verification-0.1`.

## Source choice and authority

Select three UTF-8 TSV files in one explicitly selected directory: plan.tsv,
activities.tsv, coverage.tsv. The existing pilot already authors one activity or
coverage assertion per row; the bounded second experiment demonstrates this carrier
without nested data or multiline procedures. This decision is independent of YAML
requirements. Safety, evidence, procedures and other artifacts retain their own
format decisions. The carrier is a verification plan, not a test-procedure language.

Exact headers (tabs separate columns):

```text
plan.tsv: format  plan_id  context  baseline_scope  current_scope
activities.tsv: activity_id  method  objective  expected_evidence
coverage.tsv: plan_id  activity_id  requirement_id
```

Each plan row repeats the exact source identifier in format. All files require LF
or CRLF termination and at least the header. No BOM, quoting/escaping convention,
blank data rows, comments, tab/newline/control characters within values or padded
values are supported. Reject wrong/duplicate headers, wrong column counts, duplicate
IDs/coverage tuples, and unknown activity/plan references. Each file is at most
8 MiB; each table at most 10000 data rows. Plans and activities are nonempty;
coverage may be empty and then all selected current requirements are uncovered.
IDs and scopes use the requirement ID pattern. Context is a nonempty authored label.
Empty scope cells mean unqualified selection under the import contract; empty cells
otherwise fail. Method is one of test, analysis, inspection, demonstration, review.
Objective and expected_evidence are nonempty text. Expected evidence describes a
planned deliverable; it neither stores evidence nor asserts a result.

Source rows are authoritative. Plans own context and baseline/current selections;
activities own method/objective/expected evidence; coverage rows own the activity
to requirement assertion. Plans may share activities and requirements. Changing an
activity changes the plan artifact's provenance, not requirement semantics.

## Compiled plan boundary

`mundane-plan --root DIRECTORY PLAN_DIRECTORY` compiles exactly those three files,
with no discovery of requirements or imports. The root/path rules and bounded-read
rechecks are those of local imports. Standalone --help/--version are supported.
Output is sorted-key compact JSON plus LF:

```text
{artifactKind:"verification-plan",format:"mundane-plan-0.1",
 sourceContract:"mundane-plan-source-0.1",compiler:{name,version,contract},
 complete:BOOLEAN,sources:[{path,sha256}],plans:[...],activities:[...],coverage:[...],diagnostics:[...]}
```

Plan: `{id,context,baselineScope,currentScope,location}`.
Activity: `{id,method,objective,expectedEvidence,location}`.
Coverage: `{planId,activityId,requirementId,location}`.
location is `{path,line,column}`, with one-based code-point coordinates and a retained
row-start column 1; paths are relative to compiler root. Plans/activities sort by ID,
coverage by plan/activity/requirement. Arrays are empty on invalid/incomplete source;
locations/checksums retain their original authority and exact bytes. Compiler name is
`mundane-plan`, version `experimental-0.1`, contract `plan-cli-0.1`. Diagnostics follow
local imports with additional code `invalid-plan`. Source invalidity returns 1;
invocation/input/output failure returns 2. A prefix after failed output is unusable.

The plan output is a public serialized input to the linker. Readers validate required
fields/types, identifiers, methods, source locations/checksums, unique keys/rows,
activity/plan references, supported format/source contract, compiler provenance and
complete=true. Unknown future normative plan fields are rejected in this version.
A file merely claiming completeness is insufficient.

## Baseline binding and analysis

`mundane-verify --root DIRECTORY --plan COMPILED_PLAN [--context CONTEXT] IMPORTS`
uses the bounded resolver and requirement output 0.1. It never imports requirement
parser classes. Each coverage row resolves the same human ID in the plan's selected
baseline and current scope; an old ID correction is a missing reference, not an alias.
Pinned baseline artifacts remain derived from retained source revisions; exact pin
checking and recorded artifact digests make the analyzed inputs reproducible.

Compare the seven fields in each requirement's values object: id, title, allocation,
statement, rationale, source, decomposes. Compare object values structurally, block
arrays in order, decomposition as a set; ignore permitted informational fields.
A change to any selected field makes that coverage row `review-stale`; otherwise
it is `current`. Paths, comments, compiler identity and other requirement records
are outside this comparison. No semantic hash is published or authored. A changed
activity alone does not make a requirement basis stale; review-current is never
approval of that activity. Rebaseline by deliberately choosing reviewed source
revisions and rebuilding/pinning their artifacts, not by editing compiled values.

Planned coverage means an authored activity reference exists for a current
requirement in a selected plan. Enumerate uncovered requirements from that plan's
entire current imported artifact. Do not silently union multiple contexts/plans.
A stale row still represents planned coverage, with a separate review finding.
Possible impact here means the covered requirement values changed and the authored
coverage row identifies an activity potentially affected; it does not establish
execution failure, invalidity, adequacy or requirement satisfaction. No transitive
cross-domain impact or test execution is implemented.

## Analysis output and exit behavior

```text
{format:"mundane-verification-0.1",complete:BOOLEAN,context:STRING_OR_NULL,
 linked:LINKED_RESULT,coverage:[...],uncovered:[...],diagnostics:[...]}
```

Coverage rows contain all linked edge fields plus `state`, `changedFields` (sorted
field names) and `possibleImpact` (true iff changedFields is nonempty). Uncovered
rows are `{planId,context,scope,requirementId,location}` with a current requirement
record-start location qualified by scope. Sort coverage by plan/activity/requirement
and uncovered by plan/scope/ID. The embedded linked result records exact import and
plan snapshots, source locations, versions and completeness; every finding can be
traced to a source assertion or selected in-scope requirement.

Exit 0: complete, no stale or uncovered findings. Exit 1: complete analysis with
stale/uncovered findings. Exit 2: analysis prevented by invalid/missing/incompatible
inputs, link failures, invocation or output failures. Prevented analysis has
complete=false, coverage=[], uncovered=[] and propagated diagnostics; no apparently
complete coverage percentage is emitted. Failed output may leave a prefix and must
never report success. Rendering may accept complete exit-1 findings as a useful
report; it cannot treat incomplete output as a successful analysis.

## Compatibility and exclusions

The three version domains are independent of requirements/source/import versions.
Unknown plan/analysis versions fail; changed meanings/required fields require a new
identifier and migration notes with before/after examples. Historical TSV fixtures
remain historical: conversion explicitly adds format/context/scope selections and
preserves activity/coverage rows. No hidden data migration or reinterpretation occurs.
No evidence execution/storage, safety inference, certification, satisfaction,
generalized policies or permanent language stability is claimed.
