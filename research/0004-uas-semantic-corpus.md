# Research 0004: UAS Semantic Experiment Corpus

Status: Draft semantic corpus

## Purpose

This document defines the engineering meaning that every source-representation candidate will encode.

It is intentionally syntax-neutral. The Markdown headings and field labels used here are editorial notation for the research record, not a candidate mundane-req language. Candidate representations must not gain or lose semantics merely because one syntax makes particular information convenient.

The corpus is original and illustrative. It is informed by realistic unmanned-aircraft traceability patterns, but it does not copy Dronology requirements or provide engineering advice for an actual aircraft.

## Experiment boundary

The corpus describes a mission-control capability for one active unmanned aircraft. It covers:

- vehicle registration and activation;
- flight-plan authorization and routing;
- communication-link monitoring;
- loss-of-link response;
- timing integrity;
- event recording.

It contains requirements only. Components named by allocations are labels, not additional model objects. External sources are references, not artifacts managed by mundane-req.

## Provisional semantic fields

These fields define corpus meaning without deciding their eventual source syntax.

| Field | Meaning in this corpus |
| --- | --- |
| ID | Stable human-facing requirement identity. It does not depend on filename, title, or view position. |
| Title | Short human-facing name that aids recognition but does not determine identity. |
| Level | Editorial classification as operational, system, or lower-level. This is not yet a required language field. |
| Allocation | Label naming the subsystem expected to implement the requirement. It is not a relationship to a modeled component. |
| Statement | Normative requirement content. |
| Rationale | Current durable reasoning for why the requirement exists or has its stated form. |
| Source | Reference to an origin outside the requirement model. It is not a requirement-to-requirement relationship. |
| Decomposes | Directed relationship from this lower-level requirement to a higher-level requirement. |
| Notes | Experiment commentary that is not part of the requirement. |

### Decomposition meaning

For this experiment:

> A requirement that decomposes a parent participates in making the parent more specific at a lower level.

A decomposition relationship does not claim that one child independently and completely satisfies its parent. Completeness is a property assessed over the relevant set of children and context.

### Self-derived requirements

A self-derived requirement has a source and rationale but no higher-level requirement in Decomposes. This allows analysis or design constraints to remain traceable without inventing a parent requirement solely to complete a tree.

## Terms

| Term | Meaning |
| --- | --- |
| Mission-control system | The complete capability in scope. |
| Registered vehicle | A vehicle whose identifier and identity evidence have been accepted into the mission repository. |
| Active vehicle | The single registered vehicle currently selected for mission control. |
| Authorized flight plan | A specific plan revision approved by an authorized operator for the active vehicle and mission. |
| Valid vehicle message | A received message whose integrity and vehicle identity checks have succeeded. |
| Command link unavailable | The state determined by SYS-006. |
| Safe-recovery command | A command requesting the vehicle's externally defined safe-recovery behavior. |
| First transmission attempt | The ground-control adapter's first handoff of the encoded command to its transport. |

## Assumptions

1. The vehicle implements a safe-recovery behavior, but that behavior is outside this requirements corpus.
2. Operator authentication and authorization are supplied by an external platform.
3. Message integrity checking exists before a message is considered valid.
4. Only one vehicle may be active for a mission in this experiment.
5. All identifiers, thresholds, and domain facts are illustrative.
6. The transport may fail after a first transmission attempt; end-to-end vehicle receipt is outside the initial response-time boundary.
7. Baseline labels demonstrate configuration mechanics, not regulatory approval or certification.

## Synthetic external sources

These references represent information supplied to the requirements effort but not managed as mundane-req requirements. They are synthetic and have no external authoritative document in this repository.

### SRC-CONOPS-001 — Illustrative UAS mission concept

- **Locator:** UAS-CONOPS-EXAMPLE, revision A.
- **Relevant content:** An authorized operator selects one registered vehicle, authorizes one flight plan, conducts the mission, and retains control responsibility until completion or safe recovery.
- **Purpose:** Origin for operational mission, identity, and flight-plan requirements.

### SRC-SAFETY-001 — Illustrative loss-of-link analysis

- **Locator:** UAS-SAFETY-EXAMPLE/LOSS-OF-LINK, revision A.
- **Relevant content:** Prolonged loss of the command link requires prompt initiation of safe recovery and preservation of enough event information for later reconstruction.
- **Purpose:** Origin for loss-of-link response requirements and the Baseline B change.

### SRC-DESIGN-001 — Illustrative timing analysis

- **Locator:** UAS-TIMING-EXAMPLE/MONOTONIC-CLOCK, revision A.
- **Relevant content:** Wall-clock corrections must not change elapsed-time decisions used for communication loss or safety-response deadlines.
- **Purpose:** Origin for the self-derived monotonic-time requirement.

## Baseline A requirements

Baseline A contains 18 requirements and 21 requirement-to-requirement decomposition relationships.

### OPS-001 — Controlled mission lifecycle

- **Level:** Operational.
- **Allocation:** Mission-control system.
- **Statement:** The mission-control system shall enable an authorized operator to control one active vehicle from vehicle activation until mission completion or initiation of safe recovery.
- **Rationale:** The operator needs an unambiguous interval of control responsibility.
- **Source:** SRC-CONOPS-001.
- **Decomposes:** None.

### OPS-002 — Vehicle identity integrity

- **Level:** Operational.
- **Allocation:** Mission-control system.
- **Statement:** The mission-control system shall prevent a flight plan or control command intended for one registered vehicle from being applied to another vehicle.
- **Rationale:** Misrouting a plan or command can create unsafe vehicle behavior.
- **Source:** SRC-CONOPS-001.
- **Decomposes:** None.

### OPS-003 — Authorized flight-plan execution

- **Level:** Operational.
- **Allocation:** Mission-control system.
- **Statement:** The mission-control system shall cause the active vehicle to execute only the flight-plan revision authorized for that vehicle and mission.
- **Rationale:** Vehicle identity, mission identity, and plan revision must remain bound throughout dispatch.
- **Source:** SRC-CONOPS-001.
- **Decomposes:** None.

### OPS-004 — Loss-of-link safety response

- **Level:** Operational.
- **Allocation:** Mission-control system.
- **Statement:** When the command link to the active vehicle remains unavailable beyond the configured loss tolerance, the mission-control system shall initiate the configured safe-recovery response.
- **Rationale:** Continuing a mission without a usable command link can exceed the assumed operational risk.
- **Source:** SRC-SAFETY-001.
- **Decomposes:** None.

### SYS-001 — Unique registered vehicle identifier

- **Level:** System.
- **Allocation:** Vehicle manager.
- **Statement:** The mission-control system shall associate each registered vehicle with exactly one vehicle identifier that is unique within the mission repository.
- **Rationale:** Stable unique identity is necessary for activation, routing, and audit records.
- **Source:** SRC-CONOPS-001.
- **Decomposes:** OPS-002.

### SYS-002 — Activation eligibility

- **Level:** System.
- **Allocation:** Vehicle manager.
- **Statement:** The mission-control system shall permit a vehicle to become active only when that vehicle is registered and its presented identity matches its registered vehicle identifier.
- **Rationale:** Activation is the boundary at which a registered identity becomes the target of mission operations.
- **Source:** SRC-CONOPS-001.
- **Decomposes:** OPS-001, OPS-002.

### SYS-003 — Single active vehicle

- **Level:** System.
- **Allocation:** Vehicle manager.
- **Statement:** The mission-control system shall maintain no more than one active vehicle for a mission.
- **Rationale:** The experiment assumes a single unambiguous command target.
- **Source:** SRC-CONOPS-001.
- **Decomposes:** OPS-001, OPS-002.

### SYS-004 — Flight-plan authorization binding

- **Level:** System.
- **Allocation:** Flight-plan manager.
- **Statement:** The mission-control system shall mark a flight-plan revision as authorized only when an authorized operator approves that revision for the active vehicle and current mission.
- **Rationale:** Authorization must bind the plan revision, vehicle, mission, and approving authority.
- **Source:** SRC-CONOPS-001.
- **Decomposes:** OPS-001, OPS-003.

### SYS-005 — Vehicle-bound dispatch

- **Level:** System.
- **Allocation:** Ground-control adapter.
- **Statement:** The mission-control system shall include the active vehicle identifier in every dispatched flight plan and control command and shall reject dispatch when the destination identifier differs from the active vehicle identifier.
- **Rationale:** Checking identity at dispatch protects the final system boundary before transport.
- **Source:** SRC-CONOPS-001.
- **Decomposes:** OPS-001, OPS-002, OPS-003.

### SYS-006 — Command-link loss determination

- **Level:** System.
- **Allocation:** Link monitor.
- **Statement:** While a vehicle is active, the mission-control system shall declare its command link unavailable at the first link-evaluation instant defined by the following mathematical expression.
- **Mathematical source:**

        k^\ast = \min\left\{k \in \{0,1,2,\ldots\} \mid
        t_0 + k T_{\mathrm{eval}} - t_{\mathrm{last}} \ge T_{\mathrm{loss}}\right\},
        \qquad
        t_{\mathrm{detect}} = t_0 + k^\ast T_{\mathrm{eval}}

        T_{\mathrm{loss}} = 2.0\,\mathrm{s},
        \qquad
        0 < T_{\mathrm{eval}} \le 0.10\,\mathrm{s}

- **Variables and units:**
  - t_0 is the first scheduled link-evaluation time after vehicle activation, in seconds.
  - k is a nonnegative integer evaluation index.
  - T_eval is the fixed interval between evaluations, in seconds.
  - t_last is the monotonic receipt time of the most recent valid vehicle message, or the vehicle activation time if no valid vehicle message has yet been received, in seconds.
  - T_loss is the configured link-loss tolerance, in seconds.
  - t_detect is the time at which the link is declared unavailable, in seconds.
- **Rationale:** The expression makes the threshold boundary and evaluation quantization explicit rather than hiding them in prose.
- **Source:** SRC-SAFETY-001.
- **Decomposes:** OPS-004.
- **Notes:** The fragment intentionally excludes container delimiters. Every candidate must preserve these exact LaTeX-style characters, variable definitions, units, and assumptions.

### SYS-007 — Loss-of-link response and record

- **Level:** System.
- **Allocation:** Mission-control coordinator.
- **Statement:** Within 250 ms after the command link is declared unavailable, the mission-control system shall cause the ground-control adapter to begin the first transmission attempt of a safe-recovery command for the active vehicle and shall record the link-loss declaration time, active vehicle identifier, and command identifier.
- **Rationale:** Safe recovery must begin promptly, and the response must be reconstructable after the event.
- **Source:** SRC-SAFETY-001.
- **Decomposes:** OPS-001, OPS-004.
- **Notes:** This requirement is intentionally compound so the synthetic change can test identity-preserving splitting.

### SYS-008 — Monotonic safety timing

- **Level:** System.
- **Allocation:** Timing service.
- **Statement:** The mission-control system shall use a monotonic time source with resolution no greater than 10 ms for every elapsed-time comparison that determines command-link availability or a safe-recovery response deadline.
- **Rationale:** Wall-clock correction must not shorten or extend safety-related elapsed-time decisions.
- **Source:** SRC-DESIGN-001.
- **Decomposes:** None.
- **Notes:** This is the explicitly self-derived requirement.

### VM-001 — Duplicate registration rejection

- **Level:** Lower-level.
- **Allocation:** Vehicle manager.
- **Statement:** The vehicle manager shall reject a registration request when its vehicle identifier is already associated with a different registered vehicle.
- **Rationale:** The registration boundary must enforce the uniqueness promised by SYS-001.
- **Source:** None; origin is expressed by the decomposition relationship.
- **Decomposes:** SYS-001.

### VM-002 — Atomic activation guard

- **Level:** Lower-level.
- **Allocation:** Vehicle manager.
- **Statement:** The vehicle manager shall reject an activation transaction unless the requested vehicle identifier matches a registered vehicle and no different vehicle is active for the mission.
- **Rationale:** One atomic decision prevents registration state and active-vehicle state from being checked inconsistently.
- **Source:** None; origin is expressed by the decomposition relationships.
- **Decomposes:** SYS-002, SYS-003.

### GCA-001 — Dispatch destination guard

- **Level:** Lower-level.
- **Allocation:** Ground-control adapter.
- **Statement:** The ground-control adapter shall reject a flight plan or control command whose destination vehicle identifier does not equal the active vehicle identifier supplied by the vehicle manager.
- **Rationale:** The adapter is the last controlled boundary at which destination mismatch can be rejected.
- **Source:** None; origin is expressed by the decomposition relationship.
- **Decomposes:** SYS-005.

### GCA-002 — Valid-message timestamp

- **Level:** Lower-level.
- **Allocation:** Ground-control adapter.
- **Statement:** The ground-control adapter shall publish the monotonic receipt time of each valid message from the active vehicle with resolution no greater than 10 ms.
- **Rationale:** SYS-006 requires an identity-checked last-message time on the same monotonic basis used for evaluation.
- **Source:** None; origin is expressed by the decomposition relationships.
- **Decomposes:** SYS-006, SYS-008.

### GCA-003 — Priority safe-recovery transmission

- **Level:** Lower-level.
- **Allocation:** Ground-control adapter.
- **Statement:** When the ground-control adapter receives a safe-recovery request, it shall place the resulting command ahead of queued non-safety commands and use the active vehicle identifier as the destination.
- **Rationale:** Queued routine traffic must not delay the safety response or change its destination.
- **Source:** None; origin is expressed by the decomposition relationship.
- **Decomposes:** SYS-007.

### MCS-001 — Loss-of-link event persistence

- **Level:** Lower-level.
- **Allocation:** Event recorder.
- **Statement:** The event recorder shall persist the link-loss declaration time, active vehicle identifier, and safe-recovery command identifier within 100 ms after receiving the event-record request.
- **Rationale:** Prompt persistence supports reconstruction even if later mission-control processing fails.
- **Source:** None; origin is expressed by the decomposition relationship.
- **Decomposes:** SYS-007.

## Baseline A trace inventory

The 21 decomposition relationships are:

1. SYS-001 → OPS-002
2. SYS-002 → OPS-001
3. SYS-002 → OPS-002
4. SYS-003 → OPS-001
5. SYS-003 → OPS-002
6. SYS-004 → OPS-001
7. SYS-004 → OPS-003
8. SYS-005 → OPS-001
9. SYS-005 → OPS-002
10. SYS-005 → OPS-003
11. SYS-006 → OPS-004
12. SYS-007 → OPS-001
13. SYS-007 → OPS-004
14. VM-001 → SYS-001
15. VM-002 → SYS-002
16. VM-002 → SYS-003
17. GCA-001 → SYS-005
18. GCA-002 → SYS-006
19. GCA-002 → SYS-008
20. GCA-003 → SYS-007
21. MCS-001 → SYS-007

OPS-002 is intentionally decomposed by several system requirements. No single child is asserted to satisfy it independently.

SYS-008 has no higher-level parent because it is self-derived from timing analysis. Its absence from the operational decomposition tree is intentional, not a missing link.

## Baseline A authored specification view

The candidates must present the following content and order without duplicating authoritative requirement statements.

1. **Purpose and operational context**
   - contextual prose summarizing the experiment boundary and assumptions;
   - OPS-001, OPS-002, OPS-003, OPS-004.
2. **Vehicle identity and activation**
   - SYS-001, VM-001;
   - SYS-002, SYS-003, VM-002.
3. **Flight-plan authorization and dispatch**
   - SYS-004;
   - SYS-005, GCA-001.
4. **Command-link monitoring**
   - SYS-006;
   - GCA-002;
   - SYS-008.
5. **Loss-of-link response**
   - SYS-007;
   - GCA-003;
   - MCS-001.
6. **Trace summary**
   - a derived or manually inspectable presentation of the decomposition inventory.

Candidates A and B will represent this as a separate plain-text view. Candidate C will represent it through the authored Markdown document structure.

## Proposed safety-driven change

The synthetic change is motivated by a revision to SRC-SAFETY-001. It concludes that the first transmission attempt must begin within 100 ms rather than 250 ms and that safety action should not share one normative statement with audit recording.

The proposed branch applies these exact semantic changes.

### 1. Tighten and split SYS-007

Preserve SYS-007 identity for the primary safety behavior.

Replace its statement with:

> Within 100 ms after the command link is declared unavailable, the mission-control system shall cause the ground-control adapter to begin the first transmission attempt of a safe-recovery command for the active vehicle.

Update its rationale to address only prompt initiation of safe recovery. Retain SYS-007 → OPS-001 and SYS-007 → OPS-004.

### 2. Add SYS-009 — Loss-of-link audit record

- **Level:** System.
- **Allocation:** Event recorder.
- **Statement:** Within 1 s after the command link is declared unavailable, the mission-control system shall record the link-loss declaration time, active vehicle identifier, and safe-recovery command identifier.
- **Rationale:** Audit recording supports event reconstruction but must not obscure the timing or identity of the primary safety action.
- **Source:** SRC-SAFETY-001, revision B.
- **Decomposes:** None.
- **Notes:** This is the requirement created by splitting the recording clause from SYS-007. It is self-derived from the revised safety analysis.

### 3. Retarget MCS-001

Retain the MCS-001 statement and identity.

Change its decomposition relationship:

- remove MCS-001 → SYS-007;
- add MCS-001 → SYS-009.

This isolates a relationship-only semantic edit for ordinary Git review.

### 4. Add GCA-004 — Recovery transmission deadline allocation

- **Level:** Lower-level.
- **Allocation:** Ground-control adapter.
- **Statement:** The ground-control adapter shall begin the first transmission attempt of a safe-recovery command within 50 ms after receiving the safe-recovery request.
- **Rationale:** Allocating half of the system response budget to the adapter leaves time for detection notification, request construction, and dispatch.
- **Source:** None; origin is expressed by the decomposition relationship.
- **Decomposes:** SYS-007.

### 5. Reorder SYS-008 in the authored view

Move SYS-008 before SYS-006 in the command-link monitoring section so the timing basis is introduced before the formula that uses it.

Do not change SYS-008 identity, statement, rationale, source, or relationships.

### 6. Reorganize the loss-of-link view

The Baseline B loss-of-link portion becomes:

1. SYS-007;
2. GCA-003;
3. GCA-004;
4. SYS-009;
5. MCS-001.

The view must make the safety action and audit record visibly distinct.

### 7. Record an impact-review observation

The unchanged GCA-003 → SYS-007 relationship requires human impact review because SYS-007's response threshold changed. The proposed source does not store a persistent suspect flag.

The expected review disposition is:

- GCA-003 remains relevant and its relationship is retained;
- GCA-004 supplies the newly explicit lower-level timing allocation;
- the disposition belongs to the review record, not the authoritative requirement fields.

## Baseline B expected inventory

After accepted review corrections, Baseline B contains:

- 20 requirements;
- 22 decomposition relationships;
- two self-derived system requirements, SYS-008 and SYS-009;
- the unchanged mathematical source fragment in SYS-006;
- one tightened response threshold;
- one identity-preserving split;
- one relationship retarget;
- one added lower-level timing requirement;
- one view-only move.

Baseline B will be identified by a Git commit and annotated experimental tag. The exact tag metadata remains to be decided in the source experiment.

## Cross-candidate invariants

Every representation candidate must preserve:

- all Baseline A and Baseline B requirement IDs;
- exact normative words and numerical values;
- the mathematical fragment character-for-character;
- variable definitions, units, and assumptions;
- rationales and source references;
- allocations as labels rather than modeled components;
- all decomposition endpoints and direction;
- the authored view order in each baseline;
- the distinction between requirement rationale and change rationale;
- the absence of approval status, suspect flags, item revisions, and generated timestamps from requirement source.

A candidate may add only syntax required to express these semantics. It may not add fields or model objects merely because its carrier makes them convenient.

## Review questions enabled by the corpus

A reviewer should be able to answer from ordinary source and Git history:

1. Which requirement retained identity after the split, and why?
2. Which new requirement received the separated audit behavior?
3. Which numerical deadline changed?
4. Which lower-level requirement was added to address the new deadline?
5. Which relationship was retargeted without changing its source requirement?
6. Which unchanged relationship required impact review?
7. Why do SYS-008 and SYS-009 have no parent requirement?
8. Can the SYS-006 equation, variables, and units be understood and compared without rendering?
9. Does optional rendering reproduce the canonical mathematical source faithfully?
10. Did the view-only movement of SYS-008 avoid changing requirement identity?
