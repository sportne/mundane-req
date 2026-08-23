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
| `invalid/prohibited-c1-control.mreq` | `control-character` at line 2, column 10 | U+0080 is prohibited physical-source content. |
| `invalid/leading-non-ascii-whitespace.mreq` | `empty-or-padded-scalar` at line 2, column 8 | U+00A0 cannot begin a scalar value. |
| `invalid/trailing-non-ascii-whitespace.mreq` | `empty-or-padded-scalar` at line 2, column 36 | U+00A0 cannot end a scalar value. |
| `invalid/supplementary-scalar-column.mreq` | `control-character` at line 2, column 10 | The prohibited U+007F follows U+1F600, which advances the source column by one Unicode scalar value. |

Diagnostic code strings are reference-probe behavior rather than standardized
cross-implementation identifiers. Each conforming interpreter must reject the
underlying violation at a useful source position.

The control and whitespace characters in the last four fixtures are
intentionally present as UTF-8 source characters. Their escaped descriptions
above make the otherwise invisible data reviewable. In
`supplementary-scalar-column.mreq`, the title prefix occupies columns 1
through 7, `A` is column 8, U+1F600 is column 9, and U+007F is column 10.
An implementation that counts UTF-16 code units would incorrectly report
column 11.

The 0.1 fixtures remain unchanged. A strict 0.1 interpreter rejects comment
syntax; every comment-free conforming 0.1 source set is also conforming 0.2
source with the same semantic value.

The shared record grammar and semantic-model failures under
[`../0.1/invalid`](../0.1/invalid) remain invalid under 0.2 and are part of the
maintained 0.2 evidence baseline. This directory adds only fixtures specific
to comments and the Unicode edge conditions exposed during formalization; it
does not duplicate every inherited invalid input.
