# Experiment 0003: Formal Model-Pressure Review

Status: Completed assessment

Review date: 2026-08-22

## Purpose

This review asks whether formal systems-engineering workflows require changes to the provisional requirement model. It uses the committed Experiment 0003 history, the final Baseline B corpus, and a temporary ID-correction probe.

The goal is not to reproduce every field found in a requirements-management product. It is to locate the responsibility for each engineering need: requirement source, Git or forge workflow, independent analysis, project policy, or a genuinely missing model concept.

## Workflow assessment

| Workflow | Current support | Pressure or gap | Disposition |
| --- | --- | --- | --- |
| Trace to external origin | `source` and `rationale` remain visible in each applicable record | `source` is opaque and not resolvable or typed | Adequate initially; test locator fidelity on another corpus before structuring it |
| Represent self-derived requirements | SYS-008 has an external design source and no `decomposes` parent | A project may require a derivation classification or waiver | Treat as trace policy; do not add a self-derived flag |
| Follow lower-to-higher decomposition | Repeated `decomposes` lines are explicit and validated | Completeness is not implied and relationship semantics remain intentionally narrow | Keep the current relationship meaning |
| Find incoming impact | Exact search for `decomposes: ID` finds children | Repetitive and lacks transitive or baseline-aware analysis | Candidate for an independent trace query, not new syntax |
| Review normative change | Git unified and word diffs expose statement edits | Word diff can align across object boundaries during splits | Prefer unified diff; semantic diff remains optional future tooling |
| Allocate and reallocate | Optional `allocation` label changes locally | Labels have no referential identity, type, rename behavior, or controlled vocabulary | Keep the label hypothesis until another corpus requires modeled allocation targets |
| Retire a requirement | Delete the record; Git preserves content, author, reason, and revision | Final source has no tombstone; leaf deletion may silently reduce coverage | Keep status and tombstones out; define review/commit policy and coverage analysis separately |
| Establish and compare baselines | Commits and annotated tags identify exact repository snapshots | Tag existence alone does not establish organizational approval or scope | Git supplies mechanics; authority belongs to project convention or external evidence |
| Review and approve a revision | A pull/merge request reviews the proposed commit and exact source diff | Approval applies to a revision or change, not timeless requirement identity | Keep approval out of requirement fields |
| Plan verification | No minimum-language field represents method, procedure, result, or evidence | Formal teams need to plan coverage and later bind results to particular revisions | Genuine unresolved workflow; study as a focused model experiment before adding syntax |
| Assess trace completeness | Incoming and outgoing links can be derived; validator detects dangling targets | SYS-005 and SYS-009 can lose all children without becoming syntactically invalid | Add policy analysis only when a project declares expected coverage rules |
| Correct or replace an ID | Source and incoming references can be edited consistently | The model interprets the old and new IDs as different identities and has no continuity assertion | Genuine unresolved identity workflow; do not add aliases or hidden IDs yet |

## Evidence from the ID-correction probe

The temporary probe changed the opener of SYS-008 to SYS-012.

With no other edit, validation failed at the exact incoming relationship:

    link-and-recovery.mreq:84:13: dangling-reference:
    decomposes target 'SYS-008' does not exist in the selected source set

Changing GCA-002's relationship target to SYS-012 restored a valid inventory of 21 requirements and 25 relationships. The ordinary word diff then showed two direct token replacements: the record ID and the incoming target.

Nothing in the resulting model stated that SYS-012 continued SYS-008 rather than replacing it. Git history makes the editing event visible, but a tool comparing only semantic snapshots must report one removed identity and one added identity.

The probe was reverted and did not enter a baseline or commit.

### Identity alternatives left open

1. Retain the current rule: ID changes replace identity; exceptional corrections rely on reviewed Git history.
2. Add a temporary explicit rename or supersession record outside the requirement object.
3. Separate durable machine identity from the human-facing ID.
4. Permit aliases or previous IDs.

Options 2–4 add concepts and failure modes. Their value depends on how frequently formal users must correct identifiers while preserving machine continuity across exported baselines. Another corpus and ReqIF work should supply that evidence.

## Verification-planning pressure

The current statements clearly suggest different verification work:

- SYS-006 contains mathematical boundary behavior suited to analysis plus boundary testing;
- SYS-007 and GCA-004 contain response deadlines suited to timed testing;
- SYS-008 contains a time-source resolution constraint suited to inspection and test;
- SYS-010 contains operator-visible behavior suited to demonstration or test.

The language cannot record those planned methods, link a requirement to a verification procedure, state coverage, or associate evidence with the exact baseline under test.

This does not establish that `verification_method` belongs inside every requirement. Meaningful alternatives are:

- a durable repeatable attribute on the requirement;
- a typed relationship from the requirement to an externally managed verification artifact;
- a separate verification-plan source keyed by requirement ID and baseline;
- derived coverage from test metadata outside the requirements repository.

Approval and result state clearly apply to particular revisions and executions. Planned verification method may be a durable characteristic of a requirement, but the current experiment did not execute enough verification workflow to decide that question.

Disposition: make verification planning a required pressure case for the transferability corpus; do not expand Specification 0002 yet.

## Concept ownership after the review

### Intrinsic requirement source

- human-facing identity;
- title and normative statement;
- durable rationale;
- provisional allocation label;
- opaque external source reference;
- outgoing decomposition relationships.

### Repository revision

- the exact state of each requirement;
- addition, editing, movement, and deletion;
- author and timestamp;
- comparison with another snapshot.

### Review or project policy

- change justification when not retained in requirement rationale;
- required reviewers and approval authority;
- baseline scope and release meaning;
- expected decomposition and verification coverage;
- permitted identifier-change procedure.

### Independent analysis or tooling

- incoming and transitive trace queries;
- coverage and suspect-link analysis;
- allocation vocabulary checks;
- baseline-aware semantic comparison;
- verification matrices and coverage reports.

## Decisions after Stage 4

1. Keep the Specification 0002 grammar and minimum semantic model unchanged.
2. Keep review status, approval, retirement status, timestamps, and per-requirement revisions outside requirement records.
3. Continue treating baseline as a Git snapshot whose authority is established by convention or external evidence.
4. Treat decomposition completeness as project policy evaluated by independent analysis, not a universal validity rule.
5. Continue treating allocation and external source as opaque labels pending evidence from another corpus.
6. Carry verification planning and identity correction forward as the two most important unresolved model questions.
7. Treat an incoming trace query as the clearest observed tooling opportunity, but wait for transferability evidence before implementing it.

## Stage 4 conclusion

Formal traceability creates real pressure, but most of it does not require additional authoritative source syntax. The current model remains intentionally incomplete as a requirements-management product while still being sufficient as a requirements source foundation.

The two possible model gaps—verification planning and identity continuity across ID correction—are now narrow enough for focused future experiments. Expanding the language before those experiments would be premature.
