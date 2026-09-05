# Experiment 0026: Requirements YAML batch evidence

The maintained implementation now provides explicit requirements YAML 0.3,
source-preserving formatting and checked migration, together with CLI output and
snapshot fixes. This directory preserves before/after safety evidence; maintained
conformance and command tests are under src/test/java and conformance/0.3.

After `make test`, reproduce the pre-fix observations against commit `1c6ec7f`:

```sh
python3 experiments/0026-yaml-requirements-batch/regression-proof.py
```

The proof compiles the historical sources into ignored build output, simulates a
failed stdout and a source edit after selection, then applies the same observations
to current classes. It does not modify historical commits or current source.
Expected outcomes are asserted: old false-success/source overwrite; new status 2
and preserved external edit. Reflection is used only in this historical test driver
to observe the old private batch write function, not as an implementation contract.
Maintained tests exercise observable outcomes through supported internal test seams.

Reproduce the full current evidence with `make verify` under Java 21/GraalVM,
Python 3 with venv, curl, Ruby and the existing native build utilities. This includes
JVM tests, native boundaries and package verification, original legacy corpora,
YAML command parity and migration, and an independent Draft 2020-12 schema check.
No release is published by these commands. The migration utility is independently
built by `make native-migrate` and is not added to the existing three-tool archive.

See the [batch verification record](../../research/0035-yaml-requirements-batch-verification.md),
[source specification](../../specification/0010-requirements-yaml-0.3.md),
[command addendum](../../specification/0011-tool-safety-and-yaml-commands.md), and
[authoring guide](../../examples/yaml/README.md). Other artifact authoring formats
remain unresolved. Parser-specific broad performance, independent usability and
external-tool ReqIF compatibility are outside these recorded checks.
