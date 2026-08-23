# Task TC-0102: Correct Reference Parser Conformance

Status: Ready

Roadmap stage: 1

Type: Implementation

Depends on: TC-0101

Unlocks: TC-0103

## Question

Can the experimental Java parser satisfy the formal 0.2 Unicode rules without disturbing existing source interpretation?

## Outcome

The three known parser deviations are repaired and all old and new conformance tests pass on the JVM and as a GraalVM native image.

## Work

- Reject all prohibited C1 controls required by the standard.
- Apply the standard's explicit Unicode whitespace set symmetrically at scalar boundaries.
- Count diagnostic columns in Unicode scalar values rather than UTF-16 code units.
- Add focused regression tests mapped to the TC-0101 fixtures.
- Run the complete Experiment 0002 and source-comment test harnesses.

## Acceptance evidence

- All TC-0101 fixtures receive the specified result and coordinate.
- Existing 0.1 and 0.2 semantic inventories remain unchanged.
- The JVM and native-image test runs pass.
- The repair does not add a third-party dependency or broaden accepted syntax.

## Out of scope

- Extracting production modules from the probe.
- Redesigning diagnostics.
- Adding language features or formatter support.

## Completion decision

If a repair requires a semantic change, stop and return to the provisional standard. Otherwise record the probe as behaviorally aligned with 0.2 for the audited cases.

## References

- [TC-0101](task-0101-strengthen-unicode-conformance-fixtures.md)
- [Experiment 0002](../experiments/0002-deterministic-interpretation/README.md)
- [0.2 language standard](../specification/0005-mundanereq-source-language-0.2.md)
