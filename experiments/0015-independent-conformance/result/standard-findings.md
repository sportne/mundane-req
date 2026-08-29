# Standard findings and interpretation choices

1. **The `0.1` fixture path does not select a 0.1 parsing mode.** Only the 0.2 standard is supplied, source files have no embedded version directive (Clause 15.1), and the task asks for interpretation from that standard. I therefore assessed every selection in strict 0.2 terms. This is consistent with Clause 15.1's compatibility statement. The directory name itself has no source-language semantics.

2. **A duplicate identifier has no traversal-independent “second” occurrence.** Clause 11.1 makes the two occurrences symmetrically invalid, while Clauses 5.1 and 7.2 make file traversal order nonsemantic. The standard requires a diagnostic location but does not prescribe which duplicate occurrence to anchor. For the TSV's single location, I chose the occurrence in lexicographically later path `two.mreq` at its identifier start; this is only a deterministic reporting choice and does not assign semantic order.

3. **Construct-level failure coordinates are not uniquely prescribed.** Clause 12.1 requires a line and column but does not standardize which token represents a missing field, an unterminated construct, or a file-wide absence. I anchored a missing statement and an unterminated math block at the encountered record closer, and the comment-only file at its sole source line. Other conforming diagnostics could choose an EOF or related construct location without changing any verdict.

4. **Specific rules overlap summary validity and diagnostic clauses.** The same defect can be described by a syntax/interpretation clause, Clause 10's collected file-validity conditions, and Clause 12's diagnostic coverage. I interpreted “first decisive standard clause” in the requested TSV as the most specific substantive rule that makes the source nonconforming, not a later summary or diagnostic-capability clause.

No other ambiguity, internal inconsistency, fixture uncertainty, or interpretation choice affected these conclusions.
