#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "usage: $0 VALIDATOR NEW_OUTPUT_DIRECTORY" >&2
    exit 2
fi

validator=$(readlink -f "$1")
output=$(readlink -m "$2")
experiment=$(cd "$(dirname "$0")" && pwd)
root=$(cd "$experiment/../.." && pwd)
plain_a="$experiment/plain/baseline-a/requirements.mreq"
plain_b="$experiment/plain/baseline-b/requirements.mreq"
typo="$experiment/plain/typo/requirements.mreq"
vocabulary_a="$experiment/policy/vocabulary-a.tsv"
vocabulary_b="$experiment/policy/vocabulary-b.tsv"
requirements="$experiment/referenced/requirements.mreq"
targets_a="$experiment/referenced/baseline-a/targets.tsv"
targets_b="$experiment/referenced/baseline-b/targets.tsv"
allocations_a="$experiment/referenced/baseline-a/allocations.tsv"
allocations_b="$experiment/referenced/baseline-b/allocations.tsv"
temporary=$(mktemp -d)
trap 'rm -rf "$temporary"' EXIT

if [[ -e $output ]]; then
    echo "output already exists: $output" >&2
    exit 2
fi
mkdir -p "$output"

check_policy() {
    local source=$1 vocabulary=$2
    local display=$3
    awk -F '\t' -v source_display="$display" '
        FILENAME == ARGV[1] && FNR == 1 {
            vocabulary_header=1
            if ($0 != "label\ttarget_type") malformed=1
            next
        }
        FILENAME == ARGV[1] {
            if (NF != 2 || $1 == "" || $2 == "" || labels[$1]++) malformed=1
            next
        }
        FILENAME == ARGV[2] && /^allocation: / {
            label=substr($0, length("allocation: ") + 1)
            if (!(label in labels)) {
                print source_display ":" FNR ":1: allocation-policy: unknown allocation label " label > "/dev/stderr"
                failures++
            }
        }
        END {
            if (malformed || !vocabulary_header) exit 2
            exit failures ? 3 : 0
        }
    ' "$vocabulary" "$source"
}

check_referenced() {
    local targets=$1 allocations=$2
    local display=$3
    awk -F '\t' -v allocation_display="$display" '
        FILENAME == ARGV[1] && /^requirement / {
            split($0, fields, " "); requirements[fields[2]]=1; next
        }
        FILENAME == ARGV[2] && FNR == 1 {
            target_header=1
            if ($0 != "target_id\tdisplay_name\ttarget_type") malformed=1
            next
        }
        FILENAME == ARGV[2] {
            if (NF != 3 || $1 == "" || $2 == "" || $3 !~ /^(service|adapter|human-role)$/ || targets[$1]++) malformed=1
            next
        }
        FILENAME == ARGV[3] && FNR == 1 {
            allocation_header=1
            if ($0 != "requirement_id\ttarget_id\trole") malformed=1
            next
        }
        FILENAME == ARGV[3] {
            key=$1 SUBSEP $2 SUBSEP $3
            if (NF != 3 || $1 == "" || $2 == "" || $3 !~ /^(primary|supporting)$/ || facts[key]++) {
                malformed=1
                next
            }
            if (!($1 in requirements)) {
                print allocation_display ":" FNR ": allocation-companion: unresolved requirement " $1 > "/dev/stderr"
                references=1
            }
            if (!($2 in targets)) {
                print allocation_display ":" FNR ": allocation-companion: unresolved target " $2 > "/dev/stderr"
                references=1
            }
        }
        END {
            if (malformed || !target_header || !allocation_header) exit 2
            exit references ? 3 : 0
        }
    ' "$requirements" "$targets" "$allocations"
}

"$validator" "$plain_a" > "$output/plain-a-validation.txt"
"$validator" "$plain_b" > "$output/plain-b-validation.txt"
"$validator" "$requirements" > "$output/referenced-validation.txt"
"$validator" "$typo" > "$output/typo-language-validation.txt"
grep -Fqx 'Validated 4 requirements and 0 decomposition relationships from 1 file as mundanereq-source-0.2.' "$output/plain-a-validation.txt"
grep -Fqx 'Validated 4 requirements and 0 decomposition relationships from 1 file as mundanereq-source-0.2.' "$output/plain-b-validation.txt"
grep -Fqx 'Validated 4 requirements and 0 decomposition relationships from 1 file as mundanereq-source-0.2.' "$output/referenced-validation.txt"
grep -Fqx 'Validated 1 requirements and 0 decomposition relationships from 1 file as mundanereq-source-0.2.' "$output/typo-language-validation.txt"

check_policy "$plain_a" "$vocabulary_a" "plain/baseline-a/requirements.mreq"
check_policy "$plain_b" "$vocabulary_b" "plain/baseline-b/requirements.mreq"
set +e
check_policy "$typo" "$vocabulary_b" "plain/typo/requirements.mreq" 2> "$output/policy-diagnostic.txt"
policy_status=$?
set -e
[[ $policy_status -eq 3 ]]
grep -Fq 'allocation-policy: unknown allocation label Authorisation service' "$output/policy-diagnostic.txt"

check_referenced "$targets_a" "$allocations_a" "referenced/baseline-a/allocations.tsv"
check_referenced "$targets_b" "$allocations_b" "referenced/baseline-b/allocations.tsv"

printf 'wrong-header\n' > "$temporary/malformed-vocabulary.tsv"
set +e
check_policy "$plain_a" "$temporary/malformed-vocabulary.tsv" "plain/baseline-a/requirements.mreq" 2> /dev/null
malformed_policy_status=$?
set -e
[[ $malformed_policy_status -eq 2 ]]

: > "$temporary/empty-targets.tsv"
: > "$temporary/empty-allocations.tsv"
set +e
check_referenced "$temporary/empty-targets.tsv" "$temporary/empty-allocations.tsv" "mutated/allocations.tsv"
empty_companion_status=$?
set -e
[[ $empty_companion_status -eq 2 ]]

awk -F '\t' 'BEGIN { OFS="\t" } NR == 4 { $2="TGT-MISSING" } { print }' \
    "$allocations_b" > "$temporary/dangling-allocations.tsv"
set +e
check_referenced "$targets_b" "$temporary/dangling-allocations.tsv" "mutated/allocations.tsv" \
    2> "$output/companion-diagnostic.txt"
dangling_status=$?
set -e
[[ $dangling_status -eq 3 ]]
grep -Fqx 'mutated/allocations.tsv:4: allocation-companion: unresolved target TGT-MISSING' \
    "$output/companion-diagnostic.txt"
grep -v '^allocation: ' "$plain_a" > "$temporary/plain-a-without-allocation.mreq"
grep -v '^allocation: ' "$plain_b" > "$temporary/plain-b-without-allocation.mreq"
cmp "$requirements" "$temporary/plain-a-without-allocation.mreq"
cmp "$requirements" "$temporary/plain-b-without-allocation.mreq"

diff -u --label plain/baseline-a/requirements.mreq --label plain/baseline-b/requirements.mreq \
    "$plain_a" "$plain_b" > "$output/plain-label.diff" || [[ $? -eq 1 ]]
diff -u --label referenced/baseline-a/targets.tsv --label referenced/baseline-b/targets.tsv \
    "$targets_a" "$targets_b" > "$output/target-rename.diff" || [[ $? -eq 1 ]]
diff -u --label referenced/baseline-a/allocations.tsv --label referenced/baseline-b/allocations.tsv \
    "$allocations_a" "$allocations_b" > "$output/reallocation.diff" || [[ $? -eq 1 ]]
diff -u --label policy/vocabulary-a.tsv --label policy/vocabulary-b.tsv \
    "$vocabulary_a" "$vocabulary_b" > "$output/vocabulary-evolution.diff" || [[ $? -eq 1 ]]
for diff_file in "$output"/*.diff; do
    sed -i 's/[[:space:]]\+$//' "$diff_file"
done

awk -F '\t' '
    FILENAME == ARGV[1] && FNR > 1 { names[$1]=$2; types[$1]=$3; next }
    FILENAME == ARGV[2] && FNR > 1 && $1 == "SYS-012" {
        print $1 "\t" $3 "\t" $2 "\t" names[$2] "\t" types[$2]
    }
    ' "$targets_b" "$allocations_b" > "$output/multi-target-query.txt"
[[ $(wc -l < "$output/multi-target-query.txt") -eq 2 ]]
grep -Fqx $'SYS-012\tprimary\tTGT-GCA\tGround-control adapter\tadapter' "$output/multi-target-query.txt"
grep -Fqx $'SYS-012\tsupporting\tTGT-LINK\tLink monitor\tservice' "$output/multi-target-query.txt"

{
    echo "prior_reallocation_commit=$(git -C "$root" rev-parse 0ee3a83)"
    git -C "$root" show -s --format='prior_reallocation_subject=%s' 0ee3a83
    git -C "$root" show --format= -U0 0ee3a83 -- '*.mreq' | grep -E '^[+-]allocation:'
} > "$output/prior-workflow.txt"

plain_rename_edits=$(grep -Ec '^[+-]allocation: (Vehicle manager|Vehicle registry service)$' "$output/plain-label.diff")
plain_reallocation_edits=$(grep -Ec '^[+-]allocation: (Flight-plan manager|Authorization service)$' "$output/plain-label.diff")
target_rename_edits=$(grep -Ec '^[+-]TGT-VEHICLE' "$output/target-rename.diff")
referenced_reallocation_edits=$(grep -Ec '^[+-]SYS-004' "$output/reallocation.diff")
[[ $plain_rename_edits -eq 4 && $plain_reallocation_edits -eq 2 ]]
[[ $target_rename_edits -eq 2 && $referenced_reallocation_edits -eq 2 ]]
{
    echo "language_accepts_unknown_allocation_label=yes"
    echo "policy_rejects_typo=yes"
    echo "plain_rename_requirement_lines=$plain_rename_edits"
    echo "identified_rename_target_lines=$target_rename_edits"
    echo "plain_reallocation_requirement_lines=$plain_reallocation_edits"
    echo "identified_reallocation_assertion_lines=$referenced_reallocation_edits"
    echo "plain_multi_target_support=not-representable-with-one-allocation-field"
    echo "identified_multi_target_assertions=2"
    echo "general_system_model_introduced=no"
} > "$output/model-summary.txt"

(
    cd "$output"
    sha256sum *.diff *.txt > SHA256SUMS
)
echo "Allocation-model evidence written to $output"
