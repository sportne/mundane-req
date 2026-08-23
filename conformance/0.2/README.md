# mundanereq 0.2 Conformance Fixtures

These fixtures exercise the provisional `mundanereq-source-0.2` contract in
[Specification 0006](../../specification/0006-provisional-0.2-contract.md) and
the normative [mundanereq Source Language Specification
0.2](../../specification/0005-mundanereq-source-language-0.2.md).

## Compatibility fixture

[`valid/requirements.mreq`](valid/requirements.mreq) has the same requirement
semantics as the 0.1 valid fixture while adding comments at every permitted
kind of structural location: file boundaries, after an opener, between fields,
before a closer, and as the sole separator between records.

The reference probe must accept it:

    experiments/0002-deterministic-interpretation/build/mundanereq conformance/0.2/valid

Its `--inventory` output must equal [`valid/expected.inventory`](valid/expected.inventory),
which is byte-identical to the 0.1 expected inventory. This equality is the
normative example that comments have no semantic value.

## Invalid fixtures

| Input | Expected reference diagnostic | Violated rule |
| --- | --- | --- |
| `invalid/comment-only.mreq` | `empty-source-file` | Comments do not replace a required record. |
| `invalid/comment-before-body.mreq` | `empty-body` | A comment cannot occur between a prose-field label and its body. |
| `invalid/comment-splits-prose.mreq` | `malformed-record` | A comment cannot interrupt a prose body. |
| `invalid/comment-splits-math.mreq` | `unterminated-math` | A comment cannot occur within a math block. |

Diagnostic code strings are reference-probe behavior rather than standardized
cross-implementation identifiers. Each conforming interpreter must reject the
underlying violation at a useful source position.

The 0.1 fixtures remain unchanged. A strict 0.1 interpreter rejects comment
syntax; every comment-free conforming 0.1 source set is also conforming 0.2
source with the same semantic value.
