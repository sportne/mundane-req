# Local artifact imports and linking 0.1

Status: Selected experimental contract

Import format: `mundane-imports-0.1`; linked output: `mundane-linked-0.1`.

This contract covers the first requirements/verification consumer. It selects
compiled references and provenance, not a shared engineering authoring language.
Requirements retain human IDs and their independent source/command contracts.

## Explicit inputs and snapshots

A checked-in JSON import declaration has this shape (no duplicate/unknown keys):

```json
{"format":"mundane-imports-0.1","imports":[
  {"scope":"baseline","path":"artifacts/baseline.json","kind":"requirements","sha256":"EXACT_64_LOWERCASE_HEX","dependsOn":[]},
  {"scope":"current","path":"artifacts/current.json","kind":"requirements","sha256":null,"dependsOn":[]}
]}
```

The shown digest placeholder must be replaced by the exact file's SHA-256.
Scopes use `[A-Za-z0-9][A-Za-z0-9._-]*`, are unique and authored; they qualify an
ID without becoming a second requirement identity. All five import fields are
required. Paths are relative to an explicit invocation root; reject absolute paths,
backslashes, empty segments, `.`/`..` segments and resolved symlink escapes from
that root. Source locations *inside* compiled artifacts are relative to each
artifact's compiler source root, qualified by import scope when reported.

A null digest selects the current local artifact snapshot. A nonnull digest pins
exact serialized artifact bytes, including compiler/source/provenance metadata;
any mismatch fails before linking. No network, registry, Git discovery or automatic
build is performed. For pinned Git use, the caller prepares a detached checkout
at a recorded commit, compiles explicit inputs and records the artifact digest.
The declaration's digest is a binding, not a new requirement ID. Generated build
manifests record resolved bytes/formats/versions, never replace checked-in inputs.

Read each file once into a bounded snapshot, then recheck bytes and available file
identity before returning linked output. Detected changes fail `input-changed`.
There remains a race after the last recheck; output describes the checked snapshots,
not an atomic filesystem transaction. Limits: 16 MiB per JSON file, depth 64,
100 imported artifacts and 128 MiB aggregate read bytes. Reject malformed UTF-8,
duplicate JSON keys, non-finite numbers and invalid Unicode strings.

## Resolution and cycles

The companion contract supplies plans, activities and references; requirements
use `mundanereq-requirements-0.1`. The bounded resolver reads published JSON only.
It validates required fields and types, completeness, unique IDs, local decomposition
references, source spans and SHA-256 provenance before any linking. An arbitrary
object with complete=true is not sufficient. Unknown kinds/formats fail; no numeric
version guessing or fallback source parsing occurs.

Each plan names a baseline scope, current scope and context. A null scope means
unqualified selection and is accepted only when exactly one requirement import
exists. Two scopes containing equal IDs require explicit qualification. Each
coverage row supplies a human requirement ID and activity ID, its owner plan and
source location. Resolve both baseline and current references. Preserve failures
as diagnostics at that authored row; publish no successful edge list if any required
reference/import fails. A missing baseline cannot be treated as a newly covered
current requirement. Activity references resolve within the selected plan artifact.

`dependsOn` declares import build dependencies. Reject missing names, duplicate
entries, self/cyclic build dependencies, even though resolution is order-independent.
The resolver never builds them. Requirement decomposition cycles and self-links
remain legal and distinct from build cycles. Resolving links establishes existence
and kind compatibility only, not coverage adequacy, approval or satisfaction.

Context filtering selects whole plans by exact context. Unknown context is an
error. All declared imports and the complete plan artifact must be structurally
valid, including unselected plans; reference resolution applies to selected plans.
A complete filtered result explicitly names its context and selected plans; it
makes no claim about other contexts. Derived reverse indexes retain the authored
edge ID and source location; there is no editable inverse relationship store.

## Command and output boundary

```text
mundane-link --root DIRECTORY --plan COMPILED_PLAN [--context CONTEXT] IMPORTS
```

Paths in command arguments are relative to the invocation directory; all must
resolve beneath root. JSON import paths are relative to root. No directory discovery.
Help/version are standalone options. Unknown/duplicate options, absent arguments
and invalid root are invocation errors (stderr, exit 2, no artifact).

Normal output is one sorted-key compact UTF-8 JSON object plus LF:
`{format, complete, context, linker, plans, imports, planArtifact, edges, inverse, diagnostics}`.
`linker` identifies `{name:"mundane-link",version:"experimental-0.1",contract:"link-cli-0.1"}`.
Imports are scope-sorted records `{scope,path,sha256,artifact}` containing validated
compiled snapshots. planArtifact is `{path,sha256,artifact}`. Plans list selected
plan IDs sorted. Edges are sorted by planId/activityId/requirementId; each contains
`planId,activityId,requirementId,context,baselineScope,currentScope,location`.
Inverse is a sorted map from `scope:requirementId` to edge indexes for current
references. It is entirely derived. Partial/invalid results set complete=false,
edges=[], inverse={}; diagnostics remain available. Output records the exact
import/plan bytes and their published provenance, making downstream interpretation
possible without rereading sources or parser classes.

Diagnostics are `{code,message,location}`; location is `{path,line,column}`, one-based
code points. Plan assertion paths are qualified `plan:PATH`; import problems point
to the declaration (line/column 1 when no finer retained location exists). Stable
codes: `input-unavailable`, `input-changed`, `digest-mismatch`, `invalid-json`,
`invalid-import`, `invalid-artifact`, `unsupported-format`, `wrong-kind`,
`incomplete-import`, `duplicate-scope`, `missing-dependency`, `build-cycle`,
`missing-scope`, `ambiguous-scope`, `missing-target`, `missing-activity`,
`unknown-context`. Sort code/path/line/column/message. Link errors return 1;
operational input/output failures return 2. Output failures override success and
may leave a prefix; consumers require exit 0 and complete=true with empty diagnostics.

## Change matrix and compatibility

| Change | Identity | Exact artifact pin | Requirement-value comparison / rebuild |
| --- | --- | --- | --- |
| Comment-only | Same ID | Changes | Equal values; new provenance |
| Normative text | Same ID, revised content | Changes | Changed values; review policy decides staleness |
| Unrelated requirement | Covered ID unchanged | Changes | Covered values equal |
| File move | Same ID | Changes | Equal values, changed source location |
| ID correction | Old ID removed/new ID added | Changes | Old external reference fails; no implicit alias |
| Imported Git revision with equal values | Same IDs | May change with provenance/tool bytes | Compare values independently of Git history |
| Two scopes reuse an ID | Two explicit qualifications | Independently selected | Ambiguous without scope; authored IDs unchanged |

Preserve [semantic output 0.1](0012-requirement-semantic-output-0.1.md) values and
source meanings. Import/linked formats evolve independently; changed required fields
or meanings require new identifiers and explicit migration notes. Unknown keys in
this narrow authored manifest fail, while informational additions in requirement
artifacts follow their existing output contract. No public per-requirement digest
is selected: the first consumer compares semantic values under TC-0905's policy.

Implementation refinement: linked output now includes explicit linker provenance
alongside imported compiler metadata. This informational field does not change
reference meanings or the requirements/plan source contracts.
