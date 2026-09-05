# Task TC-1402: Report CLI Output Failures Consistently

Status: Ready

Roadmap stage: 14

Type: Implementation and verification

Depends on: none

Unlocks: TC-1504

## Question

How can each CLI avoid reporting successful completion when required command
output or diagnostics could not be delivered?

## Outcome

Validator, formatter, and trace report output failures consistently while
preserving their independent command interfaces.

## Work

- Reproduce ValidatorMain's unchecked PrintStream output for help, version, and
  successful validation. Reuse formatter/trace finishOutput behavior where applicable.
- Audit stdout and stderr completion paths, including diagnostic-only failures;
  decide precedence when source nonconformance and diagnostic delivery both fail.
- Specify broken-pipe handling, a stream failing after a prefix, flush failure,
  already-closed streams, and the case where both streams are unusable.
- Extend existing output-failure tests rather than duplicating covered formatter
  and trace cases; make the selected behavior usable by future artifact emitters.

## Acceptance evidence

- Injected output/flush failures cannot yield a success exit from any tested
  help/version/normal-output path; operational delivery failures use documented exits.
- Partial-write and diagnostic-stream cases assert exit behavior and available
  fallback reporting without requiring writing to an already failed stream.
- A process-level JVM/native check exercises a closed pipe where supported and
  records the chosen platform behavior.
- Existing successful output and source-versus-input failure distinctions remain
  verified; tests demonstrate the validator regression before its repair.

## Out of scope

- New output formats, refactoring every CLI into one application, or changing
  language validity to accommodate presentation failures.

## Compatibility and affected components

Tighten current false-success behavior with explicit trial-contract notes.
Components: the three CLI classes, focused tests, contracts, and future shared
output helpers only where they prevent inconsistent error handling.

## Completion decision

Close when every required output path has a verifiable completion policy. Record
platform-specific pipe limitations rather than assuming an in-memory failing
stream proves all native behavior.

## References

- [Roadmap](0001-initial-roadmap.md)
- [Validator](../src/main/java/mundanereq/cli/ValidatorMain.java)
- [Formatter output tests](../src/test/java/mundanereq/cli/FormatterMainTest.java)
- [Trace output tests](../src/test/java/mundanereq/cli/TraceMainTest.java)
