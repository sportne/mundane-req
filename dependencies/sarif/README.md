# SARIF schema verification dependency

Unmodified OASIS SARIF 2.1.0 Errata 01 schema downloaded from the
[dated standard's schema](https://docs.oasis-open.org/sarif/sarif/v2.1.0/errata01/os/schemas/sarif-schema-2.1.0.json).
`SHA256SUMS` pins the exact downloaded bytes; `NOTICE.txt` preserves the associated
specification's notices. It is used only by the independently pinned Python
jsonschema verifier in `make sarif-verify`, not loaded by native commands.
