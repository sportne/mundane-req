# Task TC-0404: Publish the Formatter Trial Contract

Status: Planned

Roadmap stage: 4

Type: Decision and documentation

Depends on: TC-0403

Unlocks: TC-0601

## Question

Which formatting behavior is mature enough for teams to use in branches and CI without creating surprise diffs?

## Outcome

A versioned formatter trial contract documents canonical behavior, modes, safety properties, and deliberate omissions.

## Work

- Document canonical choices and preserved source properties.
- Explain check, standard-output, and in-place behavior and exit status.
- Provide small before-and-after examples.
- Record the semantic-preservation and idempotence evidence.
- Identify the reproducible implementation baseline.

## Acceptance evidence

- An author can predict whether formatting will change a valid file.
- The contract distinguishes semantic source rules from formatting policy.
- The release does not imply comment attachment or view semantics.
- The README can point to the formatter as a maintained independent tool.

## Out of scope

- A stable 1.0 formatter promise.
- Editor integration.
- Automatic formatting on merge.

## Completion decision

Publish only if the formatter's behavior is small enough to explain completely and safe enough that ordinary diffs remain the review surface.

## References

- [TC-0403](task-0403-verify-formatter-safety-properties.md)
- [Roadmap cross-cutting rules](0001-initial-roadmap.md#cross-cutting-rules-for-every-stage)
