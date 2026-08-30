#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "usage: $0 VALIDATOR NEW_OUTPUT_DIRECTORY" >&2
    exit 2
fi

validator=$(readlink -f "$1")
output=$(readlink -m "$2")
root=$(git rev-parse --show-toplevel)
presenter="$root/experiments/0019-diagnostic-presentation/present.sh"
temporary=$(mktemp -d)
trap 'rm -rf "$temporary"' EXIT
invalid="$temporary/invalid"

if [[ -e $output ]]; then
    echo "output already exists: $output" >&2
    exit 2
fi
mkdir -p "$output" "$invalid"

for index in $(seq -w 0 1199); do
    printf 'content outside a record\n' > "$invalid/invalid-$index.mreq"
done

capture() {
    local destination=$1 status
    set +e
    "$validator" "$invalid" > "$temporary/stdout" 2> "$destination"
    status=$?
    set -e
    [[ $status -eq 1 && ! -s $temporary/stdout ]]
}

canonicalize() {
    local source=$1 destination=$2
    sed "s#$invalid#<invalid>#g" "$source" > "$temporary/normalized"
    awk -F ': ' '
        {
            split($1, location, ":")
            message=$3
            for (i=4; i<=NF; i++) message=message ": " $i
            print NR "\t" location[1] "\t" location[2] "\t" location[3] "\t" $2 "\t" message
        }
    ' "$temporary/normalized" > "$destination"
}

to_jsonl() {
    awk -F '\t' '
        function escape(value) {
            gsub(/\\/, "\\\\", value)
            gsub(/"/, "\\\"", value)
            gsub(/\r/, "\\r", value)
            gsub(/\n/, "\\n", value)
            gsub(/\t/, "\\t", value)
            return value
        }
        {
            printf "{\"sequence\":%d,\"path\":\"%s\",\"line\":%d,\"column\":%d,\"category\":\"%s\",\"message\":\"%s\"}\n", \
                $1, escape($2), $3, $4, escape($5), escape($6)
        }
    ' "$1" > "$2"
}

from_jsonl() {
    perl -MJSON::PP=decode_json -ne '
        $value=decode_json($_);
        die "unexpected JSON diagnostic fields\n" unless keys(%$value) == 6;
        print join("\t", @{$value}{qw(sequence path line column category message)}), "\n";
    ' "$1" > "$2"
}

capture "$temporary/full.stderr"
canonicalize "$temporary/full.stderr" "$temporary/full.tsv"
cp "$temporary/normalized" "$temporary/full.txt"
[[ $(wc -l < "$temporary/full.tsv") -eq 1200 ]]

limit=20
set +e
"$presenter" "$validator" "$limit" "$invalid" > "$temporary/bounded.stdout" 2> "$temporary/bounded.stderr"
bounded_status=$?
set -e
[[ $bounded_status -eq 1 && ! -s $temporary/bounded.stdout ]]
sed "s#$invalid#<invalid>#g" "$temporary/bounded.stderr" > "$output/bounded-text.txt"
[[ $(wc -l < "$output/bounded-text.txt") -eq 21 ]]
grep -Fqx 'mundanereq-validate: showing 20 of 1200 diagnostics; 1180 omitted; rerun without --max-diagnostics to show all.' \
    "$output/bounded-text.txt"

to_jsonl "$temporary/full.tsv" "$temporary/full.jsonl"
[[ $(wc -l < "$temporary/full.jsonl") -eq 1200 ]]
from_jsonl "$temporary/full.jsonl" "$temporary/from-jsonl.tsv"
cmp "$temporary/full.tsv" "$temporary/from-jsonl.tsv"
sed -n '1,3p' "$temporary/full.jsonl" > "$output/jsonl-sample.txt"
sed -n '1,3p' "$temporary/full.tsv" > "$output/canonical-sample.tsv"

sed -n "1,${limit}p" "$temporary/full.tsv" > "$temporary/expected-bounded.tsv"
sed -n '1,20p' "$output/bounded-text.txt" > "$temporary/bounded-only.txt"
canonicalize "$temporary/bounded-only.txt" "$temporary/parsed-bounded.tsv"
cmp "$temporary/expected-bounded.tsv" "$temporary/parsed-bounded.tsv"

set +e
"$presenter" "$validator" 0 "$invalid" > "$temporary/zero.stdout" 2> "$temporary/zero.stderr"
zero_status=$?
"$presenter" "$validator" 1200 "$invalid" > "$temporary/all.stdout" 2> "$temporary/all.stderr"
all_status=$?
"$presenter" "$validator" 2000 "$invalid" > "$temporary/over.stdout" 2> "$temporary/over.stderr"
over_status=$?
"$presenter" "$validator" -1 "$invalid" > /dev/null 2> "$temporary/negative.stderr"
negative_status=$?
"$presenter" "$validator" nope "$invalid" > /dev/null 2> "$temporary/nonnumeric.stderr"
nonnumeric_status=$?
"$presenter" "$validator" 20 "$temporary/missing" > /dev/null 2> "$temporary/operational.stderr"
operational_status=$?
set -e
[[ $zero_status -eq 1 && $all_status -eq 1 && $over_status -eq 1 ]]
[[ $negative_status -eq 2 && $nonnumeric_status -eq 2 && $operational_status -eq 2 ]]
grep -Fqx 'mundanereq-validate: showing 0 of 1200 diagnostics; 1200 omitted; rerun without --max-diagnostics to show all.' "$temporary/zero.stderr"
cmp "$temporary/full.stderr" "$temporary/all.stderr"
cmp "$temporary/full.stderr" "$temporary/over.stderr"
grep -Fq 'input-unavailable' "$temporary/operational.stderr"

printf '%s\n' \
    'requirement VALID-001' \
    'title: Valid source' \
    'statement:' \
    '  The source shall remain valid.' \
    'end requirement' > "$temporary/valid.mreq"
"$presenter" "$validator" 20 "$temporary/valid.mreq" > "$temporary/valid.stdout" 2> "$temporary/valid.stderr"
grep -Fqx 'Validated 1 requirements and 0 decomposition relationships from 1 file as mundanereq-source-0.2.' "$temporary/valid.stdout"
[[ ! -s $temporary/valid.stderr ]]

printf '%s\n' \
    'requirement REPAIRED-0000' \
    'title: Repaired first diagnostic' \
    'statement:' \
    '  The repaired source shall conform.' \
    'end requirement' > "$invalid/invalid-0000.mreq"
capture "$temporary/repaired.stderr"
canonicalize "$temporary/repaired.stderr" "$temporary/repaired.tsv"
[[ $(wc -l < "$temporary/repaired.tsv") -eq 1199 ]]
grep -Fq $'1\t<invalid>/invalid-0001.mreq\t1\t1\tcontent-outside-record' "$temporary/repaired.tsv"
tail -n +2 "$temporary/full.tsv" | cut -f 2-6 > "$temporary/expected-repaired.tsv"
cut -f 2-6 "$temporary/repaired.tsv" > "$temporary/actual-repaired.tsv"
cmp "$temporary/expected-repaired.tsv" "$temporary/actual-repaired.tsv"

mkdir -p "$temporary/archive" "$temporary/retrieved"
cp "$temporary/full.txt" "$temporary/archive/complete-diagnostics.txt"
tar -C "$temporary/archive" --sort=name --mtime='@0' --owner=0 --group=0 --numeric-owner \
    -cf - complete-diagnostics.txt | gzip -n > "$temporary/diagnostics-artifact.tar.gz"
tar -C "$temporary/retrieved" -xzf "$temporary/diagnostics-artifact.tar.gz"
cmp "$temporary/full.txt" "$temporary/retrieved/complete-diagnostics.txt"

{
    echo "initial_diagnostics=1200"
    echo "bounded_shown=20"
    echo "bounded_omitted=1180"
    echo "bounded_complete_recovery=rerun-without-limit"
    echo "structured_diagnostics=1200"
    echo "limit_zero_status=$zero_status"
    echo "limit_equal_total_status=$all_status"
    echo "limit_above_total_status=$over_status"
    echo "invalid_limit_status=$negative_status"
    echo "operational_failure_status=$operational_status"
    echo "after_one_local_repair=1199"
    echo "first_after_repair=invalid-0001.mreq:1:1"
    echo "default_text_behavior=unchanged-complete-stream"
    echo "source_validity_changed_by_presentation=no"
    echo "archive_retrieval=byte-identical"
} > "$output/workflow-summary.txt"

{
    sha256sum "$temporary/full.tsv" | sed 's#  .*#  complete-canonical-diagnostics.tsv#'
    sha256sum "$temporary/full.jsonl" | sed 's#  .*#  complete-diagnostics.jsonl#'
    sha256sum "$temporary/full.txt" | sed 's#  .*#  complete-diagnostics.txt#'
    sha256sum "$temporary/diagnostics-artifact.tar.gz" | sed 's#  .*#  diagnostics-artifact.tar.gz#'
    echo "complete_text_lines=$(wc -l < "$temporary/full.tsv")"
    echo "complete_jsonl_lines=$(wc -l < "$temporary/full.jsonl")"
    echo "complete_jsonl_bytes=$(wc -c < "$temporary/full.jsonl")"
} > "$output/complete-output-manifest.txt"

(
    cd "$output"
    sha256sum *.tsv *.txt > SHA256SUMS
)
echo "Diagnostic-presentation evidence written to $output"
