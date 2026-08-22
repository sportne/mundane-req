# Research 0006: Non-Markdown View Notation

Status: Working experiment fixture; not a language decision

## Purpose

This study asks:

> What is the smallest plain-text notation that can place independent requirements into an authored specification order with contextual prose?

It applies only to Candidates A and B. Candidate C uses its Markdown document structure as the authored view and will be studied separately.

The view must remain visibly subordinate to requirement source. It selects and orders requirements; it does not own their identity, statements, rationales, allocations, sources, or relationships.

## Behaviors required by the experiment

The frozen corpus requires a view to express:

- a human-facing specification title;
- introductory contextual prose;
- a sequence of named sections;
- optional contextual prose within a section;
- ordered references to authoritative requirements;
- a view-only reordering that does not edit a requirement record;
- placement of newly added requirements without copying their content.

The initial experiment does not demonstrate a need for:

- stable view identity;
- nested sections;
- numbered sections;
- generated table-of-contents state;
- styles or rendering directives;
- conditional inclusion;
- queries or filters;
- reusable prose fragments;
- requirement-title annotations beside references;
- comments with separate semantics;
- multiple views in one file.

The experiment will therefore use one view per file. The file has a title but no independent semantic ID because nothing currently refers to a view as a stable model object. If a later workflow needs view-to-view references, reusable views, or view baselines independent of repository state, that decision can be reopened.

Requirement references contain only IDs. Repeating titles beside IDs would improve local orientation but create a second copy that can become stale. The resulting navigation cost is useful evidence for the experiment rather than a reason to add synchronization machinery in advance.

## Relationship to the selected record syntax

Research 0005 selected keyword records with labeled fields and indented multiline bodies for the Candidate A/B experiment. The view sketches reuse those few lexical ideas where useful:

- lowercase structural keywords;
- labeled single-line values;
- indented multiline prose;
- spaces rather than tabs;
- explicit outer boundaries.

They do not assume that a requirement record and a view have the same semantic fields.

## Sketch 1 — flat section sequence

### Shape

A file begins with 'view' and ends with 'end view'. A 'section:' line starts a section; the next 'section:' or 'end view' ends it. Requirement references and context fields belong to the current section by physical sequence, without nested indentation.

    view
    title: UAS Mission-Control Requirements
    context:
      This view presents the illustrative mission-control requirements in an order intended for engineering review. Requirement records remain authoritative.

    section: Purpose and operational context
    context:
      These requirements define the mission boundary, identity constraints, flight-plan authority, and loss-of-link response.
    requirement: OPS-001
    requirement: OPS-002
    requirement: OPS-003
    requirement: OPS-004

    section: Vehicle identity and activation
    requirement: SYS-001
    requirement: VM-001

    requirement: SYS-002
    requirement: SYS-003
    requirement: VM-002

    section: Flight-plan authorization and dispatch
    requirement: SYS-004

    requirement: SYS-005
    requirement: GCA-001

    section: Command-link monitoring
    requirement: SYS-006
    requirement: GCA-002
    requirement: SYS-008

    section: Loss-of-link response
    requirement: SYS-007
    requirement: GCA-003
    requirement: MCS-001

    section: Traceability orientation
    context:
      Outgoing decomposition relationships are authoritative in requirement records and can be inspected by searching for decomposes: lines. A disposable report may present the same relationships as a matrix or summary.
    end view

Blank lines may visually group adjacent references but have no model meaning. A section may have context, requirement references, or both. The experiment does not permit content before the first section other than the view title and introductory context.

### Interpretation

- 'view' declares the file type but does not create a stable view ID.
- 'title' is view-owned presentation content.
- 'context' is authored prose. Its multiline folding rules are the same as ordinary prose fields in Research 0005.
- 'section' titles and physical section order define the authored organization.
- each 'requirement' line is an ordered reference, not copied content or an ownership relationship.
- different views may reference the same requirement, but the experiment treats a duplicate reference within one view as an error because no current workflow requires it.

### Baseline B diff

Moving SYS-008 before SYS-006 affects only the view:

    section: Command-link monitoring
    +requirement: SYS-008
     requirement: SYS-006
     requirement: GCA-002
    -requirement: SYS-008

The loss-of-link reorganization is similarly local:

    section: Loss-of-link response
     requirement: SYS-007
     requirement: GCA-003
    +requirement: GCA-004
    +requirement: SYS-009
     requirement: MCS-001

No requirement record changes merely because its placement changes.

### Strengths

- It adds only five structural elements: view boundaries, title, context, section lines, and requirement-reference lines.
- All ordering is visible as physical source order.
- Requirement placement diffs are one-line additions, deletions, or moves.
- It does not use Markdown headings or list conventions.
- It avoids nested indentation and explicit section terminators.
- It leaves authoritative requirement content unduplicated.

### Risks

- Section termination is implicit at the next section or end of the view.
- A raw view shows IDs but not requirement titles, so readers must navigate or search.
- Flat sections cannot represent a deeper document hierarchy without extending the notation.
- Blank-line grouping is visible to humans but intentionally unavailable to semantic tools.

## Sketch 2 — explicitly terminated section blocks

### Shape

Each section has an opener, a title field, and an explicit end marker:

    view
    title: UAS Mission-Control Requirements

    section
    title: Command-link monitoring
    requirement: SYS-006
    requirement: GCA-002
    requirement: SYS-008
    end section

    section
    title: Loss-of-link response
    requirement: SYS-007
    requirement: GCA-003
    requirement: MCS-001
    end section

    end view

### Strengths

- Every section boundary is explicit.
- A future parser can report a missing 'end section' precisely.
- The structure resembles the selected requirement-record boundary style.
- A section could acquire another fixed field without changing its opener.

### Risks

- Three structural lines are needed to express each section name and boundary instead of one.
- Repeated 'end section' lines compete with a short reference list.
- The extra delimiter does not answer a workflow that Sketch 1 cannot express.
- Visual similarity between requirement records and section blocks may imply more shared semantics than actually exist.

## Sketch 3 — indentation-defined outline

### Shape

Sections open with a title and contain indented entries:

    view
    title: UAS Mission-Control Requirements

    section: Command-link monitoring
      requirement: SYS-006
      requirement: GCA-002
      requirement: SYS-008

    section: Loss-of-link response
      requirement: SYS-007
      requirement: GCA-003
      requirement: MCS-001

    end view

### Strengths

- Section membership is immediately visible as an outline.
- A section needs no end marker.
- The form could generalize naturally to nested sections if that later became necessary.

### Risks

- Indentation now means both multiline field content and structural containment.
- The apparent ease of nesting invites document-hierarchy features not exercised by the corpus.
- Moving a reference between sections often changes indentation along with position.
- A single accidental de-indent can change membership while remaining superficially plausible.

## Comparative assessment

| Criterion | Sketch 1: flat sequence | Sketch 2: explicit blocks | Sketch 3: indented outline |
| --- | --- | --- | --- |
| Source concepts | Fewest | Adds section records and terminators | Adds containment indentation |
| Section boundary | Next section or end view | Explicit 'end section' | Indentation |
| Reference diff locality | Strong | Strong | Strong, sometimes with whitespace change |
| Visual noise | Low | Moderate | Low |
| Accidental hierarchy pressure | Low | Moderate | Highest |
| Consistency with record lexical style | Strong | Strongest superficially | Partial |
| Adequate for frozen corpus | Yes | Yes | Yes |

## Authored view versus derived trace report

Research 0004 originally called the final portion of the authored view a “Trace summary.” That wording risks conflating two artifacts:

1. an authored specification view that selects and orders requirements; and
2. a derived trace report that enumerates or tabulates relationships already stored in requirement records.

Copying the 21 relationships into the view would create duplicate authoritative-looking state. Adding a 'derive:' or query directive would introduce a report-generation concept solely to satisfy a heading in the experiment corpus.

This study recommends instead:

- keep the authored view limited to title, context, sections, and requirement references;
- permit a final contextual section explaining where trace relationships are stored and how they can be inspected with ordinary search;
- evaluate manual trace following directly from requirement source;
- treat matrices, inverse-link lists, and trace summaries as disposable outputs of later tools.

This does not remove formal traceability from the model. It keeps each relationship authoritative in one place and separates authored composition from derived analysis.

## Experiment fixture

Sketch 1 is being used as the Candidate A/B fixture. Its implicit section termination is a small rule with low ambiguity because nesting is deliberately absent. Explicit 'end section' lines and structural indentation do not provide enough additional experimental value for this corpus to justify their cost.

The proposed experimental notation is therefore:

    view
    title: ...
    context:
      ...
    section: ...
    context:
      ...
    requirement: REQ-ID
    requirement: REQ-ID
    section: ...
    requirement: REQ-ID
    end view

This is deliberately not a product or language decision. Full-corpus use may reveal that ID-only references create too much navigation friction, that one flat section level is inadequate, or that a separate authored view is not valuable enough to keep.

## Decisions proposed for Candidate A/B views

1. Use Sketch 1's flat section sequence.
2. Store exactly one view per file.
3. Give the view a title but no stable semantic ID in the initial experiment.
4. Support one non-nested section level.
5. Use physical source order as authored presentation order.
6. Reference requirements by authoritative ID only.
7. Do not duplicate requirement titles or statements in the view.
8. Permit the same requirement in different views, but reject duplicate references within one view during the experiment.
9. Use the selected prose-folding rules for 'context' fields.
10. Keep trace summaries and matrices derived rather than embedding relationship copies or report queries in the view.

## Questions to observe during use

1. Is implicit section termination sufficiently obvious in the actual fixture?
2. Is one flat section level adequate for the experiment?
3. Do ID-only references impose enough navigation cost to undermine the independent-object model?
4. Does the separate view provide useful evidence beyond what repository organization and generated output could supply?
5. Is traceability-orientation prose useful, or should even that be omitted while trace summaries remain derived?

Candidate C now uses fenced purpose-built records as an experiment fixture. Annotated baseline-tag contents remain a subsequent decision.
