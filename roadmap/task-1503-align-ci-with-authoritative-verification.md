# Task TC-1503: Align CI with Authoritative Verification

Status: Ready

Roadmap stage: 15

Type: Implementation and verification

Depends on: none

Unlocks: follow-up work selected by the completion decision

## Question

How can repository CI exercise the documented complete verification command rather
than only the current example requirements workflow?

## Outcome

CI executes the authoritative verification command with attributable failures in
a documented supported clean environment.

## Work

- Compare .github/workflows/requirements.yml with Makefile's verify prerequisites:
  the workflow currently runs native-suite and selected example/CI checks.
- Preserve the useful independent-tool demonstration while ensuring all required
  verification targets run. Use explicit environment/toolchain setup and record
  actual commands.
- Verify that corpus failures and a failure in a currently omitted verification
  target fail CI. Avoid duplicated expensive native builds where dependency reuse
  suffices; measure before broader performance optimization.
- Reuse package checksum, notice, installation and isolation smoke checks already in
  native-suite-verify. Identify additional provenance or packaging work only for a
  demonstrated missing input, not to recreate completed TC-0601.
- Document the exact local equivalent and tested operating-system/toolchain
  assumptions; account for required Ruby and native utilities as well as Java.

## Acceptance evidence

- A clean supported environment runs the same complete gate locally and in CI;
  captured logs identify toolchain versions, invoked targets, and exit status.
- Deliberate example-source and omitted-target failures are attributed and cause
  failure; the evidence describes the injected cases and cleanup.
- Build outputs remain disposable, required checks cannot be silently skipped,
  and existing executable independence remains verified.
- Any cache or deduplication change has a recorded correctness check; no untested
  platform or byte-identical native-build claim is introduced.

## Out of scope

- Publishing artifacts, new operating-system packages without a user need,
  replacing the build system, or adding an approval process.

## Compatibility and affected components

Present problem: hosted checks do not cover the repository's complete gate.
Components: requirements workflow, authoritative Make targets, CI verification
tests, build-environment and contributor documentation. Coordinate path changes
with TC-1104; that optional move is not a prerequisite for closing today's gap.

## Completion decision

Complete on recorded full-gate and deliberate-failure evidence. If an environment
cannot run a required check, report and fix that limitation rather than relabeling
the partial workflow as complete.

## References

- [Roadmap](0001-initial-roadmap.md)
- [Current workflow](../.github/workflows/requirements.yml)
- [Makefile](../Makefile)
- [Existing CI card](closed/task-0602-create-the-clean-checkout-ci-workflow.md)
- [Packaging evidence](../research/0017-native-suite-packaging.md)
