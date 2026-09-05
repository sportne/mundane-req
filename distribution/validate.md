# mundanereq-validate

Purpose: determine whether explicitly selected requirements files form a
conforming `mundanereq-source-0.2` source set.

    mundanereq-validate FILE_OR_DIRECTORY...

The command reads source and reports diagnostics; it does not modify files,
format source, answer trace questions, or enforce project policy. Status `0`
means conformance, `1` means readable source did not conform, and `2` means an
invocation or input failure prevented validation.

Run `mundanereq-validate --help` for the concise invocation summary and
`mundanereq-validate --version` for its independent tool/source versions. The
complete interface is packaged as
`docs/contracts/0007-validator-trial-contract-0.1.md`.


## Explicit YAML requirements mode

A leading `--source=yaml-0.3` selects Requirements YAML 0.3; the default remains
custom source 0.2. The selector precedes other modes/operations. The current
source contract and safety addendum are included under docs/contracts in the
native package and under specification in the repository. Output delivery failure
returns non-success, including diagnostic-only paths. See examples/yaml/README.md
in the repository for validated authoring and migration commands.
