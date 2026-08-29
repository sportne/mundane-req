# Independent-Author Task Sheet

You are evaluating the supplied requirements source and three command-line
tools. Use only the supplied source-language standard and tool documents. Work
in an ordinary Git branch and record every command, question, failed attempt,
and source edit. Do not optimize for speed; make your reasoning inspectable.

1. Read the corpus and report the requirement that sets the operator-notification time bound, the bound, and its direct higher-level requirement.
2. Add `EDGE-ALERT-QUEUE-001`, requiring the station processor to retain a detected local hazard until a gateway acknowledges it, as a decomposition of `SYS-ALERT-NOTIFY-001`.
3. Change operational-record retention from 30 days to 90 days. Inspect trace impact and update other requirements only when their authored statements would otherwise contradict the change.
4. Move `EDGE-STORE-001` to a different source file without changing its requirement semantics.
5. Introduce a temporary dangling `decomposes` target, run the appropriate tool, use its diagnostic to repair the source, and retain only the repaired source in the final commit.
6. Run formatter check or write, validate the complete source set, and answer which component requirements trace to `OPS-RECORD-001`.
7. Review the ordinary Git diff. State which lines are normative changes, which are a non-semantic move, and whether any tool-only output is needed to understand the proposed source change.
8. Commit the completed change on your branch. Do not create or edit a database, index, manifest, generated report, or source-language configuration file.
