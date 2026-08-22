# Experiment 0005: Incoming and Transitive Trace Query

Status: Completed

Result date: 2026-08-22

Reproducible result: annotated tag `experiment-0005-result`

## Question

Can one focused tool materially improve reverse and transitive trace navigation while leaving plain `.mreq` source and Git authoritative?

## Selection

Experiment 0003 repeatedly needed exact searches for requirements that decompose a changed parent. Experiment 0004 also confirmed that relationship completeness is policy, but did not introduce a verification-plan language suitable for a coverage query.

The selected tool is therefore an incoming decomposition query over the existing semantic model. It adds no fields, indexes, caches, database, configuration, or persistent derived state.

## Interface

The Experiment 0002 GraalVM native probe now accepts:

    build/mundanereq --incoming REQUIREMENT_ID FILE_OR_DIRECTORY...

The probe first performs the ordinary parse and validation operation. If the source set is invalid or the target does not exist, it emits a diagnostic and no trace result.

For each requirement that directly or transitively decomposes the target, the output contains:

- its distance from the target;
- one shortest child-to-parent path ending at the target.

Results use breadth-first traversal with lexically sorted incoming edges. Output is deterministic for the same semantic model and is independent of file traversal order. If several equal-length paths exist, the first lexical breadth-first path is shown rather than every possible path.

## UAS result

Running the native executable against the final Experiment 0003 corpus produces:

    Incoming decomposition trace for OPS-001:
    1 SYS-002 -> OPS-001
    1 SYS-003 -> OPS-001
    1 SYS-004 -> OPS-001
    1 SYS-005 -> OPS-001
    1 SYS-007 -> OPS-001
    1 SYS-011 -> OPS-001
    2 VM-002 -> SYS-002 -> OPS-001
    2 GCA-003 -> SYS-007 -> OPS-001
    2 GCA-004 -> SYS-007 -> OPS-001
    2 GCA-001 -> SYS-011 -> OPS-001

The result exposes direct and second-level impact in one command. The equivalent manual workflow required repeated searches followed by opening and interpreting each child record.

The transferred Lift-Plus-Cruise corpus correctly reports no incoming relationships because it contains no decomposition links. The query does not confuse verification coverage, shared variables, or state-transition adjacency with decomposition.

## Tests

The dependency-free harness now passes 12 grouped tests. The focused query test covers:

- deterministic breadth-first order;
- direct and transitive paths;
- a node reachable by multiple paths;
- shortest-path selection;
- an existing target with no incoming relationships;
- command-line output;
- a missing target diagnostic.

Both JVM tests and the native-image build pass under GraalVM CE Java 21.0.2. The resulting executable remains a disposable build artifact.

## Findings

1. The semantic model already contains enough information for useful reverse trace navigation.
2. A transient in-memory reverse index is sufficient for the tested corpus; persistent indexes are unnecessary.
3. The query materially reduces a repeated navigation task without changing source ownership or syntax.
4. Paths are more informative than an unordered incoming-ID list because they expose the intervening requirements.
5. Returning one shortest path is a useful minimum. Enumerating every path, rendering graphs, comparing baselines, and evaluating project coverage policy remain separate potential capabilities.

## Disposition

The experiment succeeds. Retain `--incoming` as provisional 0.1 tooling behavior and do not add reverse links to requirement source. Stage 7 should document it as an optional derived query, not as part of source-language validity.
