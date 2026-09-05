# Task TC-1504: Emit SARIF Validation Diagnostics

Status: Planned

Roadmap stage: 15

Type: Implementation and verification

Depends on: TC-1201, TC-1402, TC-1403

Unlocks: follow-up work selected by the completion decision

## Question

How can validation diagnostics be consumed by code-review and editor tools without
scraping the human-readable CLI output?

## Outcome

An explicit SARIF output mode represents the selected diagnostic contract with
correct ranges, rule identifiers, severity, and failure behavior.

## Work

- Select and document the SARIF specification/schema version and CLI output mode;
  map TC-1201's diagnostic rules and completeness states.
- Define artifact URIs, path roots, Unicode coordinate conversion, range endpoints,
  severity mapping, tool metadata, and deterministic multi-file ordering.
- Handle malformed source, input-selection failure, partial recovery, no findings,
  unknown options, closed output, partial writes, and flush failure.
- Preserve the default human-readable validator interface; document exits separately
  from SARIF emission success.

## Acceptance evidence

- Golden output validates against the selected checked-in or reproducibly obtained
  SARIF schema, including clean, multi-file, and invalid-input examples.
- Fixtures verify exact source ranges with non-ASCII text and supplementary Unicode
  characters, stable rule IDs, severity mapping, and portable path encoding.
- Malformed source remains a failed validation even if a valid report was emitted;
  output failures never yield success.
- A bounded fixture consumer or available viewer demonstrates source navigation;
  record exact consumer/version if an external integration is exercised.
- Human-readable default output and existing source validity checks remain covered.

## Out of scope

- Forge upload automation, a complete language server, new Unicode validity policy,
  or representing successful report generation as successful validation.

## Compatibility and affected components

Present problem: integrations otherwise parse plain diagnostic strings.
Components: Diagnostic/source spans, validator output selection, SARIF serializer,
focused tests and CLI documentation. Rule evolution follows the experimental
compatibility policy rather than silently changing rule meanings.

## Completion decision

Accept only schema-valid, source-accurate reports with tested failure behavior.
If current locations cannot support truthful ranges, preserve accurate points
temporarily with an explicit contract decision instead of inventing spans.

## References

- [Roadmap](0001-initial-roadmap.md)
- [TC-1201](task-1201-define-requirement-semantic-output.md)
- [TC-1402](closed/task-1402-report-cli-output-failures.md)
- [TC-1403](task-1403-recover-parser-diagnostics-safely.md)
- [Validator](../src/main/java/mundanereq/cli/ValidatorMain.java)
- [Source positions](../src/main/java/mundanereq/source/SourcePosition.java)
