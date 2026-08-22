# Experiment 0001: Concurrent-Edit and Merge Review

Status: Complete

## Purpose

This experiment tests whether ordinary Git merging preserves understandable requirement source under three small concurrent-change scenarios.

It does not attempt to select a representation from merge behavior alone. It asks where changes merge cleanly, where Git reports conflicts, and whether those conflicts can be understood and resolved without mundane-req-specific tooling.

## Method

- Common base: annotated tag `experiment-0001-baseline-b`.
- Git version: 2.43.0.
- Each side of a scenario was committed on an isolated temporary branch.
- Merges used ordinary `git merge --no-commit --no-ff`.
- The same semantic edit was made in Candidates A, B, and C.
- No parser, formatter, renderer, or semantic merge driver was used.
- Temporary branches and worktrees were removed after the observations were recorded.

The edits are deliberately illustrative. Their wording and numerical values are not proposed requirement changes.

## Scenario 1 — different requirements

One branch edits the GCA-003 rationale. The other edits the MCS-001 rationale.

| Candidate | File interaction | Result |
| --- | --- | --- |
| A — subject modules | Both edits occur in `link-and-recovery.mreq`, separated by other records | Clean merge |
| B — one requirement per file | The edits occur in separate requirement files | Clean merge |
| C — Markdown document | Both edits occur in the same Markdown file, separated by other records | Clean merge |

Both rationale changes were present after the merge in every candidate.

Observation: file-level separation is sufficient but not necessary for clean merging. Git can merge independent records in one file when their changed line regions do not overlap. Candidate B nevertheless makes the independence obvious before attempting the merge.

## Scenario 2 — competing edits to one statement

One branch changes the SYS-007 deadline from 100 ms to 80 ms. The other changes it from 100 ms to 90 ms.

| Candidate | Conflict location | Result |
| --- | --- | --- |
| A — subject modules | `link-and-recovery.mreq` | One content conflict |
| B — one requirement per file | `SYS-007.mreq` | One content conflict |
| C — Markdown document | `uas-mission-control.md` | One content conflict |

Every conflict hunk showed the same direct choice:

    <<<<<<< HEAD
      Within 80 ms after the command link is declared unavailable, ...
    =======
      Within 90 ms after the command link is declared unavailable, ...
    >>>>>>> other branch

The surrounding source exposed SYS-007 identity and the normative statement without rendering.

Observation: ordinary Git correctly refuses to choose between conflicting normative values. All three conflicts are understandable. Candidate B confines the conflict file to the affected object; A and C place the same small hunk in a larger file.

## Scenario 3 — authored move concurrent with content edit

One branch moves SYS-008 from before SYS-006 to after GCA-002 in authored order. The other changes the SYS-008 rationale.

| Candidate | Representation behavior | Result |
| --- | --- | --- |
| A — subject modules | The move edits the view; the rationale edits a requirement module | Clean merge |
| B — one requirement per file | The move edits the view; the rationale edits `SYS-008.mreq` | Clean merge |
| C — Markdown document | The move deletes and reinserts the authoritative fenced record while the other branch edits that record | Content conflict |

For Candidate C, Git retained the unedited record at its moved location and presented the edited record at its old location as one side of a conflict. A careless resolution could therefore duplicate SYS-008 or discard the rationale edit.

The conflict was resolved using plain source only:

1. remove the old-location copy and conflict markers;
2. retain one SYS-008 record at the moved location;
3. apply the edited rationale to that record;
4. confirm that each candidate contains exactly one SYS-008 with the edited rationale.

Observation: explicit stable identity made the problem discoverable, but identity alone did not make a document-coupled move merge cleanly. Separating authoritative objects from authored order prevented the conflict in Candidates A and B.

## Comparative findings

1. All three candidates handle independent, well-separated edits with ordinary Git.
2. All three expose a direct and readable conflict when two branches assign incompatible normative values.
3. One-requirement-per-file storage improves conflict-file isolation but is not required for clean independent edits.
4. Separating authored order from requirement storage materially improves move-versus-edit merging.
5. Candidate C's document coupling creates a real risk of duplication or lost edits during move conflicts.
6. A custom semantic merge engine is not justified by this experiment. Ordinary Git either merged safely or surfaced a conflict that could be resolved from readable source.

## Implication for the view question

The separate view was not important because its provisional syntax was sophisticated; it was useful because it kept presentation movement from rewriting the authoritative requirement object.

That benefit could come from a minimal authored ID list, a generated composition, or another simple mechanism. The experiment supports preserving the model/view separation while continuing to treat the current `.mview` notation as disposable.

## Limitations and next evidence

- The corpus is small.
- Only one Git version and line-based merge strategy were tested.
- The independent edits were separated enough to avoid overlapping context.
- Navigation and authoring effort were observed informally but not yet measured systematically.
- Moving a requirement between Candidate A modules remains to be tested separately from moving it in authored order.

The next useful step is to synthesize the Baseline A-to-B diff evidence and these merge results into a provisional representation decision, identifying any narrower experiment still needed before full language specification.
