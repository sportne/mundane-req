# Diagnostic recovery and incomplete interpretation

Status: Experimental implementation contract. Applies to explicitly selected
custom 0.2 and requirements YAML 0.3, including preserved 0.1-compatible fixtures.
No valid-source syntax or semantic rule changes.

Custom source retains successfully parsed records before and after independently
malformed records. Recovery searches for a valid unindented `requirement ID`
opener preceded by a blank or comment line. It never treats indented prose or math
payload as a record. Missing record terminators can synchronize at such a boundary.
An unclosed math block makes later boundaries ambiguous: recovery stops and retains
only the already parsed prefix. A malformed candidate without reliable separation
is skipped. Recovery reports one primary error per attempted malformed record;
after 99 primary errors it emits `recovery-limit` and stops (100 diagnostics per
file). Source bytes/lines are traversed with forward progress; this does not impose
a new maximum custom source-file size or promise a source-set-wide error cap.

YAML document syntax, encoding, forbidden profile constructs and invalid top-level
envelopes are file-fatal. Once a valid envelope and sequence have been composed,
independently invalid requirement mappings do not discard valid neighboring
mappings. The existing YAML byte/depth/record/diagnostic limits remain in force.
No textual resynchronization of malformed YAML is attempted.

`Interpreter.Result.syntaxComplete()` is false if any selected source could not
be fully decoded and interpreted. Recovered requirements/origins are diagnostic
context and must not be treated as a complete source model. Duplicate-ID checks
can still report duplicates actually observed. Absent-target checks are suppressed
across the selected source set when syntax is incomplete, because absence cannot
be established. This may postpone a real missing-target error until primary
errors are repaired. With complete syntax, relationship validation is unchanged;
a semantic error can make `valid()` false while `syntaxComplete()` is true.

Strict validation still returns nonconformance. Formatter write-back is blocked
for the entire selected invalid source set. Compiled requirements retain
`complete:false`, primary diagnostics, and an empty `requirements` array; recovered
records are not published for linking. Existing valid-input semantic inventories,
formatter output, default human CLI mode and source selection are unchanged.
Diagnostic ordering remains path, line, column, rule, message. Existing rule IDs
keep their meanings; `recovery-limit` is an additive rule in the
[compiled catalog](0013-compiled-diagnostic-rules.md).
