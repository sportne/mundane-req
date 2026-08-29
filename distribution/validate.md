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
