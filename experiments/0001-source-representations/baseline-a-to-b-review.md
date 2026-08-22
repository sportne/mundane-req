# Experiment 0001: Baseline A-to-B Review

Status: Accepted experimental change

## Review basis

- Baseline A: commit `8ea6e5f`, annotated tag `experiment-0001-baseline-a`.
- Baseline B: the proposed safety-driven change defined in Research 0004, followed by the correction recorded below.
- Review mechanism: ordinary staged Git diffs, including unified and word-level views.
- Specialized parser, renderer, semantic diff, and requirements-review software: none.

The review concerns illustrative experiment source. Acceptance does not indicate real requirements approval, certification, or engineering suitability.

## Semantic result

Every representation contains:

- 20 requirements;
- 22 outgoing decomposition relationships;
- the same requirement IDs and byte-identical record bodies;
- the same authored requirement order;
- the unchanged SYS-006 mathematical payload.

The diff makes the intended semantic changes visible:

- SYS-007 retains identity and changes its response deadline from 250 ms to 100 ms;
- the recording clause moves from SYS-007 into new requirement SYS-009;
- new lower-level requirement GCA-004 allocates 50 ms to the adapter;
- MCS-001 changes only its outgoing relationship, from SYS-007 to SYS-009;
- SYS-008 moves in authored order without changing its record;
- GCA-003 remains unchanged and continues to decompose SYS-007.

## Human impact-review disposition

The unchanged GCA-003 to SYS-007 relationship was reviewed because SYS-007's deadline and scope changed.

Disposition:

- GCA-003 remains relevant because priority handling still supports prompt first transmission;
- GCA-004 adds the explicit adapter deadline needed by the tighter system response;
- no GCA-003 source change is required;
- no persistent suspect flag is added to requirement source.

This disposition belongs to this review record rather than to an intrinsic requirement status field.

## Review correction

The proposed diff initially left SYS-007 titled “Loss-of-link response and record” after its recording behavior moved to SYS-009. The mismatch was obvious in the ordinary source diff.

The accepted correction changes the SYS-007 title to “Loss-of-link response” in all candidates and records that change in Research 0004. SYS-007 identity remains unchanged.

## Diff observations

Before counting this review document and research clarification, the accepted candidate-source diff has these sizes:

| Candidate | Requirement-source organization | Requirement-source lines added/deleted | View lines added/deleted | Observation |
| --- | --- | --- | --- | --- |
| A | Three subject modules | 30 / 6 in one changed module | 3 / 1 | Related edits are colocated and coherent, though the relationship-only MCS-001 edit shares a larger file diff. |
| B | One requirement per file | 28 / 6 across four changed or new requirement files | 3 / 1 | New objects and the relationship-only edit are isolated clearly; reviewing the change involves more files. |
| C | One Markdown-authored specification | 48 / 20 in one document | Colocated with requirement source | Moving unchanged SYS-008 appears as a 14-line deletion and 14-line addition, making the diff substantially larger. |

Candidate B's new files did not appear in an unstaged `git diff`; they appeared when the proposed commit was staged. This is ordinary Git behavior for untracked files and reinforces that the commit or pull request, not an arbitrary unstaged diff, is the review unit.

The Candidate C movement noise does not obscure SYS-008 identity because its ID is explicit, but it demonstrates the cost of coupling authoritative objects to authored document position. Candidate A and B express the same movement as one removed and one added ID-reference line in their disposable view fixture.

## Current conclusion

All three representations remain viable enough for further experiment. This single change provides initial evidence that:

- Candidate A favors contextual locality;
- Candidate B favors object-level change isolation;
- Candidate C favors direct authored reading but creates larger diffs when document placement changes.

These observations do not yet justify selecting a representation. A realistic concurrent-edit and merge experiment remains necessary.
