# Subagent Invocations

All agents were spawned without inherited conversation context.

## Author A

Workspace: `/tmp/mundanereq-tc0706-a-qHLt86/workspace`

> You are Author A in an independent requirements-source trial. You have no
> mundane-req project history. Work only inside the workspace; do not inspect
> its parent directory, the original project repository, roadmap, research
> records, or conversation context.
>
> Begin by reading TASKS.md and the supplied files under docs/. The three
> supplied native executables are under bin/ and must be invoked by relative
> path. Create an ordinary Git branch and complete every task in TASKS.md using
> only the supplied materials.
>
> Create participant-log.md and keep a chronological record of your reasoning,
> every command, every source edit, every failed attempt or diagnostic, all
> questions, and any assistance (write “none” if there was none). Include
> approximate elapsed time or timestamps for major steps. Do not alter supplied
> tools or documentation. Do not seek outside information or coaching unless
> the task is impossible from the supplied material. Commit your final repaired
> source and log on your branch.
>
> In your final response state the branch, commit, completed and failed tasks,
> files changed, questions asked, assistance received, and your assessment of
> source readability, documentation burden, diagnostics, formatting, trace use,
> Git diff, and reviewability.

Author B received the same instruction with label B and workspace
`/tmp/mundanereq-tc0706-b-PTC1Jw/workspace`.

The request for approximate elapsed time was a protocol defect because no
external timing capture existed. Both agents generated simulated narratives.
Those labels are preserved and withdrawn in the final author logs.

## Independent reviewers

Each reviewer received this instruction with its author-specific workspace,
branch, and commit:

> You are Reviewer in an independent requirements-source trial. You have no
> mundane-req project history and did not author the change. Work read-only
> inside the supplied workspace; do not inspect its parent, the original
> repository, roadmap, research records, or conversation context.
>
> Review the author branch at the supplied commit against main using ordinary
> Git commands. Read TASKS.md and only the supplied docs as needed. Inspect the
> ordinary source diff, participant-log.md, formatter/validator results you
> reproduce, and relevant trace queries. Do not modify files or commits.
>
> Report whether each of the eight tasks is visibly satisfied; any correctness,
> language/model, tool, Git/review, policy/domain, or documentation/training
> finding with file/line evidence; whether normative changes and the
> nonsemantic move are understandable from the ordinary diff without generated
> output; any question for the author and an approve/request-changes/
> cannot-decide verdict; and commands run plus approximate review effort. Do
> not use an oracle or infer hidden project conventions.

Reviewer A evaluated Author A commit
`42eae17b1aee8304265ae3a1b64bea17eb651a86`. Reviewer B evaluated Author B
commit `bfa4eb29b2fe859020e567c500151beae5553385`.

## Review follow-ups

The administrator sent reviewer findings to authors without editing their
workspaces. Authors were instructed not to amend or conceal prior evidence, to
use follow-up commits, and to rerun relevant checks. Reviewers then received
the new commit identity and were instructed to reproduce the correction and
give a final verdict.

After initial review, the separate auditor found that Reviewer B had missed the
same timing-evidence defect found in Author A. The Author B review was reopened;
Author B made a second evidence-only follow-up, and Reviewer B explicitly
acknowledged the miss before final approval.

## Evidence auditor

The auditor received both workspace paths and complete commit chains, plus
these administrator sources:

- the withheld oracle;
- the frozen Stage 7 protocol and task sheet;
- TC-0706; and
- Experiment 0012's prior proxy result.

It was instructed to reproduce tools and Git evidence, compare both cases
independently and comparatively, classify observations, identify what was new
beyond TC-0702, state what agent evidence could not establish about humans, and
recommend a bounded TC-0706 disposition. It worked read-only.
