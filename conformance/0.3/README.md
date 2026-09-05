# Requirements YAML 0.3 conformance

The normative [source contract](../../specification/0010-requirements-yaml-0.3.md)
is checked by `make test`, `make yaml-verify`, and `make yaml-schema-verify`;
all are included in `make verify`. Python packages used for independent schema
verification are pinned under dependencies/schema-test-requirements.txt and
installed into ignored build/schema-check-venv. They are test-only dependencies.

- `valid/`: three migrated source 0.2 records, with exact expected semantics in
  the preserved conformance/0.2/valid corpus.
- `authoring/`: two hand-authored records exercising folded prose, literal math,
  flow collections, single quotes, inline comments and document markers.
- `invalid/` and `invalid-cases.tsv`: golden diagnostic rule/line/code-point-column
  expectations and an independent structural-schema verdict. `none` means duplicate
  keys must be detected before constructing the schema's object instance.
- `migration-corpus.tsv`: three current comparison pairs, 120 equivalent requirement
  values across 11 files. Generated examples remain checked-in authoring examples;
  reproduction uses the maintained migration utility and byte comparisons.

JVM tests also run deterministic generated/mutated identifiers, unknown fields,
resource bounds, malformed bytes, multi-error/multi-file invalid input, migration
failures, formatter snapshot changes and output failure injection. Native tests
compare complete command results and exercise real closed descriptors/broken pipes.
No independent human usability, broad platform support or ReqIF interoperability
result is implied by this conformance corpus.
