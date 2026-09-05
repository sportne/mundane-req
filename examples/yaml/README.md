# Authoring requirements in YAML

Requirements YAML 0.3 is an explicitly selected, experimental requirements format.
Other artifact source formats remain independent decisions. Human-authored files
and IDs remain authoritative; tools validate, format and derive relationships.

Build the maintained tools with Java 21 and GraalVM Native Image:

```sh
make native-suite native-migrate
build/maintained/mundanereq-validate --source=yaml-0.3 examples/yaml/vaccine-monitoring
build/maintained/mundanereq-format --source=yaml-0.3 --check examples/yaml/vaccine-monitoring
build/maintained/mundanereq-trace --source=yaml-0.3 impact NEED-001 examples/yaml/vaccine-monitoring
```

[The source specification](../../specification/0010-requirements-yaml-0.3.md)
defines the model, mapping, structural schema and semantic rules. Each file declares
`format: "mundanereq-yaml-0.3"` and holds a `requirements` list. A file may contain
one requirement or many. Directory selection uses `.mreq.yaml` only; the explicit
selector prevents interpreting unrelated project YAML or legacy requirements.

Quote all scalar values, including IDs and math language. Use `>-` for wrapped
single paragraphs, separate list entries for paragraphs, and `|-` for opaque math
when no terminal payload newline is intended. The [author-written example](../../conformance/0.3/authoring/requirements.mreq.yaml)
shows comments, folded prose, single quotes, ordered math/prose and relationships.
Unknown keys, duplicate IDs/keys, unresolved references and invalid values fail.
Custom attributes are not yet part of this source contract.

Formatting preserves all source text except physical CRLF-to-LF normalization.
It does not reindent, rewrap paragraphs, reorder fields or erase comments. Write
mode detects intervening edits before replacement, with the remaining filesystem
race documented in the [command addendum](../../specification/0011-tool-safety-and-yaml-commands.md).

## Migration and compatibility

Default validator/formatter/trace invocation retains the custom 0.2 contract.
Historical files and versioned conformance evidence remain valid under their
original selectors. YAML uses an explicit selector and document identifier; it
is not a silent rewrite of the meaning of `.mreq`.

```sh
build/maintained/mundanereq-migrate --dry-run build/my-yaml-project conformance/0.2/valid
build/maintained/mundanereq-migrate build/my-yaml-project conformance/0.2/valid
build/maintained/mundanereq-validate --source=yaml-0.3 build/my-yaml-project
```

Choose a new output directory whose parent exists. The converter checks complete
semantic equality before writes and never overwrites originals. It preserves
comment lines in order at the new file header; inspect their relocated placement.
Opaque math is emitted as quoted text with newline escapes to guarantee exact
payload preservation. Existing outputs, filename collisions and unsupported source
fail explicitly. An interrupted write can leave partial output; inspect it and
retry to a new directory. Retiring the legacy adapter needs a later compatibility
decision after recorded migration coverage; permanent dual-format support is not
promised.

## Example provenance

- `vaccine-monitoring/`: 57 requirements converted from the preserved formal-
  traceability pilot source under experiments/0024-vaccine-monitoring-pilot/product/requirements.
- `operational/`: 60 requirements converted from the preserved bounded operational
  corpus under experiments/0011-operational-corpus/requirements.

These YAML files are maintained authoring examples. The old experiment sources
remain historical evidence; select one source set explicitly when using tools.
The [migration inventory](../../conformance/0.3/migration-corpus.tsv) records the
pairs checked by JVM/native tests. Requirement values and IDs are unchanged;
selected source snapshots and their attribution remain in the original experiments.
