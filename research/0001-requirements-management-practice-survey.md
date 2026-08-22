# Research 0001: Requirements-Management Practice Survey

Status: Initial survey

## Purpose

This document surveys common requirements-management capabilities and workflows to identify engineering needs that may constrain the mundane-req conceptual model and source language.

It is not a product feature checklist. An observed capability is evidence of a need or established practice, not a commitment that mundane-req will reproduce it or represent it in source.

The initial survey emphasizes systems-engineering teams that use formal traceability. Sources include published systems-engineering guidance, established RM tool documentation, ReqIF, Git and forge documentation, and text-first requirements tools.

## Interpretive lens

Established tools often store several different kinds of information together:

- **requirement content** — what is required and information intrinsic to it;
- **relationship content** — meaningful connections between identifiable artifacts;
- **revision content** — requirement or relationship state at a point in history;
- **configuration content** — an identified set of compatible revisions;
- **workflow content** — proposals, reviews, decisions, approvals, and acknowledgements;
- **derived analysis** — coverage, suspect links, matrices, indexes, and reports.

These are provisional distinctions for analyzing practice, not yet the mundane-req conceptual model.

## Capability survey

| Observed capability | Underlying engineering need | Possible model or language pressure | Likely home in a source-first toolchain | Open question |
| --- | --- | --- | --- | --- |
| Identifiable requirement objects | Refer to a requirement despite movement or presentation changes | A stable reference target appears fundamental | Source plus validation | Can one human-usable ID serve both identity and display? |
| Typed attributes | Record durable facts used for filtering, planning, and analysis | A constrained metadata mechanism may be useful | Source for durable facts; tools for schemas and queries | Which facts are intrinsic, revision-specific, project-specific, or workflow state? |
| Modules, documents, folders, and hierarchies | Organize sets, preserve context, and define delivery scope | Collections or views may be needed without location-based identity | Source conventions plus renderers | Which hierarchy means decomposition and which is presentation? |
| Typed relationships | Explain origin, decomposition, allocation, implementation, and verification | Endpoints, meaning, and direction must be unambiguous | Source for authoritative requirement relationships; analyzers for traversal | What is the minimum vocabulary, and how are external artifacts referenced? |
| Matrices and coverage reports | Find missing, unjustified, unimplemented, or unverified requirements | Source must expose semantics from which coverage can be derived | Disposable analysis and rendering | What counts as coverage for collective or partial satisfaction? |
| Change history and object revisions | Determine what changed, when, why, and by whom | Source must diff cleanly and retain identity through edits | Git history, commits, blame, and forge discussion | Is separate requirement revision numbering useful? |
| Baselines and comparison | Identify an immutable meaningful configuration and compare milestones | Repository revisions must be complete; baseline meaning may need metadata | Git commits and tags for snapshots; convention or tooling for scope and authority | Is a baseline repository-wide, and how are external configurations included? |
| Review, approval, and signatures | Prove designated people reviewed a particular state | Approval must bind to a revision or configuration, not a mutable object | Forge reviews and protected branches initially | What assurance must survive outside forge records? |
| Change requests and workflow | Propose, assess, decide, and implement controlled changes | Change rationale may need association with the resulting revision | Issues, pull or merge requests, commits, and process | Must change-request identity appear in source? |
| Suspect links and impact flags | Ensure downstream effects are reconsidered after change | Suspect state depends on revisions and acknowledgement history | Analyzer plus disposable index; workflow records disposition | Which changes are relevant enough to trigger suspicion? |
| Queries, filters, views, dashboards | Locate subsets and assess state at scale | Durable facts need consistent representation | Independent query and indexing tools | Are saved views shared source artifacts or personal state? |
| Verification planning | Ensure every requirement is verifiable and assigned an approach | Verification intent may need a fact or relationship | Source for durable intent; tools for coverage | Is method intrinsic, relationship data, or a separate verification requirement? |
| Verification results and compliance | Show that a product configuration satisfies a requirement revision | Results bind requirements, configurations, procedures, and evidence | Usually external evidence systems; summaries are derived | How much evidence belongs in a requirements-only repository? |
| Import, export, and supplier exchange | Preserve identity and meaning across organizations and tools | Explicit types, attributes, relationships, and collections ease future mapping | Independent adapters; ReqIF is deferred | Which information must round-trip without loss? |
| Reuse, libraries, variants, and synchronization | Apply common requirements while tracking divergence | May require lineage, applicability, and configuration semantics | Deferred; first test what Git and explicit copying provide | Does reuse mean reference, copy-with-lineage, selection, or parameterization? |
| Access control | Restrict reading, proposing, approving, or modification | Usually no language construct is required | Repository, forge, and organizational controls | Are repository-level controls sufficient initially? |
| Comments, notifications, and assignments | Coordinate authoring and review | Little direct language pressure | Forge and collaboration tools | Which decisions must survive forge migration? |
| Requirement-quality checks | Detect ambiguity, inconsistency, or unverifiability | Deterministic parsing and accessible fields enable checks | Linters and validators | Which checks are objective versus organization-specific? |
| Document generation | Deliver specifications, matrices, and reports | Selection, ordering, headings, and context need a representation somewhere | Source-defined views plus renderers | Where does authored narrative live? |
| APIs and lifecycle integrations | Connect requirements to planning, design, test, and implementation | Stable identifiers and semantics matter more than embedded integrations | Independent tools and adapters | What is a durable external reference? |

## Workflow survey

### 1. Capture and refine a requirement

- **Observed practice:** Requirements are derived iteratively from stakeholder expectations, concepts of operation, constraints, interfaces, analyses, and higher-level requirements.
- **Engineering need:** Preserve a precise normative statement, its origin, necessary context, and enough reasoning to understand why it exists.
- **Possible implication:** Stable identity and a normative statement appear essential. Title, rationale, source, assumptions, and verification intent remain candidates rather than decided primitives.
- **Toolchain split:** Text editing supports authoring, linters check form, and reviews supply human validation.

### 2. Refine, decompose, or allocate a requirement

- **Observed practice:** Several lower-level requirements may collectively satisfy a parent; some requirements are derived through analysis or design.
- **Engineering need:** Explain why each child exists and whether the set fully addresses its parent.
- **Possible implication:** A generic parent-child tree may be too ambiguous. Relationship meaning matters, and self-derived requirements need an origin or rationale.
- **Toolchain split:** Source records relationships; analysis detects missing parents, orphans, and incomplete coverage.

### 3. Review and approve a proposed set

- **Observed practice:** A defined set is frozen for review, reviewers record comments and dispositions, authors revise it, and authorized participants approve a final state.
- **Engineering need:** Bind review evidence to exactly the reviewed content.
- **Possible implication:** Approval belongs to a revision or configuration, not to an indefinitely mutable requirement.
- **Toolchain split:** A branch and pull or merge request provide proposal and discussion; commit identity and controls bind the accepted state.

### 4. Change an approved requirement

- **Observed practice:** A change request records motivation; impacts are assessed; an authority accepts, rejects, or defers it; accepted changes enter configuration control.
- **Engineering need:** Preserve before and after states, decision rationale, authorization, and implementation linkage.
- **Possible implication:** Requirement rationale and change rationale are distinct. A generic approval-status field cannot say what was approved or by whom.
- **Toolchain split:** Issues, commits, and pull or merge requests represent proposal and decision; source carries the resulting state.

### 5. Assess downstream effects

- **Observed practice:** Engineers traverse links, inspect changed content, flag affected relationships, and record downstream review.
- **Engineering need:** Avoid overlooking effects on requirements, design, verification, interfaces, cost, or schedule.
- **Possible implication:** Typed directional relationships are valuable. Persistent suspect flags are probably derived state.
- **Toolchain split:** Git supplies revisions and diffs; an analyzer finds candidate impacts; workflow records disposition.

### 6. Establish a baseline

- **Observed practice:** After review or a milestone, an identified set of requirement revisions and relationships becomes immutable and controlled.
- **Engineering need:** Reconstruct the agreed configuration and understand its scope, purpose, authority, and date.
- **Possible implication:** A Git commit is a strong snapshot, but baseline semantics may include scope, approval context, and external configurations.
- **Toolchain split:** Commits and annotated or signed tags identify states; conventions or small tools may add governance metadata.

### 7. Compare baselines

- **Observed practice:** Teams identify added, removed, modified, moved, and relinked requirements between milestones.
- **Engineering need:** Understand meaningful differences without formatting or ordering noise.
- **Possible implication:** Stable identity must survive movement and relationship changes must be visible in ordinary diffs.
- **Toolchain split:** Git diff is the minimum experience; semantic comparison is additive.

### 8. Plan verification

- **Observed practice:** Teams define an approach for every normative requirement and link requirements to verification plans, procedures, cases, or analyses.
- **Engineering need:** Demonstrate that verification is possible and planned without gaps.
- **Possible implication:** Stable external references may be needed, but test cases need not belong in mundane-req.
- **Toolchain split:** Source may carry verification intent or references; tools derive coverage.

### 9. Determine verification coverage and compliance

- **Observed practice:** Teams query planned verification, evidence, results, waivers, and failures for a product configuration.
- **Engineering need:** Make defensible claims about satisfaction of a particular baseline.
- **Possible implication:** Compliance is evidence- and configuration-specific, not an eternal requirement property.
- **Toolchain split:** Verification tools own results; traceability analysis joins them to requirement revisions.

### 10. Generate a specification or delivery package

- **Observed practice:** Selected requirements are ordered with contextual prose, tables, and traceability data for review, contract delivery, or audit.
- **Engineering need:** Deliver a coherent document without making document position the identity of a requirement.
- **Possible implication:** Selection, ordering, and context remain real concerns even when documents are views.
- **Toolchain split:** Source-defined collections or views plus renderers produce disposable documents.

### 11. Exchange requirements with a customer or supplie

- **Observed practice:** Organizations exchange selected specifications, attributes, and relationships in one-way or round-trip workflows.
- **Engineering need:** Preserve meaning and identity across boundaries while controlling editable information.
- **Possible implication:** Future ReqIF mapping favors explicit identity and structure but does not require ReqIF-shaped source syntax.
- **Toolchain split:** Export and import adapters are future work; initial source remains independently readable.

### 12. Reuse requirements across products or variants

- **Observed practice:** Teams use libraries, copies with lineage, synchronization, and branched configurations.
- **Engineering need:** Avoid duplicate maintenance while preserving product-specific divergence.
- **Possible implication:** Reuse hides multiple models; inheritance or parameterization would be premature without a chosen workflow.
- **Toolchain split:** Deferred until ordinary Git mechanisms and explicit copying are tested.

### 13. Determine why, when, and by whom something changed

- **Observed practice:** Engineers inspect object history, baselines, change requests, review comments, and approvals.
- **Engineering need:** Reconstruct technical reasoning and accountability long after a change.
- **Possible implication:** Intrinsic rationale, derivation rationale, and change rationale should not collapse into one field.
- **Toolchain split:** Source records current durable reasoning, Git records history, and forge records proposal discussion and approval.

## Initial cross-cutting findings

1. **Revision-sensitive claims must identify a revision or configuration.** Approval, compliance, and review disposition cannot safely attach to a mutable requirement without a state.
2. **Hierarchy is overloaded.** Document nesting, decomposition, allocation, and grouping should not automatically share one mechanism.
3. **Relationships are engineering objects even if their first syntax is small.** They have endpoints, meaning, direction, and revision history.
4. **Baseline mechanics and governance differ.** Git offers strong snapshot mechanics; scope, authority, and cross-repository consistency remain questions.
5. **Suspect status is derived.** It emerges from a relevant upstream change plus missing downstream acknowledgement.
6. **Requirement rationale and change rationale differ.**
7. **Verification status is derived and configuration-specific.** Verification intent may still be durable source information.
8. **Specifications require deliberate composition.** Views still need selection, ordering, headings, and contextual narrative.

## Capabilities not yet justified as initial language features

- approval or workflow status fields;
- persistent suspect-link flags;
- electronic signatures;
- requirement revision numbers independent of Git;
- configurable metamodels or grammars;
- reuse inheritance, synchronization, or variant expressions;
- verification-result storage;
- ReqIF-shaped source syntax;
- access-control directives;
- comments, assignments, or notifications;
- rich-text and attachment embedding.

## Pressure on the first experiment

The agreed workflows are:

1. review a change using an ordinary Git diff;
2. follow rationale and decomposition to understand why a requirement exists;
3. compare two meaningful baselines.

A candidate representation must therefore explore:

- stable identity through edits and movement;
- a clearly distinguishable normative statement;
- enough context for reading outside a rendered document;
- durable rationale distinct from change discussion;
- explicit requirement-to-requirement relationships;
- a collection or view that defines review scope;
- repository states identifiable as experimental baselines;
- relationship additions, removals, and retargeting in ordinary diffs.

This is experiment pressure, not the final minimum language.

## Priority open questions

1. Can one stable human-usable ID serve identity and display?
2. Which relationship distinctions explain derivation and decomposition without a generalized metamodel?
3. How much local context must accompany a requirement for standalone reading?
4. Is rationale intrinsic to a requirement, attached to a relationship, or sometimes both?
5. How should a collection select and order requirements without owning their identity?
6. Is a baseline always a complete repository revision?
7. What review evidence must survive if forge data becomes unavailable?
8. How should unavailable external requirements be referenced?
9. Which verification-planning facts belong in a requirements-only language?
10. What extension mechanism permits project facts without creating a configurable metamodel?

## Sources consulted

These sources are evidence of practice, not normative dependencies:

- NASA, [Technical Requirements Definition](https://www.nasa.gov/reference/4-2-technical-requirements-definition/).
- NASA, [Requirements Management](https://www.nasa.gov/reference/6-2-requirements-management/).
- NASA, [Logical Decomposition](https://www.nasa.gov/reference/4-3-logical-decomposition/).
- NASA, [Requirements Verification Matrix](https://www.nasa.gov/reference/appendix-d-requirements-verification-matrix/).
- IBM, [Getting started with DOORS Next](https://www.ibm.com/docs/en/engineering-lifecycle-management-suite/doors-next/7.1.0?topic=getting-started).
- IBM, [Analyzing the impact of change](https://www.ibm.com/docs/en/engineering-lifecycle-management-suite/doors-next/7.1.0?topic=requirements-analyzing-impact-change).
- IBM, [DOORS change proposal system](https://www.ibm.com/docs/en/engineering-lifecycle-management-suite/doors/9.7.1?topic=requirements-change-proposal-system).
- Jama Software, [Baselines](https://help.jamasoftware.com/ah/en/manage-content/baselines.html).
- Jama Software, [Reuse and synchronization](https://help.jamasoftware.com/ah/en/manage-content/reuse-and-synchronization.html).
- PTC, [Codebeamer baselines](https://support.ptc.com/help/codebeamer/r3.1/en/codebeamer/user_guide/31705.html).
- PTC, [Reviewing suspected links](https://support.ptc.com/help/codebeamer/r3.0/en/codebeamer/user_guide/ug_review_suspected-links.html).
- OMG, [Requirements Interchange Format 1.2](https://www.omg.org/spec/ReqIF/1.2/).
- Git, [git-tag documentation](https://git-scm.com/docs/git-tag).
- GitHub, [About protected branches](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches).
- GitLab, [Merge request approval rules](https://docs.gitlab.com/user/project/merge_requests/approvals/rules/).
- StrictDoc, [Traceability guide](https://strictdoc.readthedocs.io/en/stable/stable/docs/strictdoc_01_user_guide-TRACE.html).
- Doorstop, [project overview](https://github.com/doorstop-dev/doorstop).
