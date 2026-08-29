# Frozen Stage 7 Trial Protocol

Status: Frozen before TC-0702 and TC-0703 execution

Date: 2026-08-29

## Corpus profile

The subject-file layout contains exactly 60 requirements and 54 authored
`decomposes` relationships across six files. There are six operational roots,
18 system-level requirements, and 36 component-level requirements. Every root
has three direct children and every system requirement has two direct children,
giving three requirement levels and maximum path length two.

Files are non-semantic. The semantic oracle for every equivalent layout is the
frozen fingerprint of the complete ID, field-content, and relationship inventory
enforced by `OperationalCorpusVerificationTest`, not path names, record order,
or file count. The test also enforces the stated per-level fanout. The
user-facing validator independently supplies conformance and counts; it does
not expose an interchange inventory.

## Evidence rules common to both trials

Before each scenario, record the Git commit, tool versions, supplied documents,
starting layout, and exact task text. Preserve ordinary source diffs, commands,
exit statuses, diagnostics, trace output used for decisions, conflicts, and
assistance. Generated reports and indexes remain disposable.

Classify each observation as one or more of:

- **language/model:** source cannot express the intended requirement semantics;
- **tool:** parser, formatter, validator, or trace behavior causes friction;
- **Git/forge:** branch, diff, move, conflict, review, or baseline behavior causes friction;
- **policy/domain:** the language is valid but a project-specific correctness rule is needed;
- **documentation/training:** the participant cannot discover correct use from supplied material.

Do not add grammar, tool behavior, project configuration, coaching conventions,
or a product-style feature during either trial. Record the pressure first.

Stop a scenario if a tool mutates source outside explicit formatter write mode,
source or Git history would be lost, the participant requests unavailable
domain facts, or continued attempts would only repeat the same failure. Record
the stop, preserve the smallest counterexample, and continue with independent
scenarios when safe. A single participant supplies case evidence, not
statistical usability evidence.

## TC-0702 independent-author protocol

Give the participant only clean copies of:

- `specification/0005-mundanereq-source-language-0.2.md`;
- `specification/0007-validator-trial-contract-0.1.md`;
- `specification/0008-formatter-trial-contract-0.1.md`;
- `specification/0009-trace-trial-contract-0.1.md`;
- `distribution/validate.md`, `distribution/format.md`, and `distribution/trace.md`;
- `mundanereq-validate trial-0.1`, `mundanereq-format trial-0.1`, and
  `mundanereq-trace trial-0.1` native executables, each reporting source
  contract `mundanereq-source-0.2` and built from Git tag
  `native-suite-trial-0.1`;
- the subject-file corpus; and
- [`independent-author-tasks.md`](independent-author-tasks.md).

Do not provide project history, roadmap, research records, this protocol's
expected outcomes, oral syntax instruction, or hidden naming conventions.
Clarifications and all other assistance must be quoted in the result.

Measure task completion, semantic correctness, invalid attempts before success,
diagnostic-guided repairs, commands consulted, elapsed interaction steps,
questions, assistance, final source diff readability, and whether correctness
was understood or achieved only by trial and error.

## TC-0703 multi-author and layout protocol

Construct a one-record-per-file layout mechanically from the frozen subject
layout and prove equal frozen semantic fingerprints with the test-only shared
interpreter oracle; independently require equal validator requirement and
relationship counts while expecting the subject layout to report six files and
the one-record layout to report 60. Run each scenario from a fresh baseline in
both layouts:

1. **Separated edits:** change `SENSOR-LEVEL-001` wording and
   `AUDIT-QUERY-001` wording on separate branches.
2. **Overlapping edit:** two branches change the 60-second notification bound in
   `SYS-ALERT-NOTIFY-001` to different values.
3. **Move versus edit:** one branch moves `EDGE-STORE-001` between subject files
   (or renames its one-record file); another changes its statement.
4. **Relationship retarget:** first create a scenario baseline that adds
   `EDGE-ALERT-QUEUE-001` below `SYS-ALERT-NOTIFY-001`. From that shared
   scenario baseline, one branch changes the queue statement while another
   retargets it to `SYS-RESILIENT-STORE-001` after review feedback.

Each branch must format, validate, and run relevant trace questions before
merge. Record Git's actual merge result, conflict hunks, resolution commands,
post-resolution inventory, ordinary diff, changed file count, and authored
source operations. Establish annotated before/after tags. Do not use semantic
merge software or treat fewer conflicts as automatic evidence of better
semantics.

## Decision bounds

The trials may recommend documentation, diagnostics, project convention, or a
new experiment. They may not mandate a file granularity or add source-language
features. A model or grammar change requires a preserved workflow failure that
cannot be addressed by ordinary source, current tools, Git/forge behavior, or a
separate project policy.

All textual participant inputs are identified by Git tag
`operational-trial-protocol-0.1`. At trial start, record that tag's commit, each
binary's `--version` output, and each binary's SHA-256 checksum. A rebuilt native
image need not be byte-identical, so the versioned source tag is the durable
tool identity and the checksum identifies the exact executable used.
