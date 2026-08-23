# Task TC-0402: Implement the Formatter Executable

Status: Planned

Roadmap stage: 4

Type: Implementation

Depends on: TC-0401 and TC-0204

Unlocks: TC-0403

## Question

Can the selected formatting policy be implemented as an independent native tool over the shared concrete source representation?

## Outcome

`mundanereq-format` implements only the selected policy through safe check and output modes, with any in-place mode explicitly controlled.

## Work

- Render valid concrete source according to the selected canonical rules.
- Preserve comment text and relative order without semantic attachment.
- Preserve opaque math payloads exactly as the formatting contract requires.
- Implement check mode and standard-output behavior before or alongside in-place writing.
- Keep parsing shared while keeping rewrite logic local to the formatter.

## Acceptance evidence

- The formatter builds as its own GraalVM native image.
- It does not invoke `mundanereq-validate` at runtime.
- Invalid input is rejected with shared diagnostics rather than partially rewritten.
- Source order remains governed by the selected policy.
- No generated index or repository configuration is required.

## Out of scope

- Repair formatting of syntactically invalid files.
- Document composition or view ordering.
- Semantic editing or requirement migration.

## Completion decision

If implementation requires semantics not selected by TC-0401, stop and revise the policy rather than infer behavior from proximity or formatting convention.

## References

- [TC-0401](task-0401-run-the-formatting-policy-experiment.md)
- [TC-0204](task-0204-port-tests-and-prove-native-tool-boundaries.md)
