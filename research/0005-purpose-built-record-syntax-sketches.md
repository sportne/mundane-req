# Research 0005: Purpose-Built Record Syntax Sketches

Status: Record form selected for the source experiment; not a final language decision

Decision date: 2026-08-22

## Purpose

This study compares three small record syntaxes for Candidates A and B of the source experiment. It asks:

> What is the least machinery needed to make requirement boundaries, fields, multiline content, relationships, and embedded mathematics obvious to both a reader and a future parser?

These are alternatives for one shared purpose-built language, not additional representation candidates. Once a record syntax is selected, Candidate A will place several records in a module and Candidate B will place exactly one unchanged record in each file. File granularity must not influence this decision.

The examples are sketches for inspection, not a grammar specification and not implementation input.

## Controlled meaning

Every sketch encodes the same provisional fields:

- `ID`, carried by the record opener;
- `title`;
- `allocation`, currently a label rather than a modeled relationship;
- `statement`;
- `rationale`;
- zero or one external `source` reference;
- zero or more outgoing `decomposes` relationships.

The syntax does not encode the corpus's editorial `Level` or `Notes`. It also does not add status, approval, revision, timestamps, relationship objects, a schema declaration, or extension fields.

`source` and `decomposes` are omitted when absent. Every record in the frozen corpus has at least one of them: an external source for a self-derived requirement, one or more higher-level requirements that it decomposes, or both. Whether a future validator should require this universally is not decided by the syntax study.

All sketches use one relationship per line. This makes an added, removed, or retargeted relationship a local line diff and avoids introducing list punctuation.

## Shared embedded-mathematics convention under test

The sketches distinguish record syntax from field-content notation. Ordinary field content is prose. A statement may contain a visibly delimited mathematical fragment:

    math latex
      k^\ast = \min\left\{k \in \{0,1,2,\ldots\} \mid
      t_0 + k T_{\mathrm{eval}} - t_{\mathrm{last}} \ge T_{\mathrm{loss}}\right\},
      \qquad
      t_{\mathrm{detect}} = t_0 + k^\ast T_{\mathrm{eval}}
    end math

The intended language name is `latex`, but this does not promise support for complete LaTeX documents, packages, project-defined macros, or arbitrary executable commands. The first corpus uses only the commands already present in Research 0004.

For indentation-based sketches, a tool would remove the structural common indentation from the mathematical payload before rendering. The visible backslash commands and formula text remain unchanged. Whether that small normalization is preferable to column-zero raw payload is part of this comparison, not a settled rule.

## Sketch 1 — keyword records with indented field bodies

### Shape

A record starts with `requirement ID` and ends with `end requirement`. Short fields can remain on one line. Multiline field bodies are indented. Fixed field names occur only at column zero, so colons inside indented prose have no structural meaning.

    requirement SYS-007
    title: Loss-of-link response and record
    allocation: Mission-control coordinator
    statement:
      Within 250 ms after the command link is declared unavailable, the mission-control system shall cause the ground-control adapter to begin the first transmission attempt of a safe-recovery command for the active vehicle and shall record the link-loss declaration time, active vehicle identifier, and command identifier.
    rationale:
      Safe recovery must begin promptly, and the response must be reconstructable after the event.
    source: SRC-SAFETY-001
    decomposes: OPS-001
    decomposes: OPS-004
    end requirement

The mathematical requirement would contain:

    requirement SYS-006
    title: Command-link loss determination
    allocation: Link monitor
    statement:
      While a vehicle is active, the mission-control system shall declare its command link unavailable at the first link-evaluation instant defined by the following mathematical expression.

      math latex
        k^\ast = \min\left\{k \in \{0,1,2,\ldots\} \mid
        t_0 + k T_{\mathrm{eval}} - t_{\mathrm{last}} \ge T_{\mathrm{loss}}\right\},
        \qquad
        t_{\mathrm{detect}} = t_0 + k^\ast T_{\mathrm{eval}}

        T_{\mathrm{loss}} = 2.0\,\mathrm{s},
        \qquad
        0 < T_{\mathrm{eval}} \le 0.10\,\mathrm{s}
      end math

      where:
      t_0 is the first scheduled link-evaluation time after vehicle activation, in seconds.
      k is a nonnegative integer evaluation index.
      T_eval is the fixed interval between evaluations, in seconds.
      t_last is the monotonic receipt time of the most recent valid vehicle message, or the vehicle activation time if no valid vehicle message has yet been received, in seconds.
      T_loss is the configured link-loss tolerance, in seconds.
      t_detect is the time at which the link is declared unavailable, in seconds.
    rationale:
      The expression makes the threshold boundary and evaluation quantization explicit rather than hiding them in prose.
    source: SRC-SAFETY-001
    decomposes: OPS-004
    end requirement

### Ordinary diff behavior

A relationship-only retarget is visually direct:

    -decomposes: SYS-007
    +decomposes: SYS-009

A simple threshold edit changes the normative line containing that threshold. Splitting SYS-007 still produces a larger statement-line replacement because the original requirement is intentionally compound; that is semantic change rather than formatting noise.

### Strengths

- The record reads much like a labeled engineering note.
- Common short values do not require quoting.
- Explicit start and end markers make concatenated records inspectable.
- Repeated relationships have excellent diff locality.
- Only multiline content depends on indentation.

### Risks

- Indentation has structural meaning and therefore needs one simple, exact rule.
- A long statement often occupies one long physical line unless authors deliberately wrap it.
- Wrapped prose needs a defined folding rule: preserving newlines verbatim and treating them as ordinary whitespace are materially different choices.
- Mathematical payload gains structural indentation in raw source and needs defined de-indentation before rendering.

## Sketch 2 — explicit sigil directives

### Shape

Every structural line begins with `@`. Multiline bodies end explicitly and do not depend on indentation. Non-directive lines inside a body are content.

    @requirement SYS-007
    @title Loss-of-link response and record
    @allocation Mission-control coordinator
    @statement
    Within 250 ms after the command link is declared unavailable, the mission-control system shall cause the ground-control adapter to begin the first transmission attempt of a safe-recovery command for the active vehicle and shall record the link-loss declaration time, active vehicle identifier, and command identifier.
    @end statement
    @rationale
    Safe recovery must begin promptly, and the response must be reconstructable after the event.
    @end rationale
    @source SRC-SAFETY-001
    @decomposes OPS-001
    @decomposes OPS-004
    @end requirement

The mathematical portion would be:

    @statement
    While a vehicle is active, the mission-control system shall declare its command link unavailable at the first link-evaluation instant defined by the following mathematical expression.

    @math latex
    k^\ast = \min\left\{k \in \{0,1,2,\ldots\} \mid
    t_0 + k T_{\mathrm{eval}} - t_{\mathrm{last}} \ge T_{\mathrm{loss}}\right\},
    \qquad
    t_{\mathrm{detect}} = t_0 + k^\ast T_{\mathrm{eval}}

    T_{\mathrm{loss}} = 2.0\,\mathrm{s},
    \qquad
    0 < T_{\mathrm{eval}} \le 0.10\,\mathrm{s}
    @end math

    where:
    t_0 is the first scheduled link-evaluation time after vehicle activation, in seconds.
    k is a nonnegative integer evaluation index.
    T_eval is the fixed interval between evaluations, in seconds.
    t_last is the monotonic receipt time of the most recent valid vehicle message, or the vehicle activation time if no valid vehicle message has yet been received, in seconds.
    T_loss is the configured link-loss tolerance, in seconds.
    t_detect is the time at which the link is declared unavailable, in seconds.
    @end statement

### Ordinary diff behavior

The same retarget appears as:

    -@decomposes SYS-007
    +@decomposes SYS-009

Statement changes remain local. Adding a new multiline field requires two delimiter lines in addition to its content.

### Strengths

- Every structural token is unmistakable in raw text and ordinary search.
- Multiline prose and mathematics can begin at column zero and preserve their payload literally.
- Content may contain colons and arbitrary indentation without ambiguity.
- Parsing boundaries appear straightforward without a general host language.

### Risks

- Directive lines are visually prominent and compete with the requirement text.
- Ordinary fields use two different shapes: one-line directives and paired multiline directives.
- Literal content beginning with a reserved `@` directive at column zero needs an escaping or collision rule.
- Repeated `@end` lines add ceremony to common records.

## Sketch 3 — assignments with heredoc bodies

### Shape

A braced record contains assignments. Short values are quoted strings or reference tokens. Multiline prose uses a named heredoc terminator.

    requirement SYS-007 {
      title = "Loss-of-link response and record"
      allocation = "Mission-control coordinator"
      statement = <<STATEMENT
    Within 250 ms after the command link is declared unavailable, the mission-control system shall cause the ground-control adapter to begin the first transmission attempt of a safe-recovery command for the active vehicle and shall record the link-loss declaration time, active vehicle identifier, and command identifier.
    STATEMENT
      rationale = <<RATIONALE
    Safe recovery must begin promptly, and the response must be reconstructable after the event.
    RATIONALE
      source = SRC-SAFETY-001
      decomposes = OPS-001
      decomposes = OPS-004
    }

The mathematical content can remain literal inside the statement heredoc while using an embedded delimiter:

    statement = <<STATEMENT
    While a vehicle is active, the mission-control system shall declare its command link unavailable at the first link-evaluation instant defined by the following mathematical expression.

    math latex <<MATH
    k^\ast = \min\left\{k \in \{0,1,2,\ldots\} \mid
    t_0 + k T_{\mathrm{eval}} - t_{\mathrm{last}} \ge T_{\mathrm{loss}}\right\},
    \qquad
    t_{\mathrm{detect}} = t_0 + k^\ast T_{\mathrm{eval}}

    T_{\mathrm{loss}} = 2.0\,\mathrm{s},
    \qquad
    0 < T_{\mathrm{eval}} \le 0.10\,\mathrm{s}
    MATH

    where:
    t_0 is the first scheduled link-evaluation time after vehicle activation, in seconds.
    k is a nonnegative integer evaluation index.
    T_eval is the fixed interval between evaluations, in seconds.
    t_last is the monotonic receipt time of the most recent valid vehicle message, or the vehicle activation time if no valid vehicle message has yet been received, in seconds.
    T_loss is the configured link-loss tolerance, in seconds.
    t_detect is the time at which the link is declared unavailable, in seconds.
    STATEMENT

### Ordinary diff behavior

The same retarget appears as:

    -  decomposes = SYS-007
    +  decomposes = SYS-009

### Strengths

- Braces make record nesting visually familiar to programmers.
- Heredocs preserve multiline payload literally without escaping quotes or backslashes.
- Assignment syntax makes fields visibly machine-oriented and deterministic.
- Repeated relationship assignments retain line-local diffs.

### Risks

- Quoting, equals signs, braces, and terminator names make requirements resemble configuration or code.
- Authors must choose terminator tokens that do not occur alone in their content.
- The embedded math heredoc creates a second delimiter mechanism inside the statement heredoc.
- Closing braces and indentation add no engineering meaning.
- The syntax solves general scalar-literal problems that this fixed requirements model may not have.

## Comparative assessment

| Criterion | Sketch 1: indented fields | Sketch 2: sigil directives | Sketch 3: assignments/heredocs |
| --- | --- | --- | --- |
| Standalone reading | Strong; labels recede behind content | Clear but directive-heavy | Clear to programmers; configuration-like |
| Record boundaries | Explicit opener and closer | Explicit opener and closer | Explicit braces |
| Multiline boundaries | Indentation plus next column-zero field | Paired directives | Named terminators |
| Literal payload preservation | Requires defined de-indentation | Strong | Strong |
| Relationship diff | One plain labeled line | One directive line | One assignment line |
| Common-field ceremony | Low | Moderate | Moderate to high |
| Collision risk | Column-zero field-looking content | Reserved column-zero `@` lines | Heredoc terminator line |
| Additional concepts | Indentation rule | Directive and end-directive rules | Quotes, assignments, braces, heredocs |
| Likely manual authoring burden | Lowest | Moderate | Highest |

No sketch needs YAML scalar typing, TOML strings, Markdown block semantics, a configurable grammar, or generated syntax.

## Experimental selection

Sketch 1 is selected for Candidates A and B because it uses the fewest concepts and leaves requirement prose visually dominant. This selection is evidence to test, not a commitment to the final mundane-req language.

Sketch 2 remains the fallback if full-corpus use shows that literal column-zero payload and explicit boundaries are more valuable than visual quiet. Sketch 3 has no demonstrated advantage large enough to justify its extra quoting and delimiter machinery; it will not be carried into full-corpus encoding unless new evidence reveals one.

This selection is deliberately limited to the experiment. A few representative records and diffs were enough to choose among these three record sketches; encoding the full corpus in every sketch before making that narrow choice would add work without answering a new question. Full-corpus encoding in the actual representation Candidates A, B, and C remains part of the experiment.

The agreed experiment will test these deliberately small rules:

- use spaces, not tabs, for structural indentation;
- fold consecutive nonblank lines in ordinary prose into one semantic paragraph, so manual line wrapping does not change meaning;
- preserve blank lines as paragraph boundaries;
- remove only the required structural indentation from a `math latex` payload, preserving its remaining characters and line breaks;
- use `math latex` as the visible source label while defining the supported notation profile outside individual records.

These remain experiment hypotheses rather than final language decisions. The raw Git diff will still show prose reflow even though semantic interpretation folds it.

## Decisions proposed for the shared Candidate A/B grammar

The following choices appear supported across the sketches:

1. Use an explicit record opener containing the authoritative ID.
2. Use a fixed, lowercase field vocabulary.
3. Keep the title as durable content but not identity.
4. Use one outgoing `decomposes` relationship per line.
5. Keep external `source` distinct from requirement relationships.
6. Permit a record to carry both an external source and decomposition relationships, as the corpus's system requirements do.
7. Omit absent fields rather than writing `none` sentinels.
8. Do not encode editorial level, experiment notes, workflow state, or revision state.
9. Delimit mathematical notation visibly and name its content language.
10. Do not require quotes around ordinary prose in the preferred syntax.
11. Keep the grammar identical across module and one-requirement-per-file layouts.

## Record rules selected for encoding

1. Structural indentation uses spaces, not tabs.
2. Consecutive nonblank prose lines fold into a semantic paragraph; blank lines remain paragraph boundaries.
3. A `math latex` block is de-indented only by its required structural indentation. Its remaining characters and line breaks are preserved.
4. The visible label remains `math latex`; the supported notation profile belongs in the language definition rather than each record.

The separate view notation, Markdown-hosted Candidate C block form, and annotated baseline-tag contents remain subsequent decisions.
