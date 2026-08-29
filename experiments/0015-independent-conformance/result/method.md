# Method

## Basis and scope

I treated `standard.md` as the sole authority and interpreted all 16 source selections as source sets under the `mundanereq-source-0.2` contract. The `0.1` and `0.2` directory names were treated as fixture organization, not embedded language-version directives; Clause 15.1 states that source files contain no such directive and that every conforming 0.1 source set also conforms to 0.2 with the same semantic value.

For directory selections, I recursively selected regular files with the exact `.mreq` suffix, consistently with Clause 7.2. I decoded and inspected the fixture bytes as UTF-8, checked line termination and prohibited characters, then applied the record grammar, field cardinality/order, body and math transformations, and complete-source-set constraints. I derived inventories by hand from Clauses 5 and 9, sorting requirements and unordered sets by exact Unicode scalar sequence while preserving statement/rationale block order.

In `fixture-results.tsv`, the columns are `selection`, `verdict`, `decisive_clause`, `source_path`, `line`, `column`, and `reason`. Line and column are one-based; columns count Unicode scalar values as required by Clause 6.4. `-` means that an accepted selection has no failure location. Where summary validity or diagnostics clauses repeat a specific rule, `decisive_clause` reports the most specific substantive clause first responsible for the verdict.

## Inventory notation

The inventory files use this deterministic notation:

- Requirements are ordered by identifier using exact Unicode scalar-sequence order.
- Every semantic string is a JSON-style double-quoted string. `\\`, `\"`, and `\n` represent backslash, double quote, and LF respectively; all other displayed characters are exact Unicode characters.
- `null` denotes an absent optional field and is distinct from a string.
- `statement` and present `rationale` are ordered block lists. Each block is tagged `prose` or `math latex`.
- `decomposition-targets` is an unordered semantic set displayed in sorted order.
- The final `relationships` set explicitly lists every directed decomposition relationship as `source -> target`, sorted first by source and then target. It intentionally restates the relationship represented by each requirement's decomposition-target set.

## Tools and package limitations

I used the system shell and standard command-line inspection utilities (`find`, `sort`, `grep`, `sed`, `wc`, `od`, and `tail`) for read-only inspection, plus `apply_patch`, `diff`, `awk`, and `sha256sum` to create and internally verify the result files. No programming-language runtime and no disposable checker were used.

The supplied package contained only `participant-task.md`, `standard.md`, and source fixtures (plus the result files created for this task). It supplied no expected inventories, fixture README, reference executable, implementation source, project history, or implementation diagnostics. I did not inspect any path outside the sealed package. I did not invoke any mundane-req tool or executable, use or reconstruct the maintained Java implementation, or use Java project source, Python, or Node.js. I consulted no prohibited implementation material and made no comparison with hidden or other implementation expectations.

