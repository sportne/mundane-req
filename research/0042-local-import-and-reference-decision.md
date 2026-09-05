# Research 0042: Local imports and references

Date: 2026-09-05

Select the [local import contract](../specification/0014-local-artifact-imports-0.1.md)
for the first verification consumer. TC-1204 should implement a small Java resolver
with an independent serialized CLI boundary, not a platform service. It needs only
requirement imports and the plan contract supplied by TC-0905. This satisfies the
conditional card's consumer criterion from Experiment 0027.

## Worked resolution cases

With `baseline:A` and `current:A`, a plan explicitly selects those scopes and owns
a coverage reference to A. A retains its authored ID in both compiled artifacts.
The resulting edge records the plan/context/location and both selected scopes;
an inverse current:A entry points back to that authored row. Two imported A IDs
with an unqualified scope fail ambiguous-scope; an explicit missing scope fails
missing-scope. A missing requirement fails missing-target. Passing a plan artifact
as a requirement import fails wrong-kind. An unknown format fails unsupported-format;
an incomplete artifact fails incomplete-import. None produces a fully linked edge list.

A requirement A decomposing itself is a supported source relationship, not a build
cycle. Imports baseline -> current -> baseline fail build-cycle. Filtering plans
to context portable produces results only for that named context; unknown contexts
fail. Invalid imported snapshots still fail even when a context would not use them.
These examples and the normative change matrix supply TC-0905's fixture expectations.

## Revision selection and alternatives

Current-checkout imports read an explicitly named compiled file and record its actual
SHA-256. Pinned selection checks a declared SHA-256. A recorded detached Git checkout
can supply that file, but Git resolution is a caller operation. Compare two local
repositories by preparing an independent requirements checkout and plan checkout,
compiling into a workspace with root-relative paths, then linking without network.
TC-1204 must execute this fixture with actual Git commits and a changed current
checkout, and show that a previously pinned artifact remains selectable.

Experiment 0027 already compared Git tree IDs with semantic values across comments,
moves, unrelated and normative edits. Exact artifact pins enforce reproducible inputs;
full requirement values provide an inspectable basis for scoped review. TC-0905 selects
its review projection. Do not introduce machine identity or a universal semantic hash.

Rejected alternatives: implicit directory import discovery (hidden input scope),
a Git-aware network resolver (unnecessary for local reproducibility), direct parser
class access (couples components), a shared YAML source schema (requirements-only
notation decision), and a general graph engine (no present consumer).

The manifest is narrowly scoped checked-in JSON configuration; it neither selects
verification source notation nor mandates JSON/YAML for safety, BOM or evidence.
Generated snapshots/manifests/reverse indexes remain disposable. Operational read
rechecks provide detected-change failure, with the residual filesystem race stated
rather than claiming an atomic multi-file build.
