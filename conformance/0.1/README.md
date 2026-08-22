# mundanereq 0.1 Conformance Fixtures

These fixtures exercise the provisional `mundanereq-source-0.1` contract in [Specification 0003](../../specification/0003-provisional-0.1-contract.md).

## Valid fixture

[`valid/requirements.mreq`](valid/requirements.mreq) contains three requirements and three decomposition relationships. It covers required and optional fields, prose folding, two paragraphs, an opaque LaTeX block, and multiple outgoing relationships.

The reference probe must accept it:

    experiments/0002-deterministic-interpretation/build/mundanereq conformance/0.1/valid

Its `--inventory` output must equal [`valid/expected.inventory`](valid/expected.inventory).

## Invalid fixtures

| Input | Expected reference diagnostic |
| --- | --- |
| `invalid/dangling-reference.mreq` | `dangling-reference` |
| `invalid/duplicate-id/` | `duplicate-id` |
| `invalid/duplicate-relationship.mreq` | `duplicate-relationship` |
| `invalid/missing-statement.mreq` | `missing-field` |
| `invalid/unknown-field.mreq` | `unknown-field` |
| `invalid/unterminated-math.mreq` | `unterminated-math` |

The code strings are reference-probe behavior rather than standardized cross-implementation identifiers. Each conforming interpreter must reject the underlying violation with a useful source position and understandable category.

These fixtures are intentionally small. The Experiment 0002 test harness covers the broader physical-source, record-boundary, field-order, indentation, identity, and source-discovery matrix required by Specification 0002.
