# Source Register

Status: Baseline A input set

Retrieved: 2026-08-30

The source fields in `.mreq` records use the identifiers and clause locators in
this register. Source references establish provenance; they do not claim that
the pilot requirement is a verbatim transcription or that the hypothetical
product conforms to the referenced document.

| ID | Role | Document and revision | Locator |
| --- | --- | --- | --- |
| `WHO-EM01.2` | Primary product-performance input | WHO/PQS/E006/EM01.2, *Equipment Monitoring Devices for Equipment Monitoring Systems*, last revised 2023-11-30 | https://extranet.who.int/prequal/key-resources/documents/pqs-performance-specification-e006em012-equipment-monitoring-devices-0 |
| `WHO-DL01.3` | External logger/interface definition | WHO/PQS/E006/DL01.3, *Data logger and machine-to-machine interface for Equipment Monitoring Systems*, last revised 2025-05-27 | https://extranet.who.int/prequal/key-resources/documents/pqs-performance-specification-e006dl013-data-logger-and-machine-machine |
| `WHO-DS01.2` | Data-format input | WHO/PQS/E006/DS01.2, *Data standards for cold chain equipment monitoring*, last revised 2023-11-30 | https://extranet.who.int/prequal/key-resources/documents/pqs-performance-specification-e006ds012-data-standards-cold-chain-0 |
| `WHO-EM01-VP.2` | Verification-method input | WHO/PQS/E006/EM01-VP.2, *Independent type-testing protocol: Equipment Monitoring Devices for Equipment Monitoring Systems*, last revised 2023-11-30 | https://extranet.who.int/prequal/key-resources/documents/pqs-independent-type-testing-protocol-e006em01-vp2-equipment-monitoring-0 |
| `CDC-TOOLKIT-2026-07` | Operational guidance | CDC, *Vaccine Storage and Handling Toolkit*, July 2026 | https://stacks.cdc.gov/view/cdc/258326 |
| `CDC-PINK-5` | Operational guidance | CDC Pink Book, Chapter 5, *Vaccine Storage and Handling*, web edition retrieved 2026-08-30 | https://www.cdc.gov/pinkbook/hcp/table-of-contents/chapter-5-vaccine-storage-and-handling.html |
| `PILOT-CONTEXT` | Project decision | [`system-context.md`](system-context.md), Baseline A context | Repository artifact |
| `CR-001` | Authorized pilot change | [`change/CR-001.md`](change/CR-001.md), five-year hosted-data period | Repository artifact |

## Source roles

`WHO-EM01.2` is the principal source for the selected external, local-and-remote
EMD concept. `WHO-DL01.3` and `WHO-DS01.2` define assumptions at its logger and
data boundaries. `WHO-EM01-VP.2` informs verification methods rather than
creating product requirements by itself. CDC publications describe operational
needs and user practices; they are not treated as a qualification standard for
SVEMS.

The pilot intentionally selects a coherent subset of source clauses. It does
not claim exhaustive coverage of any source document.
