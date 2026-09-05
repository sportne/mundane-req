# Task TC-1107: Interpret and Validate YAML Requirement Source

Status: Planned

Roadmap stage: 11

Type: Implementation

Depends on: TC-1106

Unlocks: TC-1108

## Question

The selected YAML direction has only experimental adapters. Maintained commands
need consistent profile validation and domain semantics, not merely YAML parsing.

## Outcome

An explicitly selected YAML input path constructs the specified requirement model
and truthful diagnostics on JVM and native builds.

## Work

- Integrate a pinned ordinary Java YAML parser and the selected profile, preserving
  duplicate-key detection and source marks before object construction.
- Implement schema-equivalent structural checks and domain validation, including
  decoded text restrictions, source-set uniqueness, references and partial input.
- Apply format/discovery/version selection from TC-1106. Route validator and trace
  through the shared interpretation boundary; coordinate recovery with TC-1403.
- Record parser/schema-library selection, license notices, reproducible dependency
  setup, native compatibility and resource limits; avoid production Python glue.

## Acceptance evidence

- Positive and negative fixtures cover each selected profile rule, exact text/math
  values, multiple files, malformed UTF-8, duplicate keys/IDs and unresolved links.
- Golden diagnostics distinguish syntax, profile and domain failures with the
  specified locations and incomplete-result behavior; exit/output checks reuse
  TC-1402's contract where available.
- JVM/native results agree on the selected corpus. Maintained verification passes
  and source-version compatibility fixtures prove intended legacy behavior.
- Dependencies are pinned and license/package effects documented. No accepted
  YAML construct bypasses limits or silently overwrites authored values.

## Out of scope

No new semantic fields, universal artifact parser, structural formatter, or
conversion of maintained authoring corpora.

## Compatibility, affected components and completion decision

Likely components: Interpreter, source discovery/model, validator/trace entry
points, dependency metadata, tests and native packaging. Experimental parser-only
native evidence is insufficient for acceptance. Stop on semantic drift or missing
source provenance; do not change the selected contract to conceal a library gap.

## References

- [Roadmap](0001-initial-roadmap.md)
- [Representation decision](../research/0033-yaml-source-representation-decision.md)
- [Comparison and specification outline](../experiments/0025-yaml-source-comparison/README.md)
- [TC-1106](task-1106-specify-yaml-requirement-source-profile.md)
