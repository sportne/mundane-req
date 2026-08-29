# Independent-Author Trial Assessment

Status: Completed case assessment

Date: 2026-08-29

## Participant boundary

The participant was a fresh Codex subagent started without conversation context.
It was instructed to access only the sealed workspace. This usefully tests
whether another interpreter can act from the written material without oral
project history, but it is not a human usability study. Agent behavior—such as
exhaustively reading 1,633 lines, using `apply_patch`, and maintaining a detailed
log—is not representative evidence about engineer time, attention, or editor
experience.

No coaching, clarification, or assistance occurred. The exact prompt,
participant-authored log, administrative manifest, and final source patch are
preserved beside this assessment. The participant branch ended clean at its
single change commit `9d41940` in the temporary trial repository; that local
hash is recorded for audit context, while `source-change.diff` is the durable
reconstruction evidence against `operational-trial-protocol-0.1`.

### Protocol deviation

The orchestration service did not provide a raw terminal/session transcript.
The participant's chronology records commands, a 42-entry interaction sequence,
the deliberate nonzero validator status, and important diagnostics and outputs,
but it paraphrases several successful outputs and does not state a numeric exit
status for every successful command. Consequently, claims about chronology,
absence of additional failed attempts, and assistance rely on the
participant-authored log rather than independent raw-session evidence. Final
source, final tool results, the log committed by the participant, and its Git
diff were independently rechecked. This deviation reduces evidence strength
but does not change the observed final correctness.

## Oracle comparison

| Task | Result | Correctness evidence | Learnability observation |
| --- | --- | --- | --- |
| Find notification bound and parent | Complete and exact | Identified `SYS-ALERT-NOTIFY-001`, 60 seconds, and `OPS-ALERT-001`; confirmed with `parents` | Source search plus trace documentation were sufficient |
| Add queue requirement | Complete and exact | Added the specified ID, statement, and parent; final `children` output includes it | Correct field order and relationship direction were learned from the standard without coaching |
| Change retention | Complete and exact | Changed only 30 to 90 days; inspected both children and correctly left their “configured retention period” prose unchanged | Participant distinguished trace impact from an instruction to edit every descendant |
| Move requirement | Complete and semantically exact | The deleted and added `EDGE-STORE-001` records are byte-identical | Multi-record movement appears as a full deletion/addition, but ordinary diff remained understandable |
| Diagnose dangling target | Complete and exact | Preserved exit 1 and the exact `dangling-reference` diagnostic, then committed only the repair | Diagnostic named file, position, code, target, and complete-set issue sufficiently for repair |
| Format, validate, trace | Complete and exact | Format check 0; validation reports 61 requirements/55 relationships; six expected component paths returned | Separate commands and outputs were correctly associated with separate purposes |
| Review source diff | Complete and exact | Correctly separated two normative edits from one non-semantic move and said tool output was not needed to read the change | Raw diff was adequate despite move noise |
| Branch and commit | Complete | One ordinary branch and one participant commit; no database, index, report, manifest, or language configuration added | Git workflow required no mundane-req-specific abstraction |

The final patch applies cleanly to the tagged corpus, remains formatter-clean,
validates at the expected counts, and reproduces the expected direct and
transitive traces.

## Observed friction by owner

### Documentation and training

The participant's first attempt to read all supplied documentation at once was
truncated. It detected the incomplete read, measured the material, and read the
951-line standard in chunks before acting. Correct use therefore did not depend
on project history, but the full normative-plus-tool package is substantial for
first contact. This one agent trial is insufficient to justify a new quickstart;
a human trial should test whether the one-purpose tool guides and examples are
enough without exhaustive standard reading.

### Tool

No unplanned tool failure occurred. The deliberately created dangling reference
was attributed to the validator and repaired from its diagnostic. Formatter and
trace behavior matched the written contracts. No wrapper or persistent state
was requested.

### Git and file layout

Moving one record between two multi-record subject files generated a complete
record deletion and addition rather than a semantic move marker. The participant
still recognized byte identity and understood the diff. This is concrete input
to TC-0703's layout comparison, not evidence for semantic merge or file-path
semantics.

### Language, model, and policy

No language or model gap was observed. The forced move placed a resilience
record in the records subject file, which may be undesirable organization but
does not alter semantics; any preferred destination is a project convention.
The retention exercise confirmed that descendant inspection does not imply
mechanical descendant editing. No new policy rule was required.

## Decision

The written 0.2 standard and tool documents were sufficient for this independent
agent proxy to produce a correct result without oral history. Retain the current
grammar and separate tools. Carry the documentation-volume question and
multi-record move noise into later human and layout evidence rather than hiding
them with a new abstraction.

This result closes the narrowed TC-0702 AI-proxy independent-interpretation
case. It does not establish human learnability, domain adequacy, broad usability,
forge review quality, or 1.0 readiness. TC-0706 retains the human-author question.
