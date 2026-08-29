# mundanereq-format

Purpose: normalize LF line endings and collapse only comment-free blank-line
runs between requirement records in conforming `mundanereq-source-0.2` source.

    mundanereq-format --check FILE_OR_DIRECTORY...
    mundanereq-format --write FILE_OR_DIRECTORY...

Check mode does not modify source. Write mode is explicit. The formatter does
not reflow prose, reorder records or relationships, alter comments, interpret
opaque math, or repair invalid source.

Run `mundanereq-format --help` for all modes and `mundanereq-format --version`
for its independent tool/source versions. The complete formatting and safety
contract is packaged as
`docs/contracts/0008-formatter-trial-contract-0.1.md`.
