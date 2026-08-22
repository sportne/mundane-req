# Experiment 0003: Sustained Authoring and Model Pressure

Status: In progress; starting corpus established

Plan date: 2026-08-22

## Question

Does the provisional source language remain understandable and workable across a sequence of realistic requirements changes, and which formal traceability workflows expose genuine gaps in the minimum model?

Experiment 0001 tested selected diffs and merges. Experiment 0002 tested deterministic interpretation. This experiment tests repeated use without adding language features during the run.

## Starting point

The working corpus is a dedicated copy of Experiment 0001 Candidate A at Baseline B. It uses small subject modules because that layout provides useful reading context while still exercising multiple records per file. The one-record-per-file corpus remains an equivalent conformance fixture rather than the working layout.

The starting corpus contains 20 requirements and 22 decomposition relationships. It will be identified by the annotated tag `experiment-0003-baseline-a` after the dedicated copy is committed.

Experiment 0002 is preserved independently by annotated tag `experiment-0002-result`.

## Allowed tools

Use only:

- an ordinary text editor or direct source editing;
- Git status, diff, show, log, and tag operations;
- ordinary text search such as `rg`;
- the Experiment 0002 native parser and validator.

Do not add a formatter, traceability query engine, renderer, database, view parser, semantic diff, or new source-language field while executing the scenarios. Observed friction should remain evidence rather than being hidden by immediate feature work.

## Scenarios

Each scenario is one Git commit so its source diff, rationale, and trace impact can be inspected independently.

### 1. Add a traced requirement

Add `SYS-010`, requiring timely operator notification after command-link loss. Give it an allocation, rationale, external source, and decomposition relationship to `OPS-004`.

This tests ordinary creation, ID uniqueness, module placement, and outgoing trace visibility.

### 2. Split a compound requirement and retarget a child

Preserve `SYS-005` identity for vehicle-identifier inclusion. Move its rejection behavior into new requirement `SYS-011`. Retarget `GCA-001` from `SYS-005` to `SYS-011`.

This tests identity-preserving refinement, new identity, relationship impact, and whether the raw diff communicates the split.

### 3. Tighten a linked timing constraint

Change the timestamp resolution in `SYS-008` and `GCA-002` from 10 ms to 5 ms while preserving both identities and relationships.

This tests coordinated normative change and manual impact analysis across two linked requirements.

### 4. Change an allocation label

Reallocate `SYS-004` from the flight-plan manager to the authorization service without changing its statement, rationale, source, or relationships.

This tests whether allocation is visibly independent of requirement identity and whether a plain label is adequate for the workflow.

### 5. Move a requirement between source files

Move `SYS-008` unchanged from the link-and-recovery module into a new timing module.

Compare normalized inventories before and after the move. This tests the decision that file boundaries and file traversal order are non-semantic.

### 6. Retire a leaf requirement

Remove `MCS-001` because the experiment assumes its lower-level persistence responsibility has moved outside the modeled mission-control implementation.

Do not add a status field or tombstone record. Determine whether the commit, diff, and history adequately explain retirement and whether the loss of lower-level coverage is detectable by syntax validation or requires a separate policy analysis.

## Checks after every scenario

1. Run the native validator on the working corpus.
2. Record the requirement and decomposition counts.
3. Inspect the ordinary unified and word-level Git diff.
4. Identify direct outgoing relationships from each changed record.
5. Search for incoming relationships to each changed or removed ID.
6. Note whether the reason for the change is visible in durable source, commit history, or both.
7. Record any ambiguity, navigation cost, irrelevant diff noise, or temptation to add syntax.

## Formal model-pressure workflows

After the source changes, assess the resulting history against:

- derivation from an external source;
- self-derived requirements;
- lower-to-higher decomposition tracing;
- incoming and outgoing change-impact analysis;
- allocation and reallocation;
- retirement and historical explanation;
- baseline establishment and comparison;
- review and approval of a particular revision;
- verification planning and coverage;
- correction or replacement of a human-facing ID.

For each workflow distinguish:

- what the source model represents;
- what Git or a forge already supplies;
- what independent analysis or project policy could supply;
- what may require a new language or model concept;
- what remains unresolved.

## Success criteria

The experiment succeeds if:

1. every valid scenario remains readable in source and ordinary Git diff;
2. the validator interprets every committed corpus deterministically;
3. unchanged requirement identities survive splitting, reallocation, and file movement;
4. file movement preserves the normalized semantic inventory;
5. formal-workflow gaps can be stated precisely without turning them immediately into features;
6. the evidence supports a clear decision about whether Specification 0002 should remain unchanged, be narrowed, or be expanded.

## Stop conditions

Stop and revise the specification before continuing if:

- a scenario cannot be expressed without ambiguous source;
- a valid source change produces nondeterministic interpretation;
- file movement changes the semantic inventory;
- the workflow requires hidden state to understand the authoritative requirement;
- the experiment starts accumulating syntax merely to imitate an existing RM product.

## Deliverables

- a dedicated working corpus and annotated start/end tags;
- six independently reviewable scenario commits;
- a diff, trace-impact, and authoring-friction review;
- a formal model-pressure assessment;
- any resulting specification and roadmap decisions.
