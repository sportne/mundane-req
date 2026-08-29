# Independent Conformance Interpretation Task

You are evaluating a proposed textual requirements language from its written
standard and source fixtures. Work independently of the maintained mundane-req
implementation.

## Supplied material

- `standard.md`: the complete normative source-language 0.2 standard.
- `fixtures/`: source files only, preserving the conformance directory layout.
- this task.

No expected inventories, fixture README, reference executable, Java source,
project history, prior review, or implementation diagnostics are supplied.

## Constraints

- Treat `standard.md` as authoritative.
- Do not inspect files outside this package or invoke mundane-req executables.
- Do not use or reconstruct the maintained Java implementation.
- Do not use Python or Node.js.
- You may create a disposable checker from the standard, but it must remain
  inside `result/disposable-checker/` and must not be presented as a production
  implementation.
- Freeze all conclusions before any comparison with another implementation.

## Source selections

Interpret these 16 selections independently:

1. `fixtures/0.1/valid/`
2. `fixtures/0.1/invalid/dangling-reference.mreq`
3. `fixtures/0.1/invalid/duplicate-id/`
4. `fixtures/0.1/invalid/duplicate-relationship.mreq`
5. `fixtures/0.1/invalid/missing-statement.mreq`
6. `fixtures/0.1/invalid/unknown-field.mreq`
7. `fixtures/0.1/invalid/unterminated-math.mreq`
8. `fixtures/0.2/valid/`
9. `fixtures/0.2/invalid/comment-before-body.mreq`
10. `fixtures/0.2/invalid/comment-only.mreq`
11. `fixtures/0.2/invalid/comment-splits-math.mreq`
12. `fixtures/0.2/invalid/comment-splits-prose.mreq`
13. `fixtures/0.2/invalid/leading-non-ascii-whitespace.mreq`
14. `fixtures/0.2/invalid/prohibited-c1-control.mreq`
15. `fixtures/0.2/invalid/supplementary-scalar-column.mreq`
16. `fixtures/0.2/invalid/trailing-non-ascii-whitespace.mreq`

## Required result

Create `result/` containing:

1. `method.md`: method, tools/languages used, package limitations, and explicit
   confirmation that no prohibited implementation material was consulted.
2. `fixture-results.tsv`: one row per selection with selection, accepted or
   rejected, first decisive standard clause, source path, one-based line and
   Unicode-scalar column where applicable, and a plain-language reason.
3. `valid-0.1.inventory` and `valid-0.2.inventory`: deterministic complete
   semantic inventories derived independently, including every requirement ID,
   scalar value, prose/math block, and decomposition relationship. State the
   inventory notation in `method.md`.
4. `standard-findings.md`: every ambiguity, internal inconsistency, fixture
   uncertainty, or interpretation choice. Write `None.` only if there are none.
5. `SHA256SUMS`: checksums of every result file other than `SHA256SUMS` itself.

Do not compare your result with hidden expected results. Stop after the result
is internally checked and checksummed.
