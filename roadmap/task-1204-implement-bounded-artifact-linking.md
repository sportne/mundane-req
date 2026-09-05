# Task TC-1204: Implement Bounded Artifact Linking

Status: Conditional

Roadmap stage: 12

Type: Implementation

Depends on: TC-1202, TC-1203, TC-0905

Unlocks: TC-0904

## Question

Can the selected import contract resolve requirements and verification-plan
references through published compiled interfaces with deterministic failures?

## Outcome

A bounded resolver/linker implements TC-1203 for the requirement artifacts and
plan fixture contract selected by TC-0905.

## Work

- Implement explicit local import selection, qualification, expected-kind checks,
  provenance recording, and source-linked diagnostics.
- Consume serialized published artifacts in the boundary tests; internal sharing
  must not replace the interoperability test with access to parser classes.
- Build disposable forward/reverse indexes retaining authored assertion locations,
  context, and revision bindings.
- Implement the selected distinction between partial inputs, resolvable artifacts,
  relationship cycles, and unsupported build dependency cycles.
- Keep domain coverage/staleness evaluation in TC-0904; choose a library or command
  boundary based on TC-1103 rather than assuming a platform service.

## Acceptance evidence

- The contract matrix passes for missing/ambiguous/wrong-kind targets, equal IDs in
  separate imports, incompatible formats, context distinctions, and cycle cases.
- Identical recorded inputs produce identical resolved references and diagnostics.
- A two-local-repository fixture resolves without network access or hidden state;
  each linked edge can be traced to its authored reference.
- Invalid or partial imports cannot produce a result marked fully linked; input
  and output failures have documented non-success exits at any command boundary.
- Removing linker/verification outputs leaves requirement tools independently usable.

## Out of scope

- A generic domain reasoning engine, registry, server, graph database, report
  framework, or implicit satisfaction/approval inference.

## Compatibility and affected components

Implement only the selected experimental artifact formats. Components: bounded
artifact reader/resolver, reference index, compiled fixtures and diagnostics.
Human-authored identities and current requirement CLI semantics remain intact.

## Completion decision

Execute only if TC-1103 and TC-1203 select shared linking as useful. If the
verification consumer needs only a smaller resolver, implement that boundary and
record the reduced scope; supersede this card if no maintained shared capability
is justified.

## References

- [Roadmap](0001-initial-roadmap.md)
- [TC-1202](closed/task-1202-emit-compiled-requirement-artifacts.md)
- [TC-1203](closed/task-1203-define-import-and-reference-contracts.md)
- [TC-0905](task-0905-define-verification-analyzer-contract.md)
- [TC-1103](closed/task-1103-test-compilation-linking-and-rebuilds.md)
