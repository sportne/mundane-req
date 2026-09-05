# Research 0038: Compilation and linking experiment decision

Date: 2026-09-05

[Experiment 0027](../experiments/0027-compilation-linking/README.md) establishes a
small serialized requirement/plan boundary. Select that direction for TC-1201:
requirements publish semantic values, source provenance, diagnostics, an explicit
format and completeness. Plans independently own references, review basis and
context. A Python consumer resolves qualified human IDs without parser internals.

## Observed results

The reproducible runner passes 57 requirements, 13 golden report cases and two
clean rebuilds with identical artifacts/reports. Separate local Git checkouts
produce identical normalized artifacts. The [golden report](../experiments/0027-compilation-linking/expected/results.txt)
shows two stale planned-coverage rows after CR-001's three-to-five-year obligation
edit. Unrelated requirement changes, comments and moves keep both rows current.
An ID correction causes a missing external target and also revises the dependent
RDS-002 relationship content. Qualification resolves duplicate IDs across scopes;
unqualified selection is ambiguous. A legal relationship cycle is not a build
cycle. Incomplete or unknown inputs block analysis; reports distinguish these from
missing/ambiguous links. Every coverage finding includes its authored TSV row.

## Revision comparison

Five source variants produce five different Git tree IDs: baseline A, B, unrelated
edit, comment edit and file move. Exact Git/source-byte binding therefore notices
all five. Full per-requirement semantic-value comparison flags only the changed
covered requirements; paths and comments remain in separate provenance. These
are complementary questions, not competing identities. A Git commit can additionally
record repository history/authority but does not certify technical validity.

Select exact input SHA-256 provenance for compiled source snapshots, plus directly
comparable semantic values. Do not add a public per-requirement content digest yet:
the demonstrated consumer can compare values, and TC-0905 still needs to choose
which fields matter for review. Canonicalized digests may later reduce storage;
that needs a documented projection and separate justification. The source digest
changes with byte edits and identifies only an exact input revision, never a
stable requirement identity.

## Minimum contract and alternatives

The maintained output needs: kind and exact format identifier; independently
versioned compiler/source identifiers; complete/invalid state; human ID and every
current model field; deterministic set ordering; source snapshots and requirement,
field and relationship locations; stable diagnostic codes and explicit coordinate
units. Paths and trivia must remain outside normative semantic values.

Reject a direct Java object API for cross-component integration, a universal graph
schema, automatic directory imports and an untyped future attribute bag. The
experiment's plan basis embeds full values for inspectability; it is not a final
plan storage contract. Discard its private envelope, hard-coded scopes and broad
full-record staleness policy when maintained contracts select narrower meanings.

TC-1201 can proceed after TC-1502. TC-1203 will choose maintained import semantics;
TC-0905 will choose plan notation, context and review-basis policy. No current
syntax/model change or maintained analyzer is selected here. Stop widening the
integration layer if it requires another domain parser inside requirements or
makes successful linking equivalent to approval, execution or satisfaction.
