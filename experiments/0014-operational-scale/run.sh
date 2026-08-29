#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 4 ]]; then
    echo "usage: $0 FORMATTER VALIDATOR TRACE OUTPUT_DIRECTORY" >&2
    exit 2
fi

formatter=$(readlink -f "$1")
validator=$(readlink -f "$2")
trace=$(readlink -f "$3")
output=$(readlink -m "$4")
root=$(git rev-parse --show-toplevel)
subject="$root/experiments/0011-operational-corpus/requirements"
classes="$root/build/maintained/classes"
temporary=$(mktemp -d)
trap 'rm -rf "$temporary"' EXIT

if [[ ! -d $classes ]]; then
    echo "maintained test classes are missing; run make test first" >&2
    exit 2
fi
if ! command -v native-image >/dev/null; then
    echo "native-image is missing from PATH" >&2
    exit 2
fi

mkdir -p "$output" "$temporary/one-record" "$temporary/invalid"

for source in "$subject"/*.mreq; do
    awk -v destination="$temporary/one-record" '
        /^requirement / { target = destination "/" $2 ".mreq" }
        target != "" { print > target }
        /^end requirement$/ { close(target); target = "" }
    ' "$source"
done

for index in $(seq -w 0 1199); do
    printf 'content outside a record\n' > "$temporary/invalid/invalid-$index.mreq"
done

"$validator" "$subject" > "$output/subject-validation.txt"
"$validator" "$temporary/one-record" > "$output/one-record-validation.txt"
grep -Fqx 'Validated 60 requirements and 54 decomposition relationships from 6 files as mundanereq-source-0.2.' \
    "$output/subject-validation.txt"
grep -Fqx 'Validated 60 requirements and 54 decomposition relationships from 60 files as mundanereq-source-0.2.' \
    "$output/one-record-validation.txt"
java -cp "$classes" mundanereq.InventoryMain "$subject" > "$temporary/subject.inventory"
java -cp "$classes" mundanereq.InventoryMain "$temporary/one-record" > "$temporary/one-record.inventory"
cmp "$temporary/subject.inventory" "$temporary/one-record.inventory"
sha256sum "$temporary/subject.inventory" | sed 's#  .*#  normalized-semantic-inventory#' \
    > "$output/semantic-inventory.sha256"

printf 'label,phase,run,status,elapsed_us,max_rss_kib,stdout_bytes,stderr_lines\n' > "$output/raw-measurements.csv"

coproc MONOTONIC_TIMER {
    perl -MTime::HiRes=clock_gettime,CLOCK_MONOTONIC -e '
        $| = 1;
        while (<STDIN>) {
            printf "%.0f\n", clock_gettime(CLOCK_MONOTONIC) * 1_000_000_000;
        }
    '
}
timer_read=${MONOTONIC_TIMER[0]}
timer_write=${MONOTONIC_TIMER[1]}

timestamp() {
    local destination=$1 value
    printf 'tick\n' >&"$timer_write"
    IFS= read -r value <&"$timer_read"
    printf -v "$destination" '%s' "$value"
}

measure() {
    local label=$1 expected=$2 repetitions=$3
    shift 3
    local run phase status started finished memory stdout stderr
    for run in $(seq 0 "$repetitions"); do
        phase=repeated
        if [[ $run -eq 0 ]]; then phase=first; fi
        stdout="$temporary/$label.stdout"
        stderr="$temporary/$label.stderr"
        memory="$temporary/$label.memory"
        timestamp started
        set +e
        /usr/bin/time -q -f '%M' -o "$memory" "$@" > "$stdout" 2> "$stderr"
        status=$?
        set -e
        timestamp finished
        elapsed_us=$(((finished - started) / 1000))
        if [[ $elapsed_us -le 0 ]]; then
            echo "$label: nonpositive monotonic duration $elapsed_us" >&2
            exit 1
        fi
        if [[ $status -ne $expected ]]; then
            echo "$label: expected status $expected, got $status" >&2
            exit 1
        fi
        if [[ $run -eq 0 ]]; then
            cp "$stdout" "$output/$label.stdout.txt"
            cp "$stderr" "$output/$label.stderr.txt"
        fi
        printf '%s,%s,%d,%d,%d,%s,%s,%s\n' \
            "$label" "$phase" "$run" "$status" "$elapsed_us" \
            "$(<"$memory")" "$(wc -c < "$stdout")" "$(wc -l < "$stderr")" \
            >> "$output/raw-measurements.csv"
    done
}

measure format-subject 0 20 "$formatter" --check "$subject"
measure format-one-record 0 20 "$formatter" --check "$temporary/one-record"
measure validate-subject 0 20 "$validator" "$subject"
measure validate-one-record 0 20 "$validator" "$temporary/one-record"
measure trace-deep-subject 0 20 "$trace" higher SENSOR-LEVEL-001 "$subject"
measure trace-deep-one-record 0 20 "$trace" higher SENSOR-LEVEL-001 "$temporary/one-record"
measure trace-branch-subject 0 20 "$trace" impact OPS-MONITOR-001 "$subject"
measure trace-branch-one-record 0 20 "$trace" impact OPS-MONITOR-001 "$temporary/one-record"
measure validate-diagnostics 1 5 "$validator" "$temporary/invalid"

cmp "$output/trace-deep-subject.stdout.txt" "$output/trace-deep-one-record.stdout.txt"
cmp "$output/trace-branch-subject.stdout.txt" "$output/trace-branch-one-record.stdout.txt"
sed "s#$temporary#<temporary>#g" "$output/validate-diagnostics.stderr.txt" \
    | sed -n '1,10p' > "$output/diagnostic-sample.txt"
{
    echo "lines=$(wc -l < "$output/validate-diagnostics.stderr.txt")"
    echo "bytes=$(wc -c < "$output/validate-diagnostics.stderr.txt")"
    echo "diagnostic_codes:"
    sed -n 's/.*: \([^:]*\): .*/\1/p' "$output/validate-diagnostics.stderr.txt" \
        | LC_ALL=C sort | uniq -c
} > "$output/diagnostic-shape.txt"

{
    echo "git_commit=$(git -C "$root" rev-parse HEAD)"
    echo "git_version=$(git --version)"
    echo "kernel=$(uname -srmo)"
    echo "glibc=$(getconf GNU_LIBC_VERSION)"
    echo "cpu=$(lscpu | sed -n 's/^Model name:[[:space:]]*//p')"
    echo "logical_cpus=$(getconf _NPROCESSORS_ONLN)"
    echo "memory_kib=$(awk '/MemTotal:/ { print $2 }' /proc/meminfo)"
    echo "formatter_version=$($formatter --version)"
    echo "validator_version=$($validator --version)"
    echo "trace_version=$($trace --version)"
    native-image --version 2>&1 | sed 's/^/native_image=/'
    sha256sum "$formatter" "$validator" "$trace"
} > "$output/environment.txt"

{
    echo "label,phase,runs,min_elapsed_us,mean_elapsed_us,max_elapsed_us,mean_rss_kib,max_rss_kib"
    awk -F, 'NR > 1 {
        key=$1 "," $2
        count[key]++
        elapsed[key]+=$5
        rss[key]+=$6
        if (!(key in min) || $5 < min[key]) min[key]=$5
        if ($5 > max[key]) max[key]=$5
        if ($6 > rssmax[key]) rssmax[key]=$6
    }
    END {
        for (key in count) printf "%s,%d,%d,%d,%d,%d,%d\n", key, count[key], min[key], elapsed[key]/count[key], max[key], rss[key]/count[key], rssmax[key]
    }' "$output/raw-measurements.csv" | LC_ALL=C sort
} > "$output/summary.csv"

(
    cd "$output"
    sha256sum \
        diagnostic-sample.txt diagnostic-shape.txt environment.txt \
        one-record-validation.txt raw-measurements.csv semantic-inventory.sha256 \
        subject-validation.txt summary.csv \
        trace-branch-one-record.stdout.txt trace-branch-subject.stdout.txt \
        trace-deep-one-record.stdout.txt trace-deep-subject.stdout.txt
) > "$output/SHA256SUMS"

echo "Operational-scale evidence written to $output"
