# Experiment 0032: SARIF validation diagnostics

`make sarif-verify` builds the validator and runs six golden cases through JVM and
native commands: clean source, multiple files with Unicode/byte failures, a fully
parsed semantic violation, custom recovery, YAML mapping errors and a missing
input. Output is checked against the SHA-256-pinned OASIS schema using the existing
pinned jsonschema test dependency. No forge upload or external editor was exercised.

The bounded consumer in `scripts/check-sarif.py` resolves each relative URI against
the source root, decodes filenames and navigates the line/code-point position.
The invalid fixture names exercise spaces, Greek text, supplementary Unicode,
percent and hash characters. Exact expected positions are line 2, column 9 for
invalid UTF-8 after an emoji and line 2, column 10 for padded scalar content.
A temporary colon-containing filename verifies a URI cannot be mistaken for a
scheme. Reversed input selection yields identical bytes.

Invocation, prefix, flush and closed-stream tests live in `SarifOutputTest`;
the public matrix also checks actual JVM/native closed stdout, closed stderr
on an invocation diagnostic, and a broken pipe after a prefix. Quiet SARIF output
does not write stderr; external closure of that unused descriptor is not detected. Each subprocess has a 30-second timeout. Invalid input may
produce valid SARIF but still returns failure; incomplete interpretation is visible.

All source examples are synthetic project-authored BSD-licensed fixtures.
[Selected contract](../../specification/0017-sarif-validation-output.md).
