# Experiment 0004: Transferability Review

Status: Completed assessment

Review date: 2026-08-22

## Result in brief

The minimum requirement language transfers to the 19-record FRET Lift-Plus-Cruise mini corpus without a grammar change. It preserves human-inspectable requirement identity, normative text, allocation, and precise provenance in ordinary source files. The GraalVM native validator parses both experiment baselines as 19 requirements and zero decomposition relationships from two files.

This is not a lossless FRET conversion. FRET's variable declarations, generated temporal semantics, and analysis configuration do not become mundane-req requirement fields. The result supports mundane-req as durable requirements source, not as an interchange representation for every FRET project artifact.

Disposition: **the minimum requirement model transfers for its intended role and is not overfitted to the original UAS corpus. Keep the grammar unchanged. Refine the conceptual treatment of verification planning as a separate, revision-scoped relationship model.**

## Baselines and history

| Point | Git name | Content |
| --- | --- | --- |
| Initial transfer | `experiment-0004-baseline-a` | All 19 upstream IDs and `fulltext` values preserved in the initial encoding |
| Verification-plan commit | `3474d22` | Five planned activities and explicit 19-of-19 planning coverage for Baseline A |
| Corrected-ID transfer | `experiment-0004-baseline-b` | `LPC_KIAS_0` corrected locally to `LPC_KIAS_NONNEGATIVE`; coverage consumer and baseline binding updated |

Baseline B still contains 19 requirements and zero decomposition relationships. No source-language feature was added during the pressure cases.

## Representation mapping and losses

| Upstream FRET information | Treatment | Transfer assessment |
| --- | --- | --- |
| `reqid` | Requirement ID at Baseline A | Preserved exactly; adequate for human and tool references |
| `fulltext` | `statement` with non-semantic line wrapping | Preserved exactly after mundane-req prose folding |
| component `vehicle` | Plain `allocation` label | Readable and sufficient for this corpus; vocabulary identity remains untested |
| empty rationale, comments, parent ID, and status | Omitted | No information loss from the selected records |
| project and upstream record location | Pinned opaque `source` value | Commit, path, and record are recoverable without structuring the scalar |
| record order and JSON storage layout | Not modeled | Correctly discarded because no requirement semantics were demonstrated |
| four variable declarations | Not copied into requirement records | Context loss; variable types and vocabulary may belong in a separate formal-language or glossary artifact |
| generated scope, timing, formula, and diagram fields | Not copied | Derived/tool-specific interpretation loss; would matter for FRET interchange or independent formal analysis |
| realizability selections, engine settings, and traces | Not copied | Analysis configuration/evidence loss; belongs to a revision-bound analysis record if retained |

The source remains textually inspectable, but expressions such as `FTP`, `preReal`, `kias`, and `kgs` depend on a vocabulary not defined by the requirement records. This exposes a human-readability issue beyond file syntax. A project glossary or formal symbol table may eventually be warranted, but this one corpus does not justify adding definitions, imports, or executable expression semantics to the minimum requirement language.

## Pressure-case findings

### Verification planning and coverage

The adjacent [verification plan](verification-plan.md) gives planned activities stable IDs, methods, objectives, evidence expectations, and many-to-many requirement coverage. It is explicitly bound to an annotated requirements baseline.

This structure expresses three facts the requirement record should not conflate:

1. a requirement revision is assigned to a planned verification activity;
2. the activity may later execute and produce evidence;
3. coverage, execution, and pass/fail are different states.

The plan reaches 19-of-19 planning coverage and zero executions. Repeating activity data as fields on each requirement would create duplication and would make results appear intrinsic to the requirement rather than to an activity, execution, and baseline.

Model implication: verification planning deserves a separate source model if mundane-req takes it on. Its links must identify requirement revisions or a declared baseline. This experiment does not select that model's file syntax and does not add it to `.mreq`.

### Identifier correction or replacement

Baseline B changes `LPC_KIAS_0` to the clearer local ID `LPC_KIAS_NONNEGATIVE`. The statement, title, allocation, file, and pinned upstream locator remain unchanged. The verification-plan consumer changes in the same commit.

The ordinary diff makes the editorial intent clear, and Git preserves the atomic change. The requirement model nevertheless interprets the result as removal plus addition, exactly as Specification 0002 says. The unchanged source locator is useful provenance evidence but is not an identity-continuity assertion.

This is meaningful cross-artifact pressure, but it is still one deliberately staged correction. Do not add hidden IDs or aliases yet. Before ReqIF round-trip work, compare at least these alternatives with independently versioned consumers:

- reviewed replacement relying on Git history;
- an explicit rename/continuity record outside the requirement object;
- a durable machine identity distinct from the human-facing ID.

### External-source locator and revision fidelity

The opaque `source` scalar carries a repository owner/name, immutable commit, path, and upstream record ID in one readable line. That is enough to recover the exact origin and to show after the local ID correction that the upstream ID remains `LPC_KIAS_0`.

No grammar is needed to parse the locator for basic durability. Resolution, reachability checks, license inventory, and provenance queries would benefit from conventions or tooling, but should not be inferred as universal syntax from this one Git-hosted source.

### Allocation vocabulary

All 19 FRET records name one component, `vehicle`. Representing that value as `allocation: vehicle` is straightforward, local, and readable. It does not reveal whether `vehicle` denotes a system component, responsibility, verification target, or deployment unit without upstream context.

Because the corpus has only one allocation value and no rename or reallocation, it provides weak evidence about controlled vocabularies. Keep allocation as a plain label. A repository profile can declare allowed labels before the core language models allocation targets as objects.

### Relationship and coverage policy

The selected FRET records have empty parent IDs and no demonstrated decomposition hierarchy. Shared variables, transition adjacency, and realizability conflicts are not decomposition, so the transfer correctly contains zero `decomposes` lines.

The validator accepts the set because absence of decomposition is not a syntax error. A project policy requiring upward trace coverage would report all 19 records uncovered unless the higher-level source were added or waived. Verification coverage is separately stated in the plan. This confirms that relationship validity, decomposition completeness, and verification coverage are different checks.

## Git and file-granularity observations

- Two subject files provide useful reading locality without introducing modules or namespaces.
- The source fields make each record's external origin visible even after regrouping.
- The ID correction is a one-token source change plus a one-token consumer update; no path change or whole-file churn occurs.
- Long pinned locators are repetitive but stable. A shorthand would reduce characters while adding indirection and configuration; this experiment does not justify that trade.
- The algebraic FRET statements are less approachable than the original UAS prose, but the record syntax adds little noise around them.

## Decisions

1. Keep the Specification 0002 `.mreq` grammar unchanged.
2. Treat the transfer as requirements-source preservation, not FRET round-tripping.
3. Preserve exact third-party provenance and applicable licensing beside redistributed adaptations.
4. Confirm that a precise commit/path/record locator can remain an opaque `source` value initially.
5. Keep allocation as a plain label; this corpus is insufficient to justify modeled allocation objects.
6. Keep decomposition completeness and verification coverage as separately declared policies.
7. Refine the conceptual model: planned verification is a relationship between a requirement revision and a separately identified activity, while execution/result evidence belongs to an activity execution.
8. Keep verification-plan syntax deferred until a small implementation or second plan tests actual editing and analysis needs.
9. Keep ID continuity unresolved; stage a focused cross-baseline/ReqIF experiment before adding machine identity, aliases, or rename records.

## Limitations

- The corpus was authored for FRET rather than for mundane-req, but it is still a small research case study rather than an operational supplier specification.
- All requirements share one component and have no rationale, hierarchy, status, or comments.
- The verification plan was designed but not executed against a vehicle design or implementation.
- The ID correction was deliberately introduced by this experiment rather than observed upstream.
- Exact semantic equivalence with FRET's generated temporal formulas was not tested.
- License selection was based on the upstream repository license and absence of a more specific case-study notice; this is engineering provenance documentation, not legal advice.

## Next evidence

Stage 6 should implement one focused query over the existing semantic model. Incoming and transitive requirement trace remain useful, but this experiment also exposes a small verification-plan coverage query. The tool experiment should choose one narrowly and must not turn the parser into a platform.
