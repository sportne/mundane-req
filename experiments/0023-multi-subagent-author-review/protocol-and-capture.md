# Protocol and Capture

## Scope decision

The project owner directed that subagents be used instead of recruiting a human
participant for TC-0706. The frozen TC-0701 task wording and oracle were
retained so the result remains comparable with TC-0702. Two independent author
cases and two independent reviews were used to add convergence and review
evidence beyond the earlier single-agent proxy.

## Sealed package

Each author received a separate Git repository containing only:

- the frozen eight-task sheet;
- the 60-requirement operational corpus;
- source-language standard 0.2;
- validator, formatter, and trace trial contracts and concise guides; and
- copied GraalVM native executables for the three tools.

The repositories had identical starting trees but distinct baseline commits.
No oracle, roadmap, project history, research record, or conversation context
was supplied. Each author agent was spawned without inherited thread context.

## Author instruction

Each author was told to work only in its sealed workspace, read `TASKS.md` and
the supplied documentation, create an ordinary branch, complete every task,
and use only the copied native executables. Each was required to create a
chronological participant log recording reasoning, commands, source edits,
failed attempts, diagnostics, questions, assistance, and timing information;
to leave supplied tools and documents unchanged; and to commit repaired source
and the log. The two prompts differed only in author label, workspace path, and
requested branch label.

## Review instruction

Each reviewer was spawned without inherited context and told to work read-only
in one completed author repository. It received the author branch and commit,
the task sheet, and the same supplied documentation, but not the oracle. It was
asked to assess all eight tasks, reproduce format/validation/trace checks,
classify findings, decide whether the ordinary diff was sufficient, state any
author questions, and return approve/request-changes/cannot-decide.

Review findings were sent back to the corresponding author. Authors used new
follow-up commits rather than amending trial commits. Reviewers then rechecked
the follow-ups. A separate auditor received both repositories, the withheld
oracle, the frozen protocol, TC-0706, and the earlier TC-0702 evidence.

## Capture

Durable capture includes final participant logs, requirements-only patches,
complete post-baseline format-patch commit chains, commit identities and
ancestry, tool identities, exact reviewer transcripts, the evidence-auditor
report, administrator reproduction, and capture limitations. `reviews.md`
and `assessment.md` provide the concise interpretation.

Raw model session transcripts and internal reasoning were not available. The
transient dangling-reference states were not committed and therefore are
supported only by participant-authored logs. Wall-clock timing was not captured
externally. Both authors initially supplied simulated timing narratives; those
were corrected after Git-metadata review and are not used as effort evidence.

## Stop and decision rules

No source, documentation, or executable was changed during authoring or review.
A grammar or tool recommendation required a preserved workflow failure that
could not be handled by existing source, tools, Git, project policy, or
documentation. A successful agent case was never interpreted as human or
statistical usability evidence.
