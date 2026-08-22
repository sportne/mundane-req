# Research 0007: Provisional Source Representation Decision

Status: Provisional decision for the next specification phase

Decision date: 2026-08-22

## Decision question

Experiment 0001 asked whether a small formally traced requirements set could remain understandable and reviewable as ordinary Git-managed source across three representation candidates.

This record decides which representation ideas are sufficiently supported to carry into the minimum language and model specification. It does not freeze a production language or implementation architecture.

## Evidence considered

- Research 0003: prior art and controlled candidate design.
- Research 0004: syntax-neutral UAS corpus and Baseline A-to-B change.
- Research 0005: record-boundary comparison and selected experimental record form.
- Research 0006: deliberately disposable authored-view fixture.
- Experiment 0001 Baseline A and Baseline B tags.
- Experiment 0001 ordinary source-diff review.
- Experiment 0001 concurrent-edit and merge review.

The evidence covers 20 requirements, 22 decomposition relationships, a mathematical statement, object additions, an identity-preserving split, relationship retargeting, authored movement, independent edits, competing normative edits, and movement concurrent with content editing.

It does not cover a large corpus, production parsing, ReqIF interchange, repository-scale performance, or long-term organizational use.

## Provisional decisions

### 1. Continue with the purpose-built record language

The keyword record form selected in Research 0005 is adequate for the next phase:

    requirement SYS-007
    title: Loss-of-link response
    allocation: Mission-control coordinator
    statement:
      Within 100 ms after the command link is declared unavailable, ...
    rationale:
      Prompt initiation of safe recovery limits continued operation ...
    source: SRC-SAFETY-001
    decomposes: OPS-001
    decomposes: OPS-004
    end requirement

It kept identity, normative text, rationale, external source, and relationships visible in raw source and ordinary diffs. Conflict markers remained understandable without rendering.

This is a decision to specify and test the form more rigorously, not a claim that every delimiter is permanent.

### 2. File boundaries are not requirements semantics

Candidate A and Candidate B use byte-identical records. Both passed the source-diff and merge workflows.

The language should therefore permit one or more complete requirement records in a source file. A repository may choose small subject modules, one requirement per file, or a documented mixture for operational reasons. A later repository profile may constrain that choice without changing the language or semantic model.

A conforming model must not infer requirement identity, hierarchy, allocation, ownership, level, or authored order from:

- filename;
- directory;
- file count;
- record position;
- line number.

The ID inside the record remains authoritative.

This does not mean file organization is unimportant. It means file organization is a repository convention and ergonomic choice rather than part of the requirement model.

### 3. Do not mandate one universal file granularity yet

The experiment found real, opposing strengths:

| Activity | Small subject modules | One requirement per file |
| --- | --- | --- |
| Read several related requirements | Strong contextual locality | Requires more file navigation |
| Inspect one known ID | Search within a few modules | Convenient direct file lookup when filenames repeat IDs |
| Review a relationship-only edit | Visible but shares a module diff | Isolated to the affected file |
| Add a requirement | Adds a record within a module | Adds an unmistakable new file |
| Merge separated edits | Clean in the tested scenario | Clean with obvious file independence |
| Resolve a same-statement conflict | Small readable hunk in a module | Same readable hunk in an object-sized file |

The tested file-per-requirement layout improved isolation, but the module layout did not exhibit the merge failures that would justify prohibiting it. Conversely, the experiment did not test enough scale to justify imposing one-file-per-requirement overhead universally.

Repository profiles may later recommend or require a consistent granularity. The core source model should not.

### 4. Preserve model and authored-order separation

Candidates A and B changed authored order by moving one ID reference. A concurrent content edit merged cleanly because the requirement record did not move.

Candidate C moved the authoritative Markdown block itself. A concurrent edit to that block conflicted and created a realistic risk of duplicate identity or lost content during resolution.

The project should preserve the ability to compose requirements independently of their storage position. The current '.mview' syntax is not selected as a product language; only the separation it demonstrates is retained.

Composition or authored order may eventually be represented by:

- a minimal source list of IDs;
- a generated artifact;
- a later, separately specified composition format.

No view feature should be added until a workflow demonstrates its need.

### 5. Do not select document-coupled Markdown as authoritative requirement storage

Candidate C remains readable and provides direct narrative context, but it performed worse on the experiment's defining model-oriented behavior:

- a view-only move became a 14-line deletion and 14-line addition;
- move-versus-edit produced a content conflict absent from Candidates A and B;
- generic Markdown rendered requirement records as code blocks rather than ordinary prose;
- authoritative object storage became coupled to one authored document position.

Candidate C is therefore rejected as the initial authoritative representation.

This is not a rejection of Markdown generally. Markdown may remain useful for:

- authored contextual views;
- generated specifications;
- review reports;
- disposable rendering;
- field content if a future workflow justifies it.

### 6. Continue to use ordinary Git merging

All conflicting normative edits were exposed as small readable conflict hunks. The document-coupled move conflict was resolvable from source and explicit IDs.

The evidence does not justify a custom semantic merge engine. A validator that detects duplicate IDs after a merge may eventually add useful safety, but it does not replace Git's merge behavior.

### 7. Treat Git commits and annotated tags as baseline mechanics

The two experimental baselines are ordinary repository commits identified by annotated tags. Their tag messages state scope and disclaim approval or certification.

The language does not need an intrinsic baseline field or per-requirement revision number to reproduce this workflow. Organizational authority, approval, and release meaning remain conventions or external evidence associated with repository snapshots.

## Candidate disposition

| Candidate | Disposition | Reason |
| --- | --- | --- |
| A — purpose-built subject modules | Continue as a supported source layout | Good contextual reading, clear diffs, and acceptable ordinary merges |
| B — same language, one record per file | Continue as a supported source layout | Strong object-level isolation and equally readable source |
| C — Markdown document with authoritative embedded blocks | Reject as the initial authoritative layout | Couples object movement to document editing and creates avoidable diff and merge cost |

Candidates A and B no longer need to be treated as competing semantic representations. They are two file-layout profiles over one provisional language and model.

## Implications for the minimum specification

The next specification phase should define only what Experiment 0001 exercised:

- explicit record boundaries;
- an authoritative human-facing ID carried inside each record;
- title;
- normative statement;
- rationale;
- allocation as a label in the initial model;
- external source reference;
- outgoing decomposition relationships;
- prose folding and structural indentation;
- explicitly delimited mathematical content;
- source files containing one or more records;
- repository-wide identity and reference validation;
- optional authored ordering that does not own requirement content;
- Git snapshot-based baseline mechanics.

It should continue to omit:

- workflow or approval status;
- per-requirement revision numbers;
- generated timestamps or hashes;
- filename-derived identity;
- configurable schemas or grammars;
- generalized metamodels;
- semantic merge;
- view styling and report queries;
- ReqIF-specific source machinery.

## Remaining questions

1. How is the repository's authoritative source-file set discovered without making directory layout semantic?
2. Which fields are required, optional, or repeatable?
3. What exact characters and whitespace are permitted in IDs and scalar fields?
4. How are duplicate IDs, dangling references, unknown fields, and malformed blocks diagnosed?
5. Does allocation remain a label or eventually become a typed relationship to a modeled external entity?
6. Is the external source reference an opaque string in the first language version?
7. Which constrained LaTeX-style profile is promised, if any, beyond preserving an opaque labeled payload?
8. Is a separate authored view necessary in the minimum language, or can it remain an experiment-only companion format?
9. What encoding and line-ending rules are required for deterministic interchange between tools?

## Next action

Draft the minimum language and semantic-model specification from these decisions. Distinguish:

- experiment-supported decisions;
- deliberately provisional grammar details;
- validation rules to test;
- open questions;
- deferred capabilities.

After that written specification is coherent, implement the smallest parser and validator needed to test deterministic interpretation. The implementation should not begin before the source-file discovery rule and required field cardinalities are explicit.
