# Task TC-0601: Package and Document the Native Suite

Status: Ready

Roadmap stage: 6

Type: Packaging and documentation

Depends on: TC-0303, TC-0404, and TC-0504

Unlocks: TC-0602

## Question

Can users obtain and understand three separate native tools without mistaking them for one mandatory platform?

## Outcome

Reproducible Linux packages or build artifacts and concise independent documentation exist for validator, formatter, and trace.

## Work

- Produce a reproducible build of all three GraalVM native images.
- Document installation and one-purpose invocation for each executable.
- State tool and source-contract versions independently.
- Verify that generated binaries remain uncommitted and rebuildable.
- Document which ordinary editor, Git, forge, and CI responsibilities remain outside the suite.

## Acceptance evidence

- Each tool can be installed or built independently.
- Documentation never requires a database, server, or wrapper command.
- Removing one binary affects only its stated capability.
- Licenses and third-party notices are complete for distributed artifacts.
- A clean checkout reproduces the documented Linux build.

## Out of scope

- Windows and macOS packages without demonstrated users.
- An auto-updater or package manager.
- Bundling a custom editor or forge.

## Completion decision

If separate distribution creates substantial accidental duplication, share packaging mechanics without merging executable responsibilities.

## References

- [Validator contract](task-0303-publish-the-validator-trial-contract.md)
- [Formatter contract](task-0404-publish-the-formatter-trial-contract.md)
- [Trace contract](task-0504-publish-the-trace-trial-contract.md)
