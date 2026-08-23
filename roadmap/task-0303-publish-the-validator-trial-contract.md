# Task TC-0303: Publish the Validator Trial Contract

Status: Planned

Roadmap stage: 3

Type: Decision and documentation

Depends on: TC-0302

Unlocks: TC-0601

## Question

Which validator behaviors is the project prepared to present as the first maintained tool interface?

## Outcome

The repository documents, versions, and identifies a trial release of `mundanereq-validate` without extending language stability promises to accidental CLI details.

## Work

- Document installation, invocation, exit behavior, supported source contract, and diagnostic expectations.
- Separate normative source conformance from provisional command-line behavior.
- Record limitations and unsupported policy checks.
- Tag or otherwise identify the reproducible maintained implementation baseline.
- Update README references away from the validation role of the experiment probe.

## Acceptance evidence

- A new user can validate a clean repository from the documentation.
- Language, tool, and Java API compatibility boundaries are stated separately.
- The native binary remains derived and reproducible.
- The release record links its complete test evidence.

## Out of scope

- Formatter or trace packaging.
- A stable 1.0 tool promise.
- Multi-platform binary distribution without demonstrated users.

## Completion decision

Publish as a trial tool only if its supported behavior and limitations can be stated more simply than the experiment it replaces.

## References

- [TC-0302](task-0302-verify-validator-behavior.md)
- [Project README](../README.md)
- [Roadmap Stage 10](0001-initial-roadmap.md#stage-10--decide-whether-to-stabilize-10)
