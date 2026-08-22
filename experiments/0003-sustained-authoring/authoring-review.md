# Experiment 0003: Authoring and Change Review

Status: Completed evidence review

Review date: 2026-08-22

## Evidence basis

- Start: annotated tag `experiment-0003-baseline-a`, 20 requirements, 22 decomposition relationships, three source files.
- End: annotated tag `experiment-0003-baseline-b`, 21 requirements, 25 decomposition relationships, four source files.
- Review mechanisms: ordinary Git unified and word diffs, `git log`, `git show`, `rg`, and the Experiment 0002 native validator.
- Specialized editor, renderer, semantic diff, trace browser, database, and requirements-review application: none.

The six scenario commits are:

| Scenario | Commit | Post-change inventory | Immediate result |
| --- | --- | --- | --- |
| Add traced requirement | `443eee3` | 21 requirements / 23 relationships | One complete 14-line object addition; source and outgoing trace were visible together |
| Split and retarget | `08c9993` | 22 / 26 | Preserved SYS-005, added SYS-011, and retargeted GCA-001 in one module |
| Tighten timing | `53c8f2e` | 22 / 26 | Two isolated `10` to `5` ms normative edits |
| Reallocate | `0ee3a83` | 22 / 26 | One scalar allocation change with identity and normative content untouched |
| Move between files | `e1a6a81` | 22 / 26 | Semantic inventory unchanged; file count increased from three to four |
| Retire leaf requirement | `76bb6d5` | 21 / 25 | Complete MCS-001 record removed; validator remained successful |

Every committed state passed deterministic parsing, duplicate-ID checks, and dangling-reference checks.

## Scenario observations

### Addition

SYS-010 appeared as one visually complete record. Its ID, title, statement, rationale, allocation, external source, and outgoing relationship to OPS-004 were reviewable without surrounding document machinery.

There were no incoming relationships to SYS-010. Exact search established that fact, although the validator did not report it because absence of incoming links is not generally an error.

Result: ordinary object creation is adequately represented.

### Identity-preserving split

The unified diff communicated the important operations:

- SYS-005 kept its ID and retained the vehicle-identifier inclusion behavior;
- SYS-011 received the separated mismatch-rejection behavior;
- GCA-001 changed its parent from SYS-005 to SYS-011.

The word diff was less reliable as a reading aid. Git aligned similar rationale and trace lines across the new record boundary, making some unchanged SYS-005 fields appear inserted and some SYS-011 prose appear as edits to old prose. Explicit record markers still made the result recoverable, but the ordinary unified diff was clearer.

After the retarget, SYS-005 had no incoming lower-level decomposition relationship. The corpus remained valid because relationship completeness is not a syntactic property.

Result: the language handled the split, but human review must not equate word-diff alignment with semantic object matching.

### Coordinated timing change

The word diff showed exactly two `10` to `5` replacements, one in SYS-008 and one in GCA-002. Their identities, rationales, and relationships were unchanged.

Starting from GCA-002, both outgoing parents were adjacent in its record. Starting from SYS-008, finding GCA-002 required a repository search for `decomposes: SYS-008`.

Result: normative threshold changes are unusually clear; reverse impact navigation is possible with ordinary search but repetitive.

### Reallocation

SYS-004 changed from `Flight-plan manager` to `Authorization service` on one line. The diff did not imply an identity or statement change.

Nothing validates either label, relates it to a modeled component, or distinguishes a renamed organization from a transfer of responsibility.

Result: a label is adequate for visible allocation and reallocation, but not for referential component analysis.

### File movement

Before and after moving SYS-008, the normalized inventory had the same SHA-256 digest:

    77020e71880279f18cb4fcfe4d9e94f32a2104591a739b46fba4fa93b853496f

The validator continued to report 22 requirements and 26 relationships. Only the source-file count changed from three to four.

The staged Git diff showed 12 deleted lines in `link-and-recovery.mreq` and an 11-line new `timing.mreq`; the extra deleted line was a separator. Git did not classify extraction from a larger module as a rename. Before staging, ordinary `git diff` omitted the untracked destination file entirely.

Result: file boundaries are non-semantic as designed, but complete change review must use the proposed commit or staged/forge diff rather than an arbitrary unstaged diff.

### Retirement

Retiring MCS-001 was a readable 11-line record deletion. Its outgoing relationship to SYS-009 disappeared with it, and no dangling reference remained, so the validator correctly accepted the result.

The reason for retirement is in the commit body: persistence responsibility moved to an external platform outside the modeled implementation. That reason is recoverable with ordinary Git history but does not appear in the final source snapshot.

Searching history for `MCS-001` found both its baseline introduction and retirement commit. The final source also made it easy to establish that SYS-009 no longer had an incoming lower-level requirement, but no current validation rule treated that as an error.

Result: deletion plus Git history is a credible retirement mechanism. Coverage loss and mandatory change justification are policy or analysis questions, not reasons by themselves to add status or tombstone syntax.

## Baseline comparison

Baseline A to Baseline B has:

- 43 inserted and 26 deleted lines across the requirement corpus;
- added identities SYS-010 and SYS-011;
- removed identity MCS-001;
- preserved identity for every edited, reallocated, and moved requirement;
- three net additional decomposition relationships;
- one new source file with no semantic effect from its boundary.

The six commits are more informative than one aggregate baseline diff because they preserve the purpose and review unit of each change. The annotated tags provide stable endpoints without adding revision or baseline fields to individual requirements.

## Repeated friction

1. Incoming relationships require repeated exact searches because relationships are stored only in their authoritative outgoing direction.
2. Word diff may align similar text across record boundaries during a split; unified diff should remain the default review evidence.
3. Untracked destination files do not appear in an unstaged diff.
4. Syntax validation cannot decide whether a parent has enough lower-level coverage.
5. Retirement rationale lives in commit or review history unless a surviving requirement's rationale also changes.
6. Allocation and external-source values are readable but opaque labels.

Only the first item suggests an immediately plausible focused tool: a small trace query over the existing semantic model. One experiment is not enough to prioritize its implementation ahead of transferability testing.

## Overall result

The source language remained usable across all six changes without modification. Plain source, Git, search, and the validator were sufficient to understand and validate each committed state.

The experiment supports retaining Specification 0002 unchanged while separating three concerns more explicitly:

- syntax and reference validity;
- project-policy analysis such as coverage expectations;
- revision workflow such as review, approval, and retirement justification.
