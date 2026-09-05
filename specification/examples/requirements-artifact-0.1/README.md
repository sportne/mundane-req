# Semantic output contract fixtures

These are reviewed design expectations, written before the emitter implementation.
Each source directory is its own --root and source selection. JSON siblings are
expected compiled output; unknown.json is an intentionally unsupported consumer
input. The compiler must not generate unknown.json.

- valid/: two files, every model field, optional nulls, opaque multiline math,
  a decomposition reference and Unicode before a later field (code-point spans).
- invalid/: missing title, diagnostic-only artifact.
- duplicate/: duplicate ID in two files, second source-linked definition.
- dangling/: unresolved reference with its precise token-start location.
- unknown.json: version rejection before analysis.

Run `python3 specification/examples/requirements-artifact-0.1/consume.py FILE.json`.
Only valid.json is accepted (IDs A and B). The script is a minimal boundary consumer,
not a general JSON Schema validator. It reads neither source files nor parser
implementation classes. TC-1202 will add legacy/block-source regression fixtures
and verify exact native/JVM output bytes against these expectations.
