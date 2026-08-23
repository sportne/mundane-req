# Task TC-0702: Run the Independent-Author Trial

Status: Planned

Roadmap stage: 7

Type: Usability trial

Depends on: TC-0701

Unlocks: TC-0704

## Question

Can an engineer who did not design mundane-req understand, author, repair, and trace source using only the written standard and tool documentation?

## Outcome

An independent-author trial records successful tasks, misunderstandings, repair effort, and documentation or language friction.

## Work

- Provide only the published source and tool contracts plus the selected corpus.
- Ask the participant to read, add, change, move, format, validate, trace, and review requirements.
- Capture errors and questions without coaching them toward hidden conventions.
- Compare their semantic results and Git diffs with the protocol expectations.
- Classify each issue before proposing a fix.

## Acceptance evidence

- The participant can complete or clearly fail each predefined task.
- All assistance and deviations from the protocol are recorded.
- Observed problems are tied to concrete source or tool interactions.
- The result distinguishes learnability from correctness.
- Recommendations prefer documentation or diagnostics when grammar change is unnecessary.

## Out of scope

- A broad user study.
- Changing tools during the trial to hide friction.
- Evaluating domain expertise unrelated to mundane-req.

## Completion decision

If independent use depends on oral project history, treat that as a material readiness gap before considering 1.0.

## References

- [TC-0701](task-0701-select-a-larger-corpus-and-trial-protocol.md)
- [Normative 0.2 standard](../specification/0005-mundanereq-source-language-0.2.md)
