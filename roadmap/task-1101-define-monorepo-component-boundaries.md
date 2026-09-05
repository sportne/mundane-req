# Task TC-1101: Define Monorepo Component Boundaries

Status: Ready

Roadmap stage: 11

Type: Decision

Depends on: none

Unlocks: TC-1102, TC-1104

## Question

How can this repository host an engineering tooling ecosystem while requirements
remain an independently usable language and tool suite?

## Outcome

A written architecture decision assigns component responsibilities, permitted
dependencies, public artifact boundaries, and the smallest useful monorepo layout.
This enables TC-1104's bounded reorganization and TC-1102's ownership decisions.

## Work

- Map existing Java sources, tests, experiments, contracts, native commands, and
  packaging to requirements, artifact integration, verification, views, and shared
  test infrastructure. Distinguish current code from prospective responsibilities.
- Compare logical boundaries in the existing layout with physical component
  directories. Record a concrete benefit for every proposed move.
- Define which consumers use published compiled interfaces and where shared
  implementation is appropriate; describe a test that exposes accidental use of
  requirement implementation internals.
- Distinguish this tooling monorepo from users' engineering repositories. Allow
  future same-repository and cross-repository artifacts without requiring a server.
- Preserve independent command installation and source readability. Record how
  existing source, CLI, artifact-format, and package version domains remain distinct.

## Acceptance evidence

- A decision record contains a component/dependency diagram, a current-to-proposed
  path map, alternatives, and ownership of each public contract.
- A requirements-only build/use scenario excludes verification and view tooling;
  a compiled-output consumer scenario does not need original parser internals.
- Every proposed component contains an existing capability or the first bounded
  workflow; future safety, BOM, and CAD integrations appear as possibilities, not
  empty frameworks to implement.
- The decision identifies the verification workflow as the first integration and
  identifies observable checks required before moving files.

## Out of scope

- Moving code, renaming the repository, choosing a separate language per artifact,
  a universal engineering metamodel, registries, or plugin infrastructure.

## Compatibility and affected components

Plan preservation of current .mreq semantics, human-authored IDs, explicit source
selection, native commands, and historical experiment paths. Likely components:
root documentation, src/, Makefile, distribution/, and future component boundaries.

## Completion decision

Select only boundaries with an identified consumer or independence benefit. Keep
the current physical layout where movement adds coordination without improving a
checkable boundary. Revisit any design that requires all ecosystem tools to use
requirements.

## References

- [Roadmap](0001-initial-roadmap.md)
- [Project foundation](../specification/0001-project-foundation.md)
- [Implementation lineage](../research/0011-maintained-implementation-lineage.md)
