# Comparison findings

Observed on 2026-09-04 against maintained source at `ea463f8`. The replayable
[evidence](results/summary.json) is the basis for the following claims. No author
completion times, independent feedback or general interoperability results were
collected.

## Results

| Question | Custom source | YAML candidate | Implication |
| --- | --- | --- | --- |
| Same requirement values? | Maintained interpreter baseline | All 11 records match, including ordered prose/math, optional fields and links | Representation can change without changing requirement identity/model |
| Corpus size | 153 lines, 6,015 bytes | 177 lines, 6,888 bytes | This YAML encoding is about 16% longer in lines and 15% larger in bytes; not a readability score |
| Multiple records and file order | Explicit opener/closer; ID independent of file | Requirements sequence; ID independent of position/file | Both pass reordered inputs and combined/reordered-record checks |
| Ordinary edits | Direct prose and field edits | Similar scalar edits, with quoting/indentation requirements | Both yield equal semantics after the matched normative edit, retarget, addition and requirement split |
| Wrapping, comments and moves | Tested changes preserve semantics | Tested changes preserve semantics | Both remain suitable for the exercised text/Git workflow |
| Three-way merges | Disjoint edits merge; conflicting title edits conflict | Same outcomes | No merge advantage demonstrated by this small comparison |
| Prose and math | Natural interleaving with custom block markers | Explicit ordered block entries and literal payload | YAML costs visual scaffolding but preserves this corpus exactly |
| Scalar ambiguity | No implicit Boolean/numeric typing in fields | `001`/`true` fail string schema; unquoted hash can silently truncate text | Choose and document scalar styles, not just a data schema |
| Missing/unknown/duplicate fields | Domain-specific diagnostics | Parser rejects duplicates; schema rejects missing/unknown fields | Reusable infrastructure supplies useful checks, but presentation still needs work |
| Recovery | Two-error example reports one out-of-order-field error and discards the file | Two missing required keys produce two schema errors with record locations | Demonstrates schema aggregation, not superior general syntax recovery |
| Cross-file rules | Existing uniqueness/resolution | Small explicit domain adapter | JSON Schema alone does not define source-set semantics |

The requirement split intentionally changes the baseline semantic model by
creating another authored ID. The move/reorder operations preserve it. Golden
diffs and actual `git merge-file` outputs are under results/edits and results/merges.
The unquoted-hash case is intentionally recorded as valid in both formats but
semantically unequal; it is a finding, not a passing fidelity claim.

## Formatting and comments

All tested formatting paths preserve the 11 normalized requirement values.
Comment-line sequence preservation is a separate measurement; it does not prove
correct comment attachment for every possible edit or inline comment position.

| Path | Tested comments retained | Idempotence | Use decision |
| --- | --- | --- | --- |
| Maintained custom formatter | 13/13 | All four files | Existing baseline |
| ruamel round-trip | 13/13 | All four files | Feasible experiment tooling; no production Python dependency selected |
| SnakeYAML object load/dump | 0/13 | All four files | Unsuitable for source rewriting with these settings |
| SnakeYAML node compose/dump | 9/13 | Fails on three of four files | Unsuitable as the tested formatter; not a claim about every possible library configuration |
| Java conservative CRLF-to-LF text preservation | 13/13 | All four files; CRLF also exercised | Feasible narrow starting policy; full validation supplied separately by harness |

The conservative YAML path is intentionally comparable to the maintained
formatter's restrained policy. It does not reindent or sort fields, and neither a
full YAML formatter nor current custom inter-record blank-line normalization has
been implemented for YAML. A structural formatter must earn its transformations
with concrete-source preservation tests. Ordinary object serialization is not a
safe substitute for editing authoritative source.

## Reusable tooling versus remaining work

| Facility | Evidence and license | Remaining work / limitation |
| --- | --- | --- |
| Java parser | SnakeYAML Engine 3.1.1; Apache-2.0; actual Java 21 parsing plus 43 JVM/native comparisons | Configure the selected profile, enforce resource bounds, carry source marks into semantics and diagnostics |
| Native image | GraalVM CE 21.0.2, no fallback; experimental parser/emitter builds and runs | Native schema/domain validation and maintained CLI integration untested |
| Round-trip prototype | ruamel.yaml 0.19.1, MIT; observed preservation and default-resolver discrepancy | Production Java source-preservation strategy still needs implementation |
| Structural schema | jsonschema 4.26.0, MIT; actual Draft 2020-12 validation and multiple-error collection | Select/verify a Java schema implementation or explicit equivalent validation; source spans and domain rules remain custom |
| Editors | Official YAML language-server documentation describes schema validation, completion, hover, formatting and symbols | Documentation-only assessment: no editor was run; cross-file references, domain errors, profile enforcement and safe format integration remain project work |
| Build and distribution | Pinned experimental packages and checksummed jar; Java helpers compile with warnings as errors | Adding a runtime dependency changes the existing dependency-free implementation choice and requires notices and reproducible dependency handling |

Sources: [SnakeYAML Engine](https://github.com/snakeyaml/snakeyaml-engine/blob/master/README.md),
[versioned Maven POM](https://repo.maven.apache.org/maven2/org/snakeyaml/snakeyaml-engine/3.1.1/snakeyaml-engine-3.1.1.pom),
[ruamel round-trip behavior](https://yaml.dev/doc/ruamel.yaml/detail/),
[ruamel package metadata](https://pypi.org/pypi/ruamel.yaml/0.19.1/json),
[jsonschema package metadata](https://pypi.org/pypi/jsonschema/4.26.0/json),
[YAML language-server capabilities](https://github.com/redhat-developer/yaml-language-server/blob/main/README.md).
The dependency versions and hash actually tested are recorded separately from
these potentially evolving documentation pages.

A standard parser and structural schema can replace maintained custom syntax
recognition and some field checks. Reduced future syntax/editor maintenance is
an inference from this demonstrated reuse, not a measured net cost saving.
Cross-file validity, domain-specific diagnostics, migration, semantic compilation,
source-safe formatting and attribute ownership still belong to this project.

## Decision limits and stop conditions

Recommend a YAML-based successor specification because the semantic model fits
ordinary YAML and the reusable parsing/schema path is demonstrated on Java/native
and Python. Retaining the custom format would preserve its more compact prose/math
layout and simpler scalar treatment; those are real benefits, but this bounded
comparison does not show they justify maintaining a custom authoring grammar as
the ecosystem grows. This is a project design judgment, not user-study evidence.

Before implementation, TC-1106 must settle quoting, physical rules, paragraph and
math edge cases, profile/version selection, discovery, limits, and migration.
Reject any design that silently changes normative text, loses IDs or claims
lossless rewriting through the failing emitter paths. If a current semantic case
cannot be represented faithfully, revise the profile and record the case before
proceeding. Retain current source/CLI behavior until the specified successor is
implemented and migration is verified. No permanent dual-authoring promise is made.
