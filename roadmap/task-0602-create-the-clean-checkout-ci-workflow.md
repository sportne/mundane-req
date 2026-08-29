# Task TC-0602: Create the Clean-Checkout CI Workflow

Status: Ready

Roadmap stage: 6

Type: Integration

Depends on: TC-0601

Unlocks: TC-0603

## Question

Can the three tools participate visibly and independently in an ordinary Git and CI workflow?

## Outcome

A reproducible example workflow formats or checks, validates, and runs selected trace analysis from a clean checkout using explicit commands.

## Work

- Create a small example or use the project corpus as the workflow target.
- Run formatter check, validation, and trace commands as separately named steps.
- Fail CI on formatting or conformance according to documented exit behavior.
- Retain ordinary text logs and avoid committing indexes or reports.
- Document local equivalents for authors before pushing a branch.

## Acceptance evidence

- The workflow succeeds from a clean checkout.
- A deliberate failure in each tool is attributed to the correct step.
- No tool requires network state after installation or hidden repository data.
- Removing trace analysis does not prevent formatting or validation.
- All generated output is disposable.

## Out of scope

- A custom review queue.
- Mandatory pre-commit hooks.
- Forge-specific semantic diff UI.

## Completion decision

Add a wrapper or configuration file only if the explicit workflow demonstrates repeated material friction and the added mechanism remains optional.

## References

- [TC-0601](task-0601-package-and-document-the-native-suite.md)
- [0.2 CI trial guidance](../specification/0006-provisional-0.2-contract.md#ci-trial-guidance)
