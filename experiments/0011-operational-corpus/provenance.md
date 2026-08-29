# Operational Corpus Provenance

Status: Frozen trial input

Date: 2026-08-29

## Selected corpus

All requirement titles, statements, rationales, identifiers, and relationships
under `requirements/` are original fictional material written for mundane-req
Experiment 0011. They describe a hypothetical watershed monitoring service and
do not specify an actual product. No requirement text was copied or paraphrased
from the projects surveyed below.

The corpus is distributed under mundane-req's BSD 3-Clause License. Its exact
provenance is this repository history beginning with TC-0701. This avoids
uncertain document rights, incompatible data obligations, misrepresenting an
upstream hierarchy as decomposition, and the project's stated organizational
constraint against adopting StrictDoc material directly.

## Public corpora inspected

| Candidate | Exact inspected revision | License observed | Useful evidence | Reason not imported |
| --- | --- | --- | --- | --- |
| NASA cFE | `84ef3d2ae9bb440d9f0511d27126a61ed3c43ee2` | Apache-2.0 repository license | `docs/cFE_FunctionalRequirements.csv` contains 596 data rows, including 414 unique nonblank requirement IDs, with descriptions, rationales, and realistic subsystem breadth | The inspected repository does not supply the authored subsystem-to-functional mapping needed by this trial; its SRS says formal heritage trace was omitted and discusses a separate traceability matrix, so inferring `decomposes` links from dotted IDs or sections would manufacture semantics |
| Doorstop | `cf937ece06e86df363d86b667ac9468063fe925b` | LGPL-3.0 | Self-requirements and tutorial data demonstrate one-item-per-file authoring, document trees, review stamps, and links | Corpus is smaller than the cFE source, tightly expresses Doorstop's own model, and would introduce avoidable license/provenance coupling |
| OpenFastTrace | `5628aef4c007a71fb3677d7b3b963c0880812b56` | GPL-3.0 | Self-trace material demonstrates multi-artifact coverage chains, dependencies, revisions, and Markdown source | It intentionally mixes requirements with design, implementation, and tests, outside the requirements-only trial, and would introduce GPL corpus material |
| StrictDoc | `c675ed44efa2438b72cfbeb52ec6dcb77b987e36` | Apache-2.0 | L1 system, L2 high-level, and L3 low-level documents demonstrate a large three-level requirements corpus | Import would conflict with the project's explicit instruction to treat StrictDoc only as prior art unless that provenance decision is revisited |

The survey therefore found public material that was legally clear or
structurally useful, but not one corpus that simultaneously met the
requirements-only, multi-level trace, redistribution/provenance, and project
constraint criteria. TC-0701's original-corpus fallback applies.

## Structural influence, not copied content

The original corpus deliberately adopts only commonplace structures observed
across formal requirements practice and the surveyed projects:

- stable human-facing IDs;
- concise requirement statements and occasional rationale;
- six operational roots, system-level decomposition, and component-level decomposition;
- subsystem breadth large enough to make searching and trace navigation useful;
- files grouped by subject without assigning semantics to those files.

No upstream field, hierarchy, review status, revision field, artifact type,
coverage policy, or document model was added to the mundane-req language.

## Sources

- [NASA cFE repository](https://github.com/nasa/cFE)
- [Doorstop repository](https://github.com/doorstop-dev/doorstop)
- [OpenFastTrace repository](https://github.com/itsallcode/openfasttrace)
- [StrictDoc repository](https://github.com/strictdoc-project/strictdoc)
