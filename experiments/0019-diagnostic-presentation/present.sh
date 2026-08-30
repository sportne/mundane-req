#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 3 || ! $2 =~ ^[0-9]+$ ]]; then
    echo "usage: $0 VALIDATOR MAX_DIAGNOSTICS FILE_OR_DIRECTORY..." >&2
    exit 2
fi

validator=$(readlink -f "$1")
limit=$2
shift 2
temporary=$(mktemp -d)
trap 'rm -rf "$temporary"' EXIT

set +e
"$validator" "$@" > "$temporary/stdout" 2> "$temporary/stderr"
status=$?
set -e

if [[ $status -ne 1 ]]; then
    sed -n '1,$p' "$temporary/stdout"
    sed -n '1,$p' "$temporary/stderr" >&2
    exit "$status"
fi

total=$(wc -l < "$temporary/stderr")
shown=$limit
if [[ $shown -gt $total ]]; then shown=$total; fi
if [[ $shown -gt 0 ]]; then sed -n "1,${shown}p" "$temporary/stderr" >&2; fi
omitted=$((total - shown))
if [[ $omitted -gt 0 ]]; then
    echo "mundanereq-validate: showing $shown of $total diagnostics; $omitted omitted; rerun without --max-diagnostics to show all." >&2
fi
exit 1
