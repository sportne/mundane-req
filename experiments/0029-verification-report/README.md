# Experiment 0029: Verification review report

This disposable static HTML view supports reviewing the pilot's retention change:
find stale planned assertions, inspect changed requirements, and navigate back to
source. It consumes published analyzer JSON; it imports no requirements parser.

From the repository root, with the documented Java 21/GraalVM toolchain:

```sh
make report-verify
```

Open `build/experiment-0029/report.html`. Its explicit relative source-link bases
assume that location. Local links open source files; `#L` fragments are useful on
Git forges but a local browser may not scroll plain text to that line. The visible
path and line remain usable. Without `--source-base SCOPE=BASE`, the renderer
prints locations without guessing source URLs. HTTP(S) bases are also accepted.

`run.py` archives the current committed inputs into a fresh temporary directory,
runs the installed native analyzer, renders, deletes outputs, and repeats. The
recorded input commit is `build/experiment-0029/input-revision.txt`; compiled input
and source-byte digests are embedded in the report. Analyzer and renderer are
installed tools outside that input archive. Their checked-in revisions and the
renderer golden are the reproduction recipe, not a claim of fresh tool builds.
The full `make verify` gate builds these tools before running the experiment.

The golden contains no clock time, machine-specific absolute path, or checkout
commit that would change on unrelated edits. It includes format/compiler
provenance and exact input digests. Ordering is derived by plan, activity, and
requirement ID. All 57 requirement records have anchors; the 262 internal/source
links are checked. Baseline A is the narrow retention-value projection documented
in Experiment 0028, not an invented reconstruction of historical source.

The report separates two review-stale assertions from planned coverage and shows
all built-in values, literal opaque LaTeX, activity objectives, source locations,
and selected provenance. Generation makes no execution or satisfaction claim.
Project-defined attributes do not yet exist and are not fabricated here.

The standard-library renderer rejects missing, unsupported and partial input;
checks row correspondence; escapes prose/math; and returns 2 for invalid input,
closed streams or broken output. This bounded consumer trusts the analyzer's
semantic calculation; it is not a second implementation of validation. A failed
output may contain a prefix: callers must check status before retaining a report.

[Decision and evidence](../../research/0046-verification-report-decision.md).
No human usability session was conducted. Authored view composition remains a
conditional question; this experiment establishes no need for a view language.
