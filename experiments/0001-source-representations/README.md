# Experiment 0001: Source Representations

Status: Baseline A

## Purpose

This directory encodes the syntax-neutral corpus from [Research 0004](../../research/0004-uas-semantic-corpus.md) in the three representations selected by [Research 0003](../../research/0003-representation-prior-art.md).

- Candidate A stores several purpose-built requirement records in subject modules.
- Candidate B stores the exact same record grammar with one requirement per file.
- Candidate C embeds the same record form in a Markdown-authored specification.

Candidate A and Candidate B use the minimal view fixture sketched in [Research 0006](../../research/0006-non-markdown-view-notation.md). The fixture exists to test ordering and view-only movement; it is not a proposed view language specification.

## Current baseline state

The files currently represent Baseline A:

- 18 requirements;
- 21 outgoing decomposition relationships;
- one self-derived requirement;
- one requirement containing a LaTeX-style mathematical fragment.

Baseline A is identified by the annotated Git tag 'experiment-0001-baseline-a'. Its tag message states that the snapshot is illustrative and experimental and does not indicate requirements approval, certification, or engineering suitability.

The remaining experimental sequence is:

1. apply the exact semantic change defined in Research 0004;
2. inspect ordinary diffs and merge behavior;
3. correct issues discovered during review;
4. commit and tag the accepted Baseline B fixtures.

## Fixture conventions

The file extensions '.mreq' and '.mview' are provisional experiment labels, not language decisions.

Candidate C uses a standard fenced code block with the info string 'mundane-req'. The fenced body uses the selected Candidate A/B record grammar unchanged. This keeps the requirement boundary explicit and leaves the Markdown valid and readable in an ordinary renderer, while allowing surrounding Markdown prose to supply the authored view.

Candidate duplication is intentional. Within each candidate, each authoritative requirement appears exactly once.

No parser, renderer, generated index, or hidden metadata is part of this fixture.

## Initial construction observations

- Candidate A uses three requirement modules plus one view file.
- Candidate B uses 18 requirement files plus a byte-identical copy of the view file.
- Candidate C uses one Markdown file containing 18 fenced requirement records.
- All three candidates contain byte-identical record bodies, the same 21 decomposition relationships, and the same mathematical payload.
- The separate view is not needed to understand an individual requirement. Its only demonstrated purpose so far is to preserve authored grouping and order independently of record storage.
- Candidate C's standard fenced blocks remain readable in generic Markdown, but they render as code rather than polished requirement prose. Richer presentation would require an additive renderer or a different Markdown extension.
- No meaningful representation conclusion should be drawn before applying the Baseline B change and inspecting actual diffs, navigation, and merge behavior.
