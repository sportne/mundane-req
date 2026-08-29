# Task TC-0704: Measure Operational Scale

Status: Planned

Roadmap stage: 7

Type: Operational verification

Depends on: TC-0702 and TC-0703

Unlocks: TC-0901 and TC-1001

## Question

Do native startup, scan behavior, memory use, diagnostic volume, and trace output remain practical at the selected corpus scale?

## Outcome

Reproducible measurements identify whether the disposable in-memory architecture remains adequate and where usability, rather than raw performance, becomes limiting.

## Work

- Measure clean and repeated validator, formatter-check, and representative trace invocations.
- Record corpus size, file count, relationship count, platform, GraalVM version, and command.
- Exercise large diagnostic sets and deep or branching traces.
- Measure both file layouts where their I/O behavior differs.
- Compare against human workflow tolerances rather than inventing product-scale targets.

## Acceptance evidence

- Commands and input baseline reproduce every reported measurement.
- No cache or database is added before the evidence is reviewed.
- Output navigability is assessed alongside runtime and memory.
- Any threshold used for a decision is explained by a workflow.
- Measured bottlenecks are assigned to a bounded follow-up rather than generalized architecture.

## Out of scope

- Performance marketing claims.
- Distributed processing.
- Persistent indexing without measured need.

## Completion decision

Retain the simple in-memory model unless measurements show a concrete workflow cannot be served. If derived caching is justified, require it to remain optional and disposable.

## References

- [TC-0702](closed/task-0702-run-the-independent-interpretation-proxy-trial.md)
- [TC-0703](task-0703-run-the-multi-author-and-layout-trial.md)
