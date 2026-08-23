# Task TC-0204: Port Tests and Prove Native Tool Boundaries

Status: Ready

Roadmap stage: 2

Type: Implementation and architecture verification

Depends on: TC-0203

Unlocks: TC-0301, TC-0402, and TC-0502

## Question

Does the shared foundation preserve earned behavior while supporting three genuinely separate native executables?

## Outcome

Existing regression evidence runs against maintained code, and minimal validator, formatter, and trace entry points build as independent native images.

## Work

- Port semantic inventory, invalid-source, discovery, comment, math, and relationship tests.
- Compare maintained results with the historical probe over every project corpus.
- Add minimal executable entry points without implementing later card scope.
- Build each entry point separately with GraalVM Native Image.
- Verify no executable calls another executable at runtime.

## Acceptance evidence

- All ported JVM tests pass.
- All three native images build without fallback mode.
- Each executable can be removed without preventing the other two from starting.
- Historical experiments remain reproducible.
- Shared code contains no speculative plugin or metamodel layer.

## Out of scope

- Final CLI design.
- Formatter or trace feature implementation.
- Distribution packaging.

## Completion decision

Proceed to product tools only if shared behavior remains identical and the separate native boundaries do not require duplicated parsing.

## References

- [TC-0203](task-0203-extract-the-semantic-parser-and-diagnostics.md)
- [Deterministic interpretation results](../experiments/0002-deterministic-interpretation/README.md)
- [Bounded ReqIF experiment](../experiments/0006-reqif-interchange/README.md)
