# Task TC-0504: Publish the Trace Trial Contract

Status: Ready

Roadmap stage: 5

Type: Decision and documentation

Depends on: TC-0503

Unlocks: TC-0601

## Question

Which trace operations and output guarantees are ready for independent team use?

## Outcome

A versioned trace-tool trial contract documents supported questions, relationship meaning, determinism, limitations, and reproducible native use.

## Work

- Document each operation using requirement-domain examples.
- State output ordering and cycle behavior.
- Explain that reverse links and indexes are derived and disposable.
- Separate language validity from optional policy observations.
- Identify the reproducible implementation and evidence baseline.

## Acceptance evidence

- A user can predict the trace question answered by each invocation.
- The contract does not imply arbitrary relationship support.
- The output's human-readable guarantees are explicit without freezing accidental formatting unnecessarily.
- The README can present trace as a maintained independent tool.

## Out of scope

- A stable machine-readable trace protocol.
- Visualization or browser UI.
- New relationship models.

## Completion decision

Publish only the operations demonstrated by TC-0503; retain experimental operations behind no compatibility claim or remove them.

## References

- [TC-0503](task-0503-verify-trace-graph-behavior.md)
- [Minimum decomposition semantics](../specification/0002-minimum-source-language-and-model.md#decomposition-relationship)
