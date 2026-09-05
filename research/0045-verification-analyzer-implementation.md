# Research 0045: Verification plan compiler and analyzer

Date: 2026-09-05

TC-0904 delivers two independent native tools: mundane-plan compiles the three
selected TSV tables, and mundane-verify analyzes published requirement/plan artifacts
through the bounded resolver. [Usage](../distribution/verification.md) runs the pilot
fixture without requiring requirement source parsing in either consumer.

The compiled plan matches the design fixture created before implementation. The
57-assertion baseline-B analysis reports only RDS-002 and SYS-009 review-stale, with
statement as their changed field and no uncovered requirements. Baseline reselection
produces current rows without implying approval. Header-only coverage exposes all
57 requirements as uncovered. Invalid references and incomplete imports prevent
analysis and never produce complete-looking counts.

`make verification-verify` passes native/JVM output agreement, 13 invalid TSV source
cases, retained row locations, CRLF input, unknown context/format, malformed/partial
inputs, comment/move/normative/unrelated/ID edit and rebuild cases, and real closed
stdout/broken pipes. JVM tests inject prefix/flush/stderr failures and changes to
plan files before final snapshot recheck. Source rows remain authoritative and are
never edited by these commands. Every result retains the authored assertion and
exact selected source/artifact provenance.

The analyzer compares only the seven documented requirement fields, canonicalizing
the decomposition set and ignoring permitted informational additions. It retains
coverage independently of staleness and exposes possible impact only through an
authored coverage assertion. Activity changes alter the plan revision without being
misrepresented as a changed requirement basis. No test execution, evidence storage,
safety reasoning, universal modeling language or satisfaction declaration is added.

Implementation refinement: linker/analyzer metadata explicitly records their own
independent tool/command versions in addition to input compiler provenance. The
contracts identify these informational fields; requirement source and output
meanings are unchanged. Version checking now covers all eight command identities.
The new native builds omit the YAML jar, and artifact-only Java compilation provides
an independence check. The three-command historical package retains its inventory.

Reproduction: Java 21/GraalVM CE 21.0.2, Linux x86_64, `make verification-verify
link-verify version-verify`. Full-gate and hosted verification follow in TC-1503.
