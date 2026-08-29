# Task TC-0706: Run the Human-Author Trial

Status: Ready

Roadmap stage: 7

Type: Human usability trial

Depends on: TC-0701 and TC-0702

Unlocks: TC-1001

## Question

Can a systems engineer who did not design mundane-req understand, author,
repair, and trace source using only the written standard and tool documentation?

## Outcome

A human-author case records task success, misunderstandings, repair effort,
documentation use, editor/Git experience, and assistance without conflating AI
proxy behavior with human learnability.

## Work

- Recruit at least one consenting human participant with relevant engineering background and no mundane-req design history.
- Reuse or explicitly version the TC-0701 task package without revealing its oracle.
- Capture commands, statuses, diagnostics, questions, assistance, task time, and the final source diff using a participant-approved method.
- Include at least one ordinary Git branch/review cycle and record human author effort and diff-review experience that the TC-0702 proxy and TC-0703 controlled histories could not measure.
- Compare semantic results with the frozen oracle and distinguish correctness, learnability, domain judgment, and tool usability.
- Record privacy-preserving raw evidence or state every capture limitation.

## Acceptance evidence

- The participant can complete or clearly fail every predefined task.
- The exact supplied package, task wording, assistance, deviations, and capture method are auditable.
- Observations are tied to concrete source, documentation, tool, editor, or Git interactions.
- Recommendations prefer documentation or diagnostics when grammar change is unnecessary.
- The result is described as a case, not statistical usability evidence.

## Out of scope

- A broad user study.
- Treating AI-agent performance as a substitute for the human case.
- Changing source or tools during the trial to hide friction.
- Evaluating unrelated watershed-domain expertise.

## Completion decision

If correct use depends on oral project history or unreasonable reading burden,
treat that as a readiness gap before 1.0. Do not infer broad usability from one
successful participant.

## References

- [TC-0701](closed/task-0701-select-a-larger-corpus-and-trial-protocol.md)
- [TC-0702 proxy result](closed/task-0702-run-the-independent-interpretation-proxy-trial.md)
- [Experiment 0012](../experiments/0012-independent-author-trial/README.md)
