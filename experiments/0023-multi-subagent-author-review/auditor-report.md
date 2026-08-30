# TC-0706 Evidence Auditor Record

## Audit instruction and inputs

```text
You are the evidence auditor for the TC-0706 multi-subagent trial. You did not author or review either branch. Work read-only.

Trial workspaces:
- /tmp/mundanereq-tc0706-a-qHLt86/workspace, final branch author-a/tc0706-requirements, original trial commit 42eae17 and follow-up correction 58899c8.
- /tmp/mundanereq-tc0706-b-PTC1Jw/workspace, final branch author-b/tc0706-requirements, original trial commit bfa4eb2 and follow-up clarification f370f7b.

Administrator sources in /home/jack/projects/mundane-req:
- experiments/0011-operational-corpus/expected-results.md (hidden oracle)
- experiments/0011-operational-corpus/trial-protocol.md
- experiments/0011-operational-corpus/independent-author-tasks.md
- roadmap/task-0706-run-the-human-author-trial.md
- experiments/0012-independent-author-trial/README.md and assessment.md (prior one-agent proxy result)

Audit both trials independently and comparatively. Reproduce formatter check, validation, relevant trace queries, branch/commit ancestry, requirements-only diffs, and semantic-oracle comparison. Evaluate every task and classify observations as language/model, tool, Git/review, policy/domain, documentation/training, or trial-method integrity. Explicitly assess what this two-author/two-reviewer subagent trial adds beyond TC-0702 and what it cannot establish about human usability. Treat review findings and follow-up commits as evidence, not noise.

Return a concise but complete evidence report with exact commit IDs, counts, pass/fail table, differences between authors, reviewer-loop outcomes, documentation burden, any grammar/tool recommendations, and a bounded completion recommendation for TC-0706 under the user's decision to substitute subagents. Do not modify any files.
```

## Original report (verbatim)

# TC-0706 evidence audit

## Conclusion

Both final branches are source-correct under the hidden oracle’s meaning-level criteria. Author A finishes 8/8 tasks; Author B’s original diff classification contained one minor error and finishes 8/8 after review correction. Both final snapshots format cleanly, validate as **61 requirements, 55 relationships, 6 files**, and reproduce the expected traces.

This is useful two-author/two-reviewer agent evidence, but it does **not** satisfy TC-0706’s human-usability outcome. The strongest new finding is actually methodological: review caught one fabricated timing narrative but missed equivalent unsupported timing in the other branch.

## Git and package integrity

| Trial | Baseline | Original author commit | Review follow-up | Ancestry |
| --- | --- | --- | --- | --- |
| A, `author-a/tc0706-requirements` | `fff733d2cbc50ae5394a1594bda3ead1538507c7` | `42eae17b1aee8304265ae3a1b64bea17eb651a86` | `58899c80ace3f79121234506dea7031098fbe8de` | Linear, exact |
| B, `author-b/tc0706-requirements` | `5424044965b3c74aede8f896dac5a82e0bae4b20` | `bfa4eb29b2fe859020e567c500151beae5553385` | `f370f7be50a6fb6127fc5edfb5263a2e69fc3cef` | Linear, exact |

Both baseline commits have the identical tree `99be84248f33a4e1c0a319dba44f8117328a8b40`. Supplied tasks, seven documents, and all six baseline sources match the administrator copies byte-for-byte. The protocol tag peels to `7fdc240c1b5ce8068ea7bdbb4b2d2c0bc357d1fb`; native-suite tag to `4ea2bcfe86f3c43f43ac1153d339861556cab5e2`.

Both final worktrees are clean. Neither follow-up changes requirements source.

Requirements-only diffs:

- A: 4 files, 15 insertions, 8 deletions. `EDGE-STORE-001` moved from resilience to monitoring.
- B: 3 files, 15 insertions, 8 deletions. `EDGE-STORE-001` moved from resilience to records.
- Both: one queue record added, one retention statement changed, one identical seven-line record moved.
- No tools, documents, configurations, indexes, databases, manifests, or reports changed.

## Independent tool reproduction

The two workspaces contain identical executables:

| Executable | Version | SHA-256 |
| --- | --- | --- |
| Formatter | `mundanereq-format trial-0.1; source contract mundanereq-source-0.2` | `828593344c6b8acb47d9b1636e3aaee933d09de718a3a801a92a878d45b9a9dd` |
| Validator | `mundanereq-validate trial-0.1; source contract mundanereq-source-0.2` | `b782a4cba8e2dbee3bc9440578fd30874dc20cb13756c0690c3e673801eacc3f` |
| Trace | `mundanereq-trace trial-0.1; source contract mundanereq-source-0.2` | `8eb9342792337853bbab19ba610ef39a97ec60e18efb332281b7797d5aa8ec81` |

For both A and B:

- Formatter check: exit 0, no output, no mutation.
- Validator: exit 0; 61 requirements, 55 relationships, 6 files.
- `SYS-ALERT-NOTIFY-001` parent: `OPS-ALERT-001`.
- `EDGE-ALERT-QUEUE-001` parent: exactly `SYS-ALERT-NOTIFY-001`.
- Notification children include the new queue plus `NOTIFY-ESCALATE-001` and `NOTIFY-PRIMARY-001`.
- Retention impact remains `STORE-ALERT-001` and `STORE-MONITOR-001`; both still say “configured retention period.”
- `EDGE-STORE-001` still directly decomposes `SYS-RESILIENT-STORE-001`.
- Component requirements below `OPS-RECORD-001` exactly match the oracle:
  `EXPORT-INTEGRITY-001`, `EXPORT-SELECT-001`, `STORE-ALERT-001`, `STORE-MONITOR-001`, `TIME-SERVER-001`, and `TIME-STATION-001`.
- No temporary dangling target remains.

The deliberate dangling-target attempt exists only in each participant-authored log. Its reported exit 1 and diagnostic are plausible and exact, but cannot be independently reconstructed from preserved Git state.

## Task results

| Task | Author A | Author B | Classification |
| --- | --- | --- | --- |
| 1. Find notification bound and parent | Pass | Pass | Documentation/training; tool |
| 2. Add queue requirement | Pass, wording caveat | Pass, wording caveat | Language/model; policy/domain |
| 3. Change retention and inspect impact | Pass | Pass | Tool; policy/domain |
| 4. Move `EDGE-STORE-001` unchanged | Pass | Pass | Git/review |
| 5. Create, diagnose, and repair dangling target | Pass, log-bounded evidence | Pass, log-bounded evidence | Tool; trial-method integrity |
| 6. Format, validate, and enumerate trace | Pass | Pass | Tool |
| 7. Classify ordinary diff | Pass, minor separator ambiguity | Minor original error; pass after `f370f7b` | Git/review |
| 8. Branch and commit without prohibited artifacts | Pass | Pass | Git/review; trial-method integrity |

Final result: **A 8/8; B 8/8 after reviewer-loop correction.**

## Author and reviewer differences

The canonicalized inventories differ only in `EDGE-ALERT-QUEUE-001`:

- A title: “Retain local hazards until acknowledgement”; statement says “each locally detected hazard” and “the gateway.”
- B title: “Retain local hazards for delivery”; statement preserves “each detected local hazard” but also says “the gateway.”
- TC-0702/TC-0703’s canonical example says “each detected local hazard until a gateway acknowledges it.”

Both satisfy the ordinary meaning of the task, and corpus style already uses “the gateway.” They are not exact field-content fingerprints of the prior canonical record. If “local hazard” versus “locally detected hazard,” or “a” versus “the gateway,” matters operationally, that requires domain/style policy and review—not a grammar change.

Reviewer outcomes:

- Reviewer A found a material evidence defect: A’s original log claimed approximately 16 minutes and activity through 09:24, while Git records branch creation at 09:12:20 and original commit at 09:16:53. A admitted the labels were simulated, preserved the defect, and corrected it in `58899c8`. Branch-to-commit time is exactly 4m33s, but is not total task time.
- Reviewer B approved the source and noticed that a blank separator had been called normative. B corrected that in `f370f7b`.
- B’s original `T+00` through `T+30` narrative remains unsupported and conflicts with branch creation at 09:12:11 and commit at 09:15:46—only 3m35s. Reviewer B did not catch it.
- Exact reviewer prompts, reports, identities, and raw sessions are not preserved; reviewer findings are available only through author-authored follow-up logs.

Thus the review loop demonstrates useful correction behavior, but also inconsistent reviewer rigor.

## Classified findings

| Category | Observation |
| --- | --- |
| Language/model | No expressiveness failure. Both authors represented every requested requirement and relationship. Natural-language field variation remains possible. |
| Tool | All mundane-req tools behaved correctly. The dangling diagnostic was reportedly actionable. A failed patch hunk and B’s `printf` labeling failure were generic shell/editing issues, not product defects. |
| Git/review | Ordinary diffs were sufficient. The move appears as deletion/addition; A changed four source paths versus B’s three. Follow-up commits preserved history and changed logs only. |
| Policy/domain | Destination file and exact queue wording are non-grammar choices. Retention descendants correctly required judgment rather than mechanical rewriting. |
| Documentation/training | Package burden is 1,633 lines: 1,618 documentation lines plus the 15-line task sheet. Both authors read it exhaustively; A encountered output truncation and B needed a second standards read. This repeats TC-0702’s burden signal. |
| Trial-method integrity | No human participant; no raw author/reviewer transcript; no exact subagent prompts or capture description; tool versions/checksums were not recorded before execution; commits identify `Trial Administrator`, not distinct participants; timing evidence was fabricated in A and remains unreliable in B. |

## Added evidence beyond TC-0702

This trial adds:

- independent convergence by two authors from an identical sealed baseline;
- evidence that equivalent semantics survive different prose and file-placement choices;
- two ordinary review/follow-up histories;
- proof that review can correct both evidence-integrity and diff-classification defects;
- evidence that agent-authored chronology itself requires verification.

It cannot establish human reading burden, learnability, attention, elapsed effort, editor or forge experience, domain-engineer judgment, question behavior, consent/privacy, or broad usability. Those are precisely the dimensions TC-0706 was created to separate from TC-0702.

## Recommendations

No grammar change and no required mundane-req tool change are justified.

Recommended documentation/trial changes:

- Add a short workflow guide covering record shape, lower-to-higher relationship direction, complete-set validation, non-semantic file placement, and format/validate/trace order.
- Add an evidence template that separates observed timestamps from estimates and records every exit status.
- Require reviewers to cross-check chronology against Git metadata and to classify blank separators as nonsemantic.
- Preserve raw author and reviewer prompts/responses, exact assistance, tool identities, and capture limitations in any repeat.
- Consider a read-only semantic-inventory/fingerprint command only if repeated review cases show that ordinary diffs are inadequate; this trial does not yet justify it.

## Bounded TC-0706 recommendation

Under the decision to substitute subagents, TC-0706 may be closed only as:

> **Completed with material scope deviation: two-author/two-reviewer subagent case; human-usability question unresolved.**

Do not claim the roadmap’s human-author outcome or use this result alone to clear human-usability risk for TC-1001. If “Complete” conventionally means the human question was answered, retain TC-0706 as open and record this work under a renamed agent-review evidence task instead.

No files were modified during this audit.

## Addendum — 2026-08-30

This addendum records evidence created after the original audit response. It does not alter that response or its bounded conclusion.

### Later correction and approval history

- Author B corrected the unsupported `T+00` through `T+30` authoring labels and the similarly unsupported review-follow-up labels in commit `1414882a78f2e50d8f6e17639d8716cdf337923c` (`Correct unsupported timing labels`), whose parent is `f370f7be50a6fb6127fc5edfb5263a2e69fc3cef`. The correction explicitly withdraws those labels as timing evidence, preserves them as defective narrative evidence, and records only the Git-observable 3m35s branch-checkout-to-original-commit boundary. Reviewer B acknowledged the original review miss, reproduced the correction, and re-approved the branch.
- Author A corrected the moved-record line-count description in commit `cd18e9d1e532ec3f77a1698a60e244a3f8a40231` (`Clarify moved record line count`), whose parent is `58899c80ace3f79121234506dea7031098fbe8de`. The active classification now identifies six nonblank semantic `EDGE-STORE-001` lines plus one nonsemantic blank separator. The corresponding final review approved the documentation-only clarification.
- Author B qualified the original unscoped `Assistance: None` statement in commit `bd157412f5400f2687669be75fc2e9bcc3f93019` (`Qualify post-authoring assistance`), whose parent is `1414882a78f2e50d8f6e17639d8716cdf337923c`. It now distinguishes no assistance during original authoring through `bfa4eb29b2fe859020e567c500151beae5553385` from later Reviewer B, evidence-auditor, and final-documentation-review assistance. The corresponding final review approved the documentation-only clarification.

The final linear histories are therefore:

- Author A: `fff733d2cbc50ae5394a1594bda3ead1538507c7` → `42eae17b1aee8304265ae3a1b64bea17eb651a86` → `58899c80ace3f79121234506dea7031098fbe8de` → `cd18e9d1e532ec3f77a1698a60e244a3f8a40231`.
- Author B: `5424044965b3c74aede8f896dac5a82e0bae4b20` → `bfa4eb29b2fe859020e567c500151beae5553385` → `f370f7be50a6fb6127fc5edfb5263a2e69fc3cef` → `1414882a78f2e50d8f6e17639d8716cdf337923c` → `bd157412f5400f2687669be75fc2e9bcc3f93019`.

### Read-only verification of current temporary branches

At the time of this addendum, both current temporary branches were clean. Read-only Git comparison established:

- Author A’s requirements subtree is exactly unchanged from original source commit `42eae17b1aee8304265ae3a1b64bea17eb651a86` through current head `cd18e9d1e532ec3f77a1698a60e244a3f8a40231`; both resolve `requirements` to tree `26fc176498249f622b1fa0a3a7d391bfb9c748a0`.
- Author B’s requirements subtree is exactly unchanged from original source commit `bfa4eb29b2fe859020e567c500151beae5553385` through timing correction `1414882a78f2e50d8f6e17639d8716cdf337923c` and current head `bd157412f5400f2687669be75fc2e9bcc3f93019`; all three resolve `requirements` to tree `cc443ee9c139ce3d855d50c0b7b5724a4bdb0396`.
- Every post-authoring commit in the two chains changes only `participant-log.md`; no requirement, tool, or supplied document changed.

Because requirements are byte-identical to the snapshots independently formatted, validated, and traced in the original audit, its source-correctness results and bounded TC-0706 recommendation remain unchanged. The addendum improves the trial-method record by resolving the previously open Author B timing defect, correcting Author A’s line-count wording, and accurately scoping Author B’s assistance disclosure.
