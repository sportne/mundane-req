#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "usage: $0 NEW_PACKAGE_DIRECTORY" >&2
    exit 2
fi

destination=$(readlink -m "$1")
root=$(git rev-parse --show-toplevel)
experiment="$root/experiments/0015-independent-conformance"
manifest=$(mktemp)
trap 'rm -f "$manifest"' EXIT

if [[ -e $destination ]]; then
    echo "destination already exists: $destination" >&2
    exit 2
fi

mkdir -p \
    "$destination/fixtures/0.1/valid" "$destination/fixtures/0.1/invalid" \
    "$destination/fixtures/0.2/valid" "$destination/fixtures/0.2/invalid" \
    "$destination/result"
cp "$root/specification/0005-mundanereq-source-language-0.2.md" "$destination/standard.md"
cp "$experiment/participant-task.md" "$destination/participant-task.md"
cp -R "$root/conformance/0.1/invalid/." "$destination/fixtures/0.1/invalid/"
cp "$root"/conformance/0.1/valid/*.mreq "$destination/fixtures/0.1/valid/"
cp -R "$root/conformance/0.2/invalid/." "$destination/fixtures/0.2/invalid/"
cp "$root"/conformance/0.2/valid/*.mreq "$destination/fixtures/0.2/valid/"

(
    cd "$destination"
    find fixtures -type f -print0 | LC_ALL=C sort -z | xargs -0 sha256sum
    sha256sum participant-task.md standard.md
) > "$manifest"
cmp "$manifest" "$experiment/input-SHA256SUMS"

echo "Independent package prepared at $destination"
