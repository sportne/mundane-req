# Research 0037: Requirement and assertion ownership

Date: 2026-09-05

Decision: retain the existing requirement model and separate contextual assertions.
An obligation states required behavior. An assertion says something about an
identified obligation in a context, under an identified basis and authority.
Neither compiling an assertion nor resolving its target establishes its truth.

## Ownership matrix

“Requirement revision” below means the selected source/compiled values of the
human-authored ID, not a second identity. Source locations are provenance retained
by compilers, while the optional authored `source` field is the author's provenance
statement; they are different facts.

| Fact | Owner and authority | Context and revision binding | Independent change rule |
| --- | --- | --- | --- |
| ID, title, obligation/statement | Requirement author, requirement source | Selected source set and requirement revision | Content edit yields revised values; ID correction removes/adds an ID |
| Rationale | Requirement author, requirement source | Same requirement revision | Rationale edit changes source revision, may trigger scoped review |
| Authored source citation | Requirement author, source field | Citation as written at selected revision | Does not authenticate referenced evidence |
| Physical path/ranges and compiler provenance | Compiler, derived artifact | Exact input snapshot and tool version | File move changes provenance, not obligation |
| Decomposes | Child requirement author | Child points to parent ID in selected set | Inverse children are derived; changes revise child source |
| Descriptive tags / intrinsic classification | Requirement author under future project schema | Requirement revision, explicit project vocabulary | TC-1301/1302 must justify types before adding syntax |
| Assessed safety criticality | Assessment author/assessor | Requirement revision, scheme, deployment context, evidence/basis | Reassess unchanged requirement without editing requirement source |
| Current allocation label | Requirement author | Opaque label at requirement revision | Existing allocation field stays valid |
| Allocation by product version/variant | Allocation assertion author | Requirement revision and explicitly identified product context | Variant A and B can differ simultaneously |
| Verification activity definition | Activity author | Its own revision and applicability | Procedure can evolve independently of obligation |
| Planned coverage | Verification plan author | Activity revision, requirement revision and plan context | Revised plan changes intended coverage only |
| Execution/evidence/result | Executor, evidence custodian, result authority respectively | Activity/configuration/baseline and evidence revision | A passing run does not rewrite plan or obligation |

Every authored relationship retains its own source location. Coverage points from
plan row to requirement; inverse “covered by” is derived. An assessment points from
assessment to requirement; requirement source need not enumerate assessments.
Resolver output may carry both directions for navigation, with the original
assertion's provenance. No independently editable inverse list is introduced.

## Worked cases (conceptual records, not selected source syntax)

For requirement SYS-004 at revision R1, two simultaneous assessments are:

| Assessment | Target | Context | Scheme / level | Rationale | Evidence | Authority |
| --- | --- | --- | --- | --- | --- | --- |
| AS-01 rev A | product:SYS-004 @ R1 | vaccine-store / mains | local-H1 / high | Alarm delay can conceal excursion | hazard-17 @ rev 3 | safety-review-role |
| AS-02 rev A | product:SYS-004 @ R1 | training-simulator | local-H1 / low | Simulated readings cannot damage stock | scope-4 @ rev 1 | training-review-role |
| AS-01 rev B | product:SYS-004 @ R1 | vaccine-store / mains | local-H1 / medium | Independent monitoring changes consequence | hazard-17 @ rev 4 | safety-review-role |

These are valid ownership examples: AS-01 changes with hazard analysis while SYS-004
stays R1, and AS-02 does not conflict because its context differs. The scheme and
levels are illustrative project policy, not a safety standard or certification.

Invalid examples: `SYS-004: high` as an assessed safety claim lacks scheme, context,
revision, basis and authority; AS-01 rev B with no evidence revision cannot explain
the changed assessment; two same-scope same-revision AS-01 definitions are ambiguous.
A resolver can find a target but cannot repair missing assessment arguments or
approve the rationale. Those checks belong to a future assessment tool.

Allocation examples: AL-01 assigns product:SYS-004 @ R1 to gateway-A in variant
“portable”; AL-02 assigns it to controller-B in variant “fixed”. Their owners revise
allocations when product structure changes. The requirement's existing opaque
allocation label remains available and acquires no implicit variant semantics.

Verification example: PLAN-A covers SYS-004 @ R1 using ACT-ALARM @ A. PLAN-B changes
that procedure to ACT-ALARM @ B while SYS-004 stays R1. The plan source owns the
new assertion and its location. An execution result references ACT-ALARM @ B,
actual configuration and evidence; the coverage assertion alone means neither
execution nor success. Unqualified SYS-004 with two imported scopes is ambiguous;
explicit `product:SYS-004` selects a scope without adding a machine identity.

## Analysis distinctions and decisions enabled

- Planned coverage: a valid plan claims a selected activity addresses a requirement.
- Possible impact: a changed target is reachable through a specific authored link.
- Review staleness: the plan's recorded basis differs under its declared comparison
  policy; review is needed, not automatic invalidity or test failure.
- Satisfaction: a domain judgment supported by applicable evidence and authority;
  neither generic linking nor the first planned-coverage experiment establishes it.

TC-1103 can now exercise requirements plus independent plans, with explicit scope,
revision basis, context, provenance and completeness. Compare source/Git revision
binding against requirement-content comparison, without selecting a universal
hash identity. TC-1301 can classify genuinely descriptive attribute examples;
assessed levels and variant allocations retain independent owners. No new fields
or schema syntax are selected here.

This reconciles the prior [verification](0024-verification-companion-decision.md),
[safety](0025-safety-classification-ownership-decision.md),
[allocation](0027-allocation-model-decision.md) and
[identity](0023-identity-continuity-decision.md) decisions. Both maintained custom
0.2 and YAML 0.3 requirements remain valid. The old card's 0.2 baseline wording is
extended to acknowledge the completed YAML implementation, without changing either
model. Other artifact authoring formats remain independent decisions.

Rejected: moving all metadata onto requirements (cannot represent independent
contexts/revisions); making every value an entity (adds identity without a consumer);
universal inheritance or implicit approval rules (exceeds the verification case).
Stop any generalization that loses independently reviewable authority or requires
requirements to understand hazard analysis. This evidence permits the bounded
verification experiment, not a general engineering metamodel.
