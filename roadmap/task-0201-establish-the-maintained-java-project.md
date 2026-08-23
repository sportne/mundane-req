# Task TC-0201: Establish the Maintained Java Project

Status: Planned

Roadmap stage: 2

Type: Implementation

Depends on: TC-0104

Unlocks: TC-0202

## Question

What is the smallest maintainable Java project layout that can build shared code and separate GraalVM native executables?

## Outcome

A maintained source and test area exists outside the historical experiments, with a reproducible JVM test command and native-image build path.

## Work

- Create only the source, test, and build boundaries required by the selected lineage.
- Select a minimal build arrangement compatible with Java 21 and the installed SDKMAN GraalVM.
- Keep generated classes and native binaries disposable and ignored.
- Provide one documented clean-checkout build command.
- Preserve historical experiment builds unchanged.

## Acceptance evidence

- A clean checkout runs the maintained test skeleton on the JVM.
- A minimal native-image smoke target builds without fallback mode.
- No database, server, plugin system, or unrelated dependency framework is introduced.
- The layout leaves room for three executable entry points without merging their responsibilities.

## Out of scope

- Implementing parser behavior.
- Publishing binaries for multiple platforms.
- Declaring package or API compatibility.

## Completion decision

Retain the build arrangement only if it is simpler than the tool code it supports and can produce independent executables without duplicated semantic implementations.

## References

- [TC-0104](task-0104-select-the-maintained-implementation-lineage.md)
- [Roadmap Stage 2](0001-initial-roadmap.md#stage-2--extract-the-smallest-shared-foundation)
