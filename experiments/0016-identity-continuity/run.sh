#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 4 ]]; then
    echo "usage: $0 VALIDATOR TRACE REQIF_PROBE NEW_OUTPUT_DIRECTORY" >&2
    exit 2
fi

validator=$(readlink -f "$1")
trace=$(readlink -f "$2")
reqif_probe=$(readlink -f "$3")
output=$(readlink -m "$4")
root=$(git rev-parse --show-toplevel)
experiment="$root/experiments/0016-identity-continuity"
baseline_a="$experiment/scenario/baseline-a"
baseline_b="$experiment/scenario/baseline-b"
continuity_a="$experiment/candidates/continuity-assertion/baseline-a/identity-continuity.tsv"
continuity_b="$experiment/candidates/continuity-assertion/baseline-b/identity-continuity.tsv"
bindings_a="$experiment/candidates/durable-identity/baseline-a/identity-bindings.tsv"
bindings_b="$experiment/candidates/durable-identity/baseline-b/identity-bindings.tsv"

if [[ -e $output ]]; then
    echo "output already exists: $output" >&2
    exit 2
fi
mkdir -p "$output"

hex() {
    printf '%s' "$1" | od -An -tx1 | tr -d ' \n' | tr '[:lower:]' '[:upper:]'
}

binding_for_human() {
    awk -F '\t' -v human="$2" '$2 == human { count++; value=$1 } END { if (count == 1) print value; else exit 1 }' "$1"
}

resolve_continuity() {
    local file=$1 source_baseline=$2 source_file=$3 target_baseline=$4 target_file=$5 predecessor=$6
    local source_sha256 target_sha256
    source_sha256=$(sha256sum "$source_file" | awk '{ print $1 }')
    target_sha256=$(sha256sum "$target_file" | awk '{ print $1 }')
    awk -F '\t' -v predecessor="$predecessor" -v source="$source_baseline" -v source_hash="$source_sha256" \
        -v target="$target_baseline" -v target_hash="$target_sha256" '
        NR == 1 {
            if ($0 != "predecessor_id\tsuccessor_id\tdecision\tsource_baseline\tsource_sha256\ttarget_baseline\ttarget_sha256\tauthority_role\tbasis") exit 2
            next
        }
        $1 == predecessor {
            count++
            if ($3 != "same-requirement" || $4 != source || $5 != source_hash || $6 != target \
                    || $7 != target_hash || $8 != "requirements-change-authority" || $9 == "") exit 2
            successor=$2
        }
        END { if (count != 1) exit 2; print successor }
    ' "$file"
}

transform_to_durable() {
    local source=$1 destination=$2 bindings=$3 requirements=$4
    cp "$source" "$destination"
    sed -i 's/mundanereq-reqif-profile-0.1/mundanereq-reqif-durable-identity-candidate-0.1/g' "$destination"

    while IFS=$'\t' read -r child parent; do
        child_durable=$(binding_for_human "$bindings" "$child")
        parent_durable=$(binding_for_human "$bindings" "$parent")
        sed -i "s/MR_REL_$(hex "$child")_$(hex "$parent")/MR_REL_$(hex "$child_durable")_$(hex "$parent_durable")/g" "$destination"
    done < <(awk '/^requirement / { child=$2 } /^decomposes: / { print child "\t" $2 }' "$requirements")

    while IFS=$'\t' read -r durable human; do
        [[ $durable == durable_id ]] && continue
        sed -i "s/MR_REQ_$(hex "$human")/MR_REQ_$(hex "$durable")/g" "$destination"
    done < "$bindings"
}

transform_from_durable() {
    local source=$1 destination=$2 bindings=$3 requirements=$4
    cp "$source" "$destination"

    while IFS=$'\t' read -r child parent; do
        child_durable=$(binding_for_human "$bindings" "$child")
        parent_durable=$(binding_for_human "$bindings" "$parent")
        sed -i "s/MR_REL_$(hex "$child_durable")_$(hex "$parent_durable")/MR_REL_$(hex "$child")_$(hex "$parent")/g" "$destination"
    done < <(awk '/^requirement / { child=$2 } /^decomposes: / { print child "\t" $2 }' "$requirements")

    while IFS=$'\t' read -r durable human; do
        [[ $durable == durable_id ]] && continue
        sed -i "s/MR_REQ_$(hex "$durable")/MR_REQ_$(hex "$human")/g" "$destination"
    done < "$bindings"
    sed -i 's/mundanereq-reqif-durable-identity-candidate-0.1/mundanereq-reqif-profile-0.1/g' "$destination"
}

extract_transport_ids() {
    grep -o 'IDENTIFIER="MR_\(REQ\|REL\)_[^"]*"' "$1" | sed 's/^IDENTIFIER="//; s/"$//' | sort
}

"$validator" "$baseline_a/requirements.mreq" > "$output/validate-a.txt"
"$validator" "$baseline_b/requirements.mreq" > "$output/validate-b.txt"
grep -Fqx 'Validated 3 requirements and 2 decomposition relationships from 1 file as mundanereq-source-0.2.' "$output/validate-a.txt"
grep -Fqx 'Validated 3 requirements and 2 decomposition relationships from 1 file as mundanereq-source-0.2.' "$output/validate-b.txt"

"$trace" children SYS-MONITOR-SMAPLE-001 "$baseline_a/requirements.mreq" > "$output/trace-a.txt"
"$trace" children SYS-MONITOR-SAMPLE-001 "$baseline_b/requirements.mreq" > "$output/trace-b.txt"
sed 's/SYS-MONITOR-SMAPLE-001/SYS-MONITOR-SAMPLE-001/g' "$output/trace-a.txt" | cmp - "$output/trace-b.txt"

set +e
"$trace" children SYS-MONITOR-SMAPLE-001 "$baseline_b/requirements.mreq" > /dev/null 2> "$output/stale-trace.stderr.txt"
stale_status=$?
set -e
[[ $stale_status -eq 2 ]]
grep -Fq 'missing-requirement' "$output/stale-trace.stderr.txt"

grep -Fqx $'VERIFY-SAMPLE-001\tSYS-MONITOR-SMAPLE-001\tDemonstrate complete periodic station sampling.' "$baseline_a/verification-plan.tsv"
grep -Fqx $'VERIFY-SAMPLE-001\tSYS-MONITOR-SAMPLE-001\tDemonstrate complete periodic station sampling.' "$baseline_b/verification-plan.tsv"

diff -u --label baseline-a/requirements.mreq --label baseline-b/requirements.mreq \
    "$baseline_a/requirements.mreq" "$baseline_b/requirements.mreq" > "$output/requirements.diff" || [[ $? -eq 1 ]]
diff -u --label baseline-a/verification-plan.tsv --label baseline-b/verification-plan.tsv \
    "$baseline_a/verification-plan.tsv" "$baseline_b/verification-plan.tsv" > "$output/verification-plan.diff" || [[ $? -eq 1 ]]
diff -u --label baseline-a/identity-continuity.tsv --label baseline-b/identity-continuity.tsv \
    "$continuity_a" "$continuity_b" > "$output/continuity-assertion.diff" || [[ $? -eq 1 ]]
diff -u --label baseline-a/identity-bindings.tsv --label baseline-b/identity-bindings.tsv \
    "$bindings_a" "$bindings_b" > "$output/durable-identity.diff" || [[ $? -eq 1 ]]

old_id=$(awk -F '\t' 'NR == 2 { print $2 }' "$experiment/scenario/external-supplier-reference.tsv")
successor=$(resolve_continuity "$continuity_b" baseline-a "$baseline_a/requirements.mreq" \
    baseline-b "$baseline_b/requirements.mreq" "$old_id")
[[ $successor == SYS-MONITOR-SAMPLE-001 ]]
if resolve_continuity "$experiment/candidates/continuity-assertion/invalid-conflict.tsv" baseline-a \
        "$baseline_a/requirements.mreq" baseline-b "$baseline_b/requirements.mreq" "$old_id" > /dev/null; then
    echo "conflicting continuity assertion was accepted" >&2
    exit 1
fi
if resolve_continuity "$experiment/candidates/continuity-assertion/invalid-wrong-baseline.tsv" baseline-a \
        "$baseline_a/requirements.mreq" baseline-b "$baseline_b/requirements.mreq" "$old_id" > /dev/null; then
    echo "inapplicable continuity assertion was accepted" >&2
    exit 1
fi
if resolve_continuity "$experiment/candidates/continuity-assertion/invalid-unauthorized.tsv" baseline-a \
        "$baseline_a/requirements.mreq" baseline-b "$baseline_b/requirements.mreq" "$old_id" > /dev/null; then
    echo "unauthorized continuity assertion was accepted" >&2
    exit 1
fi
if resolve_continuity "$experiment/candidates/continuity-assertion/invalid-wrong-content.tsv" baseline-a \
        "$baseline_a/requirements.mreq" baseline-b "$baseline_b/requirements.mreq" "$old_id" > /dev/null; then
    echo "content-stale continuity assertion was accepted" >&2
    exit 1
fi

durable_consumer=$(awk -F '\t' 'NR == 2 { print $2 }' "$experiment/candidates/durable-identity/supplier-reference.tsv")
durable_human_a=$(awk -F '\t' -v durable="$durable_consumer" '$1 == durable { count++; value=$2 } END { if (count == 1) print value; else exit 1 }' "$bindings_a")
durable_human_b=$(awk -F '\t' -v durable="$durable_consumer" '$1 == durable { count++; value=$2 } END { if (count == 1) print value; else exit 1 }' "$bindings_b")
[[ $durable_human_a == "$old_id" ]]
[[ $durable_human_b == SYS-MONITOR-SAMPLE-001 ]]

timestamp=2026-08-29T12:00:00Z
"$reqif_probe" export "$timestamp" "$output/git-only-a.reqif" "$baseline_a/requirements.mreq" \
    | sed "s|$output/git-only-a.reqif|OUTPUT/git-only-a.reqif|" > "$output/reqif-export-a.txt"
"$reqif_probe" export "$timestamp" "$output/git-only-b.reqif" "$baseline_b/requirements.mreq" \
    | sed "s|$output/git-only-b.reqif|OUTPUT/git-only-b.reqif|" > "$output/reqif-export-b.txt"
"$reqif_probe" import-inventory "$output/git-only-a.reqif" > "$output/reqif-import-a.txt"
"$reqif_probe" import-inventory "$output/git-only-b.reqif" > "$output/reqif-import-b.txt"
extract_transport_ids "$output/git-only-a.reqif" > "$output/git-only-a-transport.txt"
extract_transport_ids "$output/git-only-b.reqif" > "$output/git-only-b-transport.txt"
diff -u --label baseline-a/transport-ids --label baseline-b/transport-ids \
    "$output/git-only-a-transport.txt" "$output/git-only-b-transport.txt" > "$output/git-only-reqif.diff" || [[ $? -eq 1 ]]

transform_to_durable "$output/git-only-a.reqif" "$output/durable-a.reqif" "$bindings_a" "$baseline_a/requirements.mreq"
transform_to_durable "$output/git-only-b.reqif" "$output/durable-b.reqif" "$bindings_b" "$baseline_b/requirements.mreq"
extract_transport_ids "$output/durable-a.reqif" > "$output/durable-a-transport.txt"
extract_transport_ids "$output/durable-b.reqif" > "$output/durable-b-transport.txt"
cmp "$output/durable-a-transport.txt" "$output/durable-b-transport.txt"
diff -u --label baseline-a/transport-ids --label baseline-b/transport-ids \
    "$output/durable-a-transport.txt" "$output/durable-b-transport.txt" > "$output/durable-reqif.diff" || true

transform_from_durable "$output/durable-a.reqif" "$output/durable-a-reversed.reqif" "$bindings_a" "$baseline_a/requirements.mreq"
transform_from_durable "$output/durable-b.reqif" "$output/durable-b-reversed.reqif" "$bindings_b" "$baseline_b/requirements.mreq"
"$reqif_probe" import-inventory "$output/durable-a-reversed.reqif" > "$output/durable-import-a.txt"
"$reqif_probe" import-inventory "$output/durable-b-reversed.reqif" > "$output/durable-import-b.txt"
cmp "$output/reqif-import-a.txt" "$output/durable-import-a.txt"
cmp "$output/reqif-import-b.txt" "$output/durable-import-b.txt"

baseline_a_commit=$(git rev-parse 'experiment-0004-baseline-a^{}')
baseline_b_commit=$(git rev-parse 'experiment-0004-baseline-b^{}')
{
    echo "prior_history_scenario=Experiment 0004 LPC_KIAS_0 correction"
    echo "baseline_a_commit=$baseline_a_commit"
    echo "baseline_b_commit=$baseline_b_commit"
    git show -s --format='baseline_b_subject=%s' 'experiment-0004-baseline-b^{}'
    echo "atomic_change_lines:"
    git show --format= --no-ext-diff -U0 'experiment-0004-baseline-b^{}' -- \
        experiments/0004-transferability/requirements experiments/0004-transferability/verification-plan.md \
        | grep -E '^[+-].*(LPC_KIAS(_0|_NONNEGATIVE)|Applies to requirements baseline|Baseline-[AB] coverage result)' \
        | grep -Ev '^(---|\+\+\+)'
} > "$output/git-history.txt"

{
    echo "stale_human_id=$old_id"
    echo "git_only_resolution=unresolved-in-baseline-b"
    echo "stale_trace_stdout_bytes=0"
    echo "continuity_resolution=$successor"
    echo "continuity_conflict=rejected"
    echo "continuity_wrong_baseline=rejected"
    echo "continuity_unauthorized_role=rejected"
    echo "continuity_wrong_content_digest=rejected"
    echo "durable_id=$durable_consumer"
    echo "durable_resolution=$durable_human_b"
    echo "durable_identity_resolves_stale_human_id=no"
    echo "durable_identity_resolves_preexchanged_durable_reference=yes"
} > "$output/consumer-resolution.txt"

rm "$output"/*.reqif
(
    cd "$output"
    sha256sum *.diff *.txt > SHA256SUMS
)

echo "Identity-continuity evidence written to $output"
