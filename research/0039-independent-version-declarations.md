# Research 0039: Independent version declarations

Date: 2026-09-05

Decision: [versions.properties](../versions.properties) is the authoritative current
declaration location. Make reads its package version; the Python standard-library
generator emits build/maintained/generated/mundanereq/Versions.java and versions.json.
Java constants are compiled into independent native commands; runtime file loading
is unnecessary. The package carries VERSIONS.json. Generated files are disposable.

## Declaration inventory

| Consumers | Declaration / meaning |
| --- | --- |
| SourceFormat and YAML profile validator, CLI selected-source output | SOURCE_CUSTOM, SOURCE_YAML: accepted source contracts |
| Validator/formatter/trace TOOL_VERSION and migrate version output | VALIDATE_VERSION, FORMAT_VERSION, TRACE_VERSION, MIGRATE_VERSION: individual tool identities |
| Package name and VERSIONS.json | SUITE_VERSION: package identity independent of tools |
| Command documentation / generated metadata | VALIDATE_CONTRACT, FORMAT_CONTRACT, TRACE_CONTRACT identify existing trial plus safety addendum; MIGRATE_CONTRACT identifies its separate contract |
| Future compiler/output | Add distinct COMPILE_VERSION, COMPILE_CONTRACT and REQUIREMENT_ARTIFACT declarations in TC-1201/1202 |
| Historical source/CLI specifications, tags, conformance inputs and experiment results | Immutable historical identifiers, deliberately not generated from current declarations |
| README and distribution guides | Current explanatory snapshots; changes require doc review, not a new authority |
| Fixed legacy test expectations | Compatibility evidence for retained contracts; not editable current declarations |

The CLI contract labels are metadata names for existing documented behavior; they
do not change --version text or source acceptance. Command builds retain the
existing values. A package's version does not dictate its included source formats.
Make version verification rejects command-line SUITE_VERSION overrides that disagree
with the authoritative declaration. Never edit generated Java or metadata.

## Experimental change policy

| Change | Classification and consumer behavior |
| --- | --- |
| Add an optional compiled informational field under a contract that explicitly permits unknown fields | Compatible addition; readers ignore unknown informational fields but still check exact known format and completeness |
| Change a statement field's meaning or type | Breaking compiled change; assign a new output identifier, keep/reject old explicitly and document migration |
| Add optional YAML attribute syntax | Existing sources may remain valid, but strict old parsers reject unknown keys; select a new source profile or explicit extension contract after TC-1302 |
| Change an existing YAML field's meaning | Breaking source change; new source identifier and migration procedure; never reinterpret old tagged files silently |
| Package notices or layout change | Evaluate package compatibility independently; source/CLI versions do not advance automatically |

For a change: identify affected domains, state previous/new interpretation with a
worked source/output example, update relevant declarations and acceptance fixtures,
write a migration note next to the affected contract, then update current guides.
Specify which prior contracts remain accepted and the exact error for unknown
identifiers. No long-term stability promise or frozen language is implied.

## Build evidence and assumptions

| Environment | Evidence / disposition |
| --- | --- |
| Java 21.0.12 OpenJDK, Linux x86_64/WSL2 | Current `make version-verify`: all 12 maintained JVM groups and actual CLI version checks |
| GraalVM CE 21.0.2, Java 21, Linux x86_64/WSL2, gcc 13.3.0, host glibc 2.39 | Research 0035 full verification; this card repeats native package verification |
| Native x86-64 compatibility CPU, GLIBC_2.34 symbol ceiling | Make flags and package symbol/isolation checks; host glibc 2.39 is not itself a glibc 2.34 runtime test |
| Other Java/GraalVM releases, Windows native, macOS, ARM, alternate C toolchains | Untested by this card; no support result inferred from Java portability |

The Java release target stays 21. Native tools need no JVM at runtime. Build-time
Python 3 now generates declarations; pinned SnakeYAML remains the Java dependency.
This adds no build-system migration. Existing checksums and provenance packaging
remain in use; byte-identical native builds are not claimed.

## Verification

`make version-verify` compares actual JVM --version output (both source selectors)
with declarations and generated metadata, rejects deliberately stale metadata and
duplicate keys, and compiles a temporary single-domain mutation while checking
other domains remain unchanged. Native suite verification compares package
VERSIONS.json with compiled declarations and each binary's independent output.

Recorded result: `make version-verify native-suite-verify` passed with GraalVM CE
21.0.2. All 12 maintained JVM groups, independent version checks, native builds,
package metadata/binary identities, notices, symbol ceiling, archive checksums
and isolated packaged operations passed. No artifacts were published.
