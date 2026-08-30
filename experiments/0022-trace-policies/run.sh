#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 3 ]]; then
    echo "usage: $0 VALIDATOR TRACE NEW_OUTPUT_DIRECTORY" >&2
    exit 2
fi

validator=$(readlink -f "$1")
trace=$(readlink -f "$2")
output=$(readlink -m "$3")
experiment=$(cd "$(dirname "$0")" && pwd)
root=$(cd "$experiment/../.." && pwd)
pass="$experiment/source/pass/requirements.mreq"
coverage_fail="$experiment/source/coverage-fail/requirements.mreq"
cycle_fail="$experiment/source/cycle-fail/requirements.mreq"
downward_scope="$experiment/policy/downward-scope.tsv"
cycle_scope="$experiment/policy/cycle-scope.tsv"
temporary=$(mktemp -d)
trap 'rm -rf "$temporary"' EXIT

if [[ -e $output ]]; then
    echo "output already exists: $output" >&2
    exit 2
fi
mkdir -p "$output"

check_downward() {
    local source=$1 scope=$2 display=$3
    awk -F '\t' -v display="$display" '
        FILENAME == ARGV[1] {
            if (/^requirement /) { split($0, f, " "); current=f[2]; requirements[current]=1 }
            if (/^decomposes: /) { target=substr($0, length("decomposes: ") + 1); incoming[target]++ }
            next
        }
        FILENAME == ARGV[2] && FNR == 1 {
            header=1
            if ($0 != "requirement_id\tdecision\trationale") malformed=1
            next
        }
        FILENAME == ARGV[2] {
            if (NF != 3 || $1 == "" || $2 !~ /^(required|waived)$/ || $3 == "" || scoped[$1]++) { malformed=1; next }
            ids[++count]=$1
            decision[$1]=$2
            next
        }
        END {
            for (i=1; i<=count; i++) {
                id=ids[i]
                if (!(id in requirements)) { print display ": policy/downward-coverage: scoped requirement is absent: " id > "/dev/stderr"; failures++ }
                else if (decision[id] == "required" && !incoming[id]) {
                    print display ": policy/downward-coverage: required incoming decomposition is absent: " id > "/dev/stderr"; failures++
                }
            }
            if (malformed || !header) exit 2
            exit failures ? 3 : 0
        }
    ' "$source" "$scope"
}

check_acyclic() {
    local source=$1 scope=$2 display=$3
    awk -F '\t' -v display="$display" '
        FILENAME == ARGV[1] {
            if (/^requirement /) { split($0, f, " "); current=f[2]; requirements[current]=1 }
            if (/^decomposes: /) { target=substr($0, length("decomposes: ") + 1); edge[current SUBSEP target]=1 }
            next
        }
        FILENAME == ARGV[2] && FNR == 1 { header=1; if ($0 != "requirement_id") malformed=1; next }
        FILENAME == ARGV[2] { if (NF != 1 || $1 == "" || scoped[$1]++) malformed=1; else { ids[++count]=$1; in_scope[$1]=1 }; next }
        END {
            for (id in in_scope) if (!(id in requirements)) {
                print display ": policy/decomposition-cycle: scoped requirement is absent: " id > "/dev/stderr"; failures=1
            }
            for (id in requirements) if (!(id in in_scope)) {
                print display ": policy/decomposition-cycle: source requirement is absent from complete scope: " id > "/dev/stderr"; failures=1
            }
            for (pair in edge) { split(pair, e, SUBSEP); if (in_scope[e[1]] && in_scope[e[2]]) reach[e[1] SUBSEP e[2]]=1 }
            for (k=1; k<=count; k++) for (i=1; i<=count; i++) for (j=1; j<=count; j++)
                if (reach[ids[i] SUBSEP ids[k]] && reach[ids[k] SUBSEP ids[j]]) reach[ids[i] SUBSEP ids[j]]=1
            message=""
            for (i=1; i<=count; i++) if (reach[ids[i] SUBSEP ids[i]]) message=message (message == "" ? "" : " ") ids[i]
            if (message != "") { print display ": policy/decomposition-cycle: cycle members: " message > "/dev/stderr"; failures=1 }
            if (malformed || !header) exit 2
            exit failures ? 3 : 0
        }
    ' "$source" "$scope"
}

for candidate in pass coverage-fail cycle-fail; do
    "$validator" "$experiment/source/$candidate/requirements.mreq" > "$output/validate-$candidate.txt"
    case $candidate in
        pass) expected=3 ;;
        coverage-fail) expected=2 ;;
        cycle-fail) expected=4 ;;
    esac
    grep -Fqx "Validated 6 requirements and $expected decomposition relationships from 1 file as mundanereq-source-0.2." "$output/validate-$candidate.txt"
done

check_downward "$pass" "$downward_scope" "source/pass/requirements.mreq"
check_acyclic "$pass" "$cycle_scope" "source/pass/requirements.mreq"

set +e
check_downward "$coverage_fail" "$downward_scope" "source/coverage-fail/requirements.mreq" 2> "$output/downward-policy-diagnostic.txt"
downward_status=$?
check_acyclic "$cycle_fail" "$cycle_scope" "source/cycle-fail/requirements.mreq" 2> "$output/cycle-policy-diagnostic.txt"
cycle_status=$?
set -e
[[ $downward_status -eq 3 && $cycle_status -eq 3 ]]
grep -Fqx 'source/coverage-fail/requirements.mreq: policy/downward-coverage: required incoming decomposition is absent: SYS-009' "$output/downward-policy-diagnostic.txt"
grep -Fqx 'source/cycle-fail/requirements.mreq: policy/decomposition-cycle: cycle members: GCA-001 SYS-005' "$output/cycle-policy-diagnostic.txt"

sed 's/^SYS-005$/SYS-OO5/' "$cycle_scope" > "$temporary/typo-cycle-scope.tsv"
set +e
check_acyclic "$cycle_fail" "$temporary/typo-cycle-scope.tsv" "mutated/cycle-scope.tsv" 2> "$output/cycle-scope-diagnostic.txt"
scope_status=$?
set -e
[[ $scope_status -eq 3 ]]
grep -Fq 'scoped requirement is absent: SYS-OO5' "$output/cycle-scope-diagnostic.txt"
grep -Fq 'source requirement is absent from complete scope: SYS-005' "$output/cycle-scope-diagnostic.txt"

"$trace" higher GCA-001 "$cycle_fail" > "$output/trace-cycle-observation.txt"
grep -Fq 'Cycle observed among: GCA-001 SYS-005' "$output/trace-cycle-observation.txt"

fret_a="$root/experiments/0004-transferability/requirements/state-and-invariants.mreq"
fret_b="$root/experiments/0004-transferability/requirements/transitions.mreq"
awk '{ print }' "$fret_a" "$fret_b" > "$temporary/fret.mreq"
{
    printf 'requirement_id\tdecision\trationale\n'
    awk '/^requirement / { print $2 "\trequired\tMisapplied blanket downward-coverage policy." }' "$temporary/fret.mreq"
} > "$temporary/fret-scope.tsv"
set +e
check_downward "$temporary/fret.mreq" "$temporary/fret-scope.tsv" "FRET-LPC-mini" 2> "$output/misapplied-policy-diagnostics.txt"
fret_policy_status=$?
set -e
[[ $fret_policy_status -eq 3 ]]
fret_inapplicable_violations=$(wc -l < "$output/misapplied-policy-diagnostics.txt")
[[ $fret_inapplicable_violations -eq 19 ]]

{
    echo 'source_language_status_pass=0'
    echo 'source_language_status_coverage_fail=0'
    echo 'source_language_status_cycle_fail=0'
    echo 'downward_policy_status_pass=0'
    echo "downward_policy_status_fail=$downward_status"
    echo 'cycle_policy_status_pass=0'
    echo "cycle_policy_status_fail=$cycle_status"
    echo 'trace_query_cycle_status=0'
    echo "misapplied_fret_true_violations=$fret_inapplicable_violations"
    echo 'general_rule_language_introduced=no'
} > "$output/status-matrix.txt"

(cd "$output" && sha256sum *.txt > SHA256SUMS)
echo "Trace-policy evidence written to $output"
