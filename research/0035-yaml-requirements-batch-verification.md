# Research 0035: Requirements YAML batch verification

Date: 2026-09-05

The six-card batch implements the [requirements YAML contract](../specification/0010-requirements-yaml-0.3.md),
[command safety addendum](../specification/0011-tool-safety-and-yaml-commands.md),
and [authoring/migration guide](../examples/yaml/README.md). Other artifact source
formats remain independent decisions. Human-authored IDs and the existing
requirement semantic model are preserved.

## Delivered behavior

| Card | Result and evidence |
| --- | --- |
| TC-1106 | Source 0.3 YAML mapping, normative structural schema, semantic/profile rules, explicit selectors, compatibility and clause disposition in Research 0034 |
| TC-1401 | Snapshot byte/file-key checks, stop-on-write-failure with Changed/Unchanged/Unprocessed reporting, retry and temporary cleanup tests |
| TC-1402 | Shared stdout/stderr completion on all command paths, exit precedence, injected partial/flush/closed-stream failures and real JVM/native descriptor/pipe checks |
| TC-1107 | SnakeYAML Engine 3.1.1 node validation with source marks, explicit profile limits, domain references/IDs and invalid/partial model rejection |
| TC-1108 | YAML CRLF-to-LF formatting preserving all other source bytes, comments and semantic content, with check/write/native parity |
| TC-1109 | Dry-run/separate-output migration with pre-write whole-source-set equality, explicit comment relocation, no-overwrite behavior and maintained examples |

## Verification and reproduction

`make verify` is the complete local command. The recorded environment uses
GraalVM CE 21.0.2, Java 21, gcc 13.3.0 on Linux x86_64/WSL2. Native builds use
`-O0 --no-fallback -march=compatibility`. The parser jar is pinned and checksummed;
its Apache-2.0 license and dependency provenance are included in the generated
three-tool package. Migration is a separate native executable built and tested
by the same verification command, without publishing a release.

The [captured command results](../experiments/0026-yaml-requirements-batch/results/verification.txt)
record the actual final checks. Maintained tests cover:

- the unchanged historical interpretation and formatter corpora, including
  30 maintained source sets and 64 legacy files;
- 120 equal requirement values in three migration corpora and 11 files, including
  the 57-requirement pilot and 60-requirement operational example;
- an additional two-record hand-authored YAML example with folded prose, literal
  math/blank lines, flow collections, single quotes and inline comments;
- 18 golden invalid cases with rule, line and code-point column expectations,
  including a field after a supplementary Unicode character;
- 100 seeded valid identifier cases and corresponding unknown-field mutations,
  malformed UTF-8, scalar coercion/style hazards, schema/domain failures, duplicate
  keys/IDs/references, version/profile restrictions and explicit resource bounds;
- multi-error and multi-file incomplete input without misleading dangling cascades;
- migration dry-run, exact semantics/comments, output directory/file collisions,
  invalid-source rejection and deterministic mid-output failure;
- formatter edit/deletion/replacement conflicts, partial completion, cleanup, retry,
  invalid-source protection and preserved POSIX safety regressions;
- JVM/native validation, trace, formatting and migration outputs, closed stdout
  descriptors for all four commands, and actual formatter broken pipes.

`make yaml-schema-verify` independently checks Draft 2020-12 with pinned jsonschema
4.26.0 and ruamel.yaml 0.19.1: 12 valid files and 17 structural/domain distinction
cases. Duplicate-key rejection precedes schema object construction and is checked
by parser/native tests. These Python packages are verification-only; native tools
have no Python or external schema-runtime requirement.

The [reproducible regression proof](../experiments/0026-yaml-requirements-batch/README.md)
compiles commit 1c6ec7f in ignored build output. The old validator returns 0 after
stdout failure, and the old formatter overwrites a post-selection external edit.
Current tools return 2 and retain the external edit. Its captured before/after
results are separate from assertions about newly added YAML behavior.

## Compatibility, limitations and disposition

Default invocation stays custom 0.2. YAML requires the explicit selector and format
identifier; directory discovery selects only .mreq.yaml. No implicit mixed-source
fallback or new attribute schema is introduced. Historical contracts/corpora remain
preserved; their YAML copies are separately selected maintained examples.

Migration gathers each file's comments at its header, preserving their exact lines
and order. It preserves semantic text rather than original wrapping; math newlines
are escaped in quoted output. Existing output directories are rejected. An
interrupted write may leave partial new outputs, while originals remain intact.
This is documented recovery behavior, not an all-files transaction.

Formatter snapshot checks leave a check-to-rename race and depend on available
filesystem identity information. YAML formatting deliberately performs no structural
pretty-printing. Native evidence covers the recorded Linux/GraalVM combination;
there is no claim of independent usability, external ReqIF interoperability,
byte-identical native builds or unrestricted YAML conformance.

The remaining monorepo, ownership, compiled-output, linking, attribute and CI
cards retain their scope. The custom parser recovery card remains open; YAML
record/schema diagnostics do not complete that separate legacy-parser work.
