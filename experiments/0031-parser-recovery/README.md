# Experiment 0031: Bounded parser recovery

The checked-in custom source contains valid BEFORE/BETWEEN/AFTER records, two
malformed records, and a second file referencing BEFORE and BAD-A. YAML supplies
the same five-record sequence with two independently invalid mappings.

`results/before-*` records the native compiler at commit 72e385b before modifying
recovery. The custom case reports only the first primary error and two downstream
dangling errors, including the actually present BEFORE. `results/after-*` contains
the reviewed public-output golden: primary errors at records.mreq:8:1 and :21:1,
no misleading dangling findings, and no partial semantic publication. YAML keeps
its two primary mapping errors. The Java test separately checks recovered records,
which intentionally never enter incomplete compiled output.

Reproduce with `make recovery-verify` using the documented Java 21/GraalVM build.
`RecoveryVerificationTest` includes 100 deterministic cases (seed 1403, 30 records
per case), missing terminators, malformed headers, indented/math lookalikes,
unterminated math, invalid UTF-8, invalid YAML envelopes, syntax-fatal YAML,
semantic-only reference errors and the 100-diagnostic cap. The public command
matrix checks JVM/native diagnostic parity and formatter byte preservation.
The generator is bounded by case/record counts; the public subprocess matrix has
30-second per-invocation timeouts. Full integration remains under `make verify`.

These synthetic fixtures are project-authored under the repository BSD license.
They do not represent human usability sessions or independent parser evidence.
[Recovery contract](../../specification/0016-diagnostic-recovery.md).
