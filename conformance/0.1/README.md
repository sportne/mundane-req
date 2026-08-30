# mundanereq 0.1 Conformance Fixtures

These fixtures exercise the provisional `mundanereq-source-0.1` contract in [Specification 0003](../../specification/0003-provisional-0.1-contract.md) and the normative [mundanereq Source Language Specification 0.1](../../specification/0004-mundanereq-source-language-0.1.md).

## Valid fixture

[`valid/requirements.mreq`](valid/requirements.mreq) contains three requirements and three decomposition relationships. It covers required and optional fields, prose folding, two paragraphs, an opaque LaTeX block, and multiple outgoing relationships.

The historical 0.1 probe established the original result. The maintained 0.2
validator can exercise this fixture as a backward-compatibility case:

    make native-validator
    build/maintained/mundanereq-validate conformance/0.1/valid

The historical probe's `--inventory` output must equal
[`valid/expected.inventory`](valid/expected.inventory).
The inventory is language conformance material; the maintained validator does
not expose that historical probe interface. Acceptance by the 0.2 validator
demonstrates the claimed 0.1-to-0.2 compatibility for this selection, not a
separate maintained 0.1 implementation.

## Invalid fixtures

| Input | Expected reference diagnostic |
| --- | --- |
| `invalid/dangling-reference.mreq` | `dangling-reference` |
| `invalid/duplicate-id/` | `duplicate-id` |
| `invalid/duplicate-relationship.mreq` | `duplicate-relationship` |
| `invalid/missing-statement.mreq` | `missing-field` |
| `invalid/unknown-field.mreq` | `unknown-field` |
| `invalid/unterminated-math.mreq` | `unterminated-math` |

The code strings record historical reference-probe behavior rather than
standardized cross-implementation identifiers. Each conforming 0.1
interpreter must reject the underlying violation with a useful source position
and understandable category. The maintained 0.2 validator also rejects these
shared violations, but its separate CLI contract controls its diagnostics.

These fixtures are intentionally small. The Experiment 0002 test harness covers the broader physical-source, record-boundary, field-order, indentation, identity, and source-discovery matrix required by the language standard.
