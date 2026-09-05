# Research 0044: Bounded artifact resolver

Date: 2026-09-05

TC-1204 implements the selected [local import contract](../specification/0014-local-artifact-imports-0.1.md)
as `mundane-link`, with a reusable small resolver in engineering.artifacts.
It consumes validated requirement and verification-plan JSON. Its Java-only boundary
compiles without requirement/parser classes or the YAML library; its native build
also omits the YAML jar. Shared generated version constants carry no source parsing.

`make link-verify` passed the full maintained JVM suite, independent native builds
and [serialized resolver matrix](../scripts/check-artifact-linking.py). The matrix
checks 57 source-linked coverage edges and reverse indexes; 18 invalid cases;
qualified and ambiguous scopes; selected/unselected contexts; legal relationship
cycles versus illegal build cycles; incomplete, wrong-kind and unknown artifacts;
missing activities/targets; duplicate JSON keys and invalid typed fields/spans;
exact pins; real closed stdout/broken pipes and JVM/native byte agreement.

Two actual temporary Git repositories contain requirements and plan source. A
recorded requirements commit is checked out detached, compiled and pinned. A later
current checkout changes retention content and compiles independently. Both artifacts
resolve through the independent plan repository without network or hidden lookup.
The fixtures' commit identities are explicitly synthetic test identities; no contributor
or review evidence is inferred. Outputs and checkouts are disposable.

JVM checks cover strict JSON numbers/Unicode/depth, prefix and flush failures,
closed stderr, changed snapshot detection and a linker-level input change immediately
before recheck. A detected change returns non-success with no successful edge list.
The filesystem race after the final check remains documented; this is no atomic
multi-file transaction. JSON reads are bounded by file/depth/aggregate limits.

The resolver deliberately performs no coverage/staleness judgment. It preserves plan
owner, context, source location and baseline/current scope on each edge; indexes are
derived. Human IDs remain identity. TC-0904 now implements the independently scoped
plan adapter and analyzer. Requirements retain independent commands/source contracts.

Reproduce with Java 21/GraalVM CE 21.0.2 on Linux x86_64: `make link-verify`.
No hosted interoperability, byte-identical native binary or release claim is made.
