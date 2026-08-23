# Task TC-0302: Verify Validator Behavior

Status: Planned

Roadmap stage: 3

Type: Verification and trial

Depends on: TC-0301

Unlocks: TC-0303

## Question

Is the validator precise and predictable enough for ordinary editor repair and CI use?

## Outcome

Reproducible JVM and native evidence covers the normative corpus, maintained project corpora, invocation behavior, and representative repair workflows.

## Work

- Run all 0.1 and 0.2 conformance fixtures in strict selected-contract contexts.
- Validate the UAS, sustained-authoring, and FRET corpora from clean checkouts.
- Exercise representative invalid-source repair using plain diagnostics.
- Test empty selection, missing inputs, duplicate inputs, explicit files, and directories.
- Record native startup and scan behavior only far enough to identify obvious regressions.

## Acceptance evidence

- Expected fixture outcomes are automated.
- JVM and native results agree.
- Diagnostics provide enough information to repair every selected failure without a specialized UI.
- The clean-checkout command is suitable for a normal CI job log.
- Any nonconforming behavior becomes a named blocker rather than a documented caveat.

## Out of scope

- Organizational-scale performance claims.
- Formatter checks.
- Project-policy validation.

## Completion decision

Advance only if the validator can replace the experimental probe's validation role without reducing conformance or usability.

## References

- [TC-0301](task-0301-implement-the-validator-executable.md)
- [0.2 conformance suite](../conformance/0.2/README.md)
- [Transferability corpus](../experiments/0004-transferability/README.md)
