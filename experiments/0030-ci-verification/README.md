# Experiment 0030: Complete CI verification

Reproduce from a clean checkout with the environment in
[the build guide](../../distribution/build-verification.md):

```sh
scripts/run-ci-verification.sh
```

The wrapper runs `make verify` and two deliberate failure-propagation checks.
See [Research 0047](../../research/0047-complete-ci-verification.md) for the actual
commit/run IDs, toolchain comparison, setup failure and measured outcome.

`results/local-*` captures a clean detached worktree with no shared build cache.
Terminal escape sequences and trailing whitespace are removed from saved logs;
commands, diagnostics, version output and status are retained. Inputs were restored
and `git status --short` was empty after the run. Original output under that worktree's `build/ci-evidence/` is disposable and is
not required for reproduction.

Hosted results are captured from the public GitHub Actions job log. Its original
log and run status are linked in the research record. Fixtures injected by the
failure check are never committed or pushed. An evidence branch runs the candidate
before its task card is marked complete; it publishes no release artifacts.
