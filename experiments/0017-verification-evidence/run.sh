#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "usage: $0 VALIDATOR NEW_OUTPUT_DIRECTORY" >&2
    exit 2
fi

validator=$(readlink -f "$1")
output=$(readlink -m "$2")
root=$(git rev-parse --show-toplevel)
experiment="$root/experiments/0017-verification-evidence"
requirements_a="$experiment/scenario/baseline-a/requirements.mreq"
requirements_b="$experiment/scenario/baseline-b/requirements.mreq"
activities="$experiment/companion/activities.tsv"
plan_a="$experiment/companion/baseline-a/plan.tsv"
plan_b="$experiment/companion/baseline-b/plan.tsv"
coverage_a="$experiment/companion/baseline-a/coverage.tsv"
coverage_b="$experiment/companion/baseline-b/coverage.tsv"
executions="$experiment/companion/executions.tsv"
evidence="$experiment/companion/evidence.tsv"
results="$experiment/companion/results.tsv"

if [[ -e $output ]]; then
    echo "output already exists: $output" >&2
    exit 2
fi
mkdir -p "$output"

source_digest() {
    sha256sum "$1" | awk '{ print $1 }'
}

check_binding() {
    local plan=$1 requirements=$2 expected_baseline=$3
    local actual_requirements actual_activities
    actual_requirements=$(source_digest "$requirements")
    actual_activities=$(source_digest "$activities")
    awk -F '\t' -v baseline="$expected_baseline" -v requirements="$actual_requirements" \
        -v activities="$actual_activities" '
        NR == 1 {
            if ($0 != "plan_id\trequirement_baseline\trequirement_source_sha256\tactivity_source_sha256") status=2
            next
        }
        NR == 2 {
            if (NF != 4 || $1 == "" || $2 != baseline || $3 != requirements || $4 != activities) status=3
            else found=1
        }
        END { if ((!found && !status) || NR != 2) status=2; exit status }
    ' "$plan"
}

check_coverage() {
    local requirements=$1 coverage=$2 expected_plan=$3
    awk -F '\t' -v expected_plan="$expected_plan" '
        FILENAME == ARGV[1] && /^requirement / { split($0, fields, " "); requirements[fields[2]]=1; next }
        FILENAME == ARGV[2] && FNR == 1 {
            if ($0 != "activity_id\tmethod\tobjective\texpected_evidence") exit 2
            next
        }
        FILENAME == ARGV[2] {
            if (NF != 4 || $1 == "" || $2 == "" || $3 == "" || $4 == "") exit 3
            activities[$1]=1
            next
        }
        FILENAME == ARGV[3] && FNR == 1 {
            if ($0 != "plan_id\tactivity_id\trequirement_id") exit 2
            next
        }
        FILENAME == ARGV[3] {
            key=$1 SUBSEP $2 SUBSEP $3
            if (NF != 3 || $1 == "" || $2 == "" || $3 == "" || $1 != expected_plan \
                    || !($2 in activities) || !($3 in requirements) || seen[key]++) exit 3
        }
    ' "$requirements" "$activities" "$coverage"
}

render_coverage() {
    local requirements=$1 coverage=$2 destination=$3
    awk -F '\t' '
        FILENAME == ARGV[1] && /^requirement / { split($0, fields, " "); order[++count]=fields[2]; next }
        FILENAME == ARGV[2] && FNR > 1 {
            links[$3]=links[$3] (links[$3] == "" ? "" : ",") $2
        }
        END {
            for (i=1; i<=count; i++) {
                id=order[i]
                print id "\t" (links[id] == "" ? "uncovered" : links[id])
            }
        }
    ' "$requirements" "$coverage" > "$destination"
}

"$validator" "$requirements_a" > "$output/validate-a.txt"
"$validator" "$requirements_b" > "$output/validate-b.txt"
grep -Fqx 'Validated 3 requirements and 2 decomposition relationships from 1 file as mundanereq-source-0.2.' "$output/validate-a.txt"
grep -Fqx 'Validated 4 requirements and 3 decomposition relationships from 1 file as mundanereq-source-0.2.' "$output/validate-b.txt"

check_binding "$plan_a" "$requirements_a" baseline-a
check_binding "$plan_b" "$requirements_b" baseline-b
check_coverage "$requirements_a" "$coverage_a" PLAN-A
check_coverage "$requirements_b" "$coverage_b" PLAN-B
render_coverage "$requirements_a" "$coverage_a" "$output/coverage-a.txt"
render_coverage "$requirements_b" "$coverage_b" "$output/coverage-b.txt"
grep -Fqx $'DATA-ARCHIVE-001\tuncovered' "$output/coverage-a.txt"
grep -Fqx $'SENSOR-WEATHER-001\tuncovered' "$output/coverage-b.txt"
planned_a=$(grep -Fvc $'\tuncovered' "$output/coverage-a.txt")
uncovered_a=$(grep -Fc $'\tuncovered' "$output/coverage-a.txt")
planned_b=$(grep -Fvc $'\tuncovered' "$output/coverage-b.txt")
uncovered_b=$(grep -Fc $'\tuncovered' "$output/coverage-b.txt")
[[ $planned_a -eq 2 && $uncovered_a -eq 1 && $planned_b -eq 3 && $uncovered_b -eq 1 ]]

set +e
check_binding "$plan_a" "$requirements_b" baseline-b
stale_status=$?
set -e
[[ $stale_status -eq 3 ]]
{
    echo "plan=PLAN-A"
    echo "declared_baseline=baseline-a"
    echo "evaluated_baseline=baseline-b"
    echo "status=stale-requirement-baseline"
    echo "declared_sha256=$(awk -F '\t' 'NR == 2 { print $3 }' "$plan_a")"
    echo "actual_sha256=$(source_digest "$requirements_b")"
} > "$output/stale-plan.txt"

awk -F '\t' '
    (FILENAME == ARGV[1] || FILENAME == ARGV[2]) && FNR == 1 {
        if ($0 != "plan_id\trequirement_baseline\trequirement_source_sha256\tactivity_source_sha256") exit 2
        next
    }
    (FILENAME == ARGV[1] || FILENAME == ARGV[2]) && FNR > 1 {
        if (NF != 4 || $1 == "" || $2 == "" || $3 == "" || $4 == "" || plans[$1]++) exit 3
        next
    }
    FILENAME == ARGV[3] && FNR == 1 {
        if ($0 != "activity_id\tmethod\tobjective\texpected_evidence") exit 2
        next
    }
    FILENAME == ARGV[3] && FNR > 1 {
        if (NF != 4 || $1 == "" || $2 == "" || $3 == "" || $4 == "" || activities[$1]++) exit 3
        next
    }
    (FILENAME == ARGV[4] || FILENAME == ARGV[5]) && FNR == 1 {
        if ($0 != "plan_id\tactivity_id\trequirement_id") exit 2
        next
    }
    (FILENAME == ARGV[4] || FILENAME == ARGV[5]) && FNR > 1 {
        if (NF != 3 || $1 == "" || $2 == "" || $3 == "" || !($1 in plans) || !($2 in activities)) exit 3
        planned[$1 SUBSEP $2]=1
        next
    }
    FILENAME == ARGV[6] && FNR == 1 {
        if ($0 != "execution_id\tactivity_id\tplan_id\tconfiguration_ref\texecuted_at") exit 2
        next
    }
    FILENAME == ARGV[6] && FNR > 1 {
        if (NF != 5 || $1 == "" || $2 == "" || $3 == "" || $4 == "" || $5 == "" \
                || executions[$1]++ || !(($3 SUBSEP $2) in planned)) exit 3
        next
    }
    FILENAME == ARGV[7] && FNR == 1 {
        if ($0 != "execution_id\tevidence_locator\tintegrity") exit 2
        next
    }
    FILENAME == ARGV[7] && FNR > 1 {
        if (NF != 3 || $1 == "" || $2 == "" || !($1 in executions) || evidence[$1]++ \
                || $3 !~ /^sha256:[0-9a-f]{64}$/) exit 3
        next
    }
    FILENAME == ARGV[8] && FNR == 1 {
        if ($0 != "execution_id\toutcome\tbasis") exit 2
        next
    }
    FILENAME == ARGV[8] && FNR > 1 {
        if (NF != 3 || $1 == "" || !($1 in executions) || results[$1]++ \
                || $2 !~ /^(pass|fail|inconclusive)$/ || $3 == "") exit 3
        if ($2 == "pass" && !($1 in evidence)) exit 3
    }
    END { if (length(executions) != 2 || length(evidence) != 1 || length(results) != 1) exit 3 }
    ' "$plan_a" "$plan_b" "$activities" "$coverage_a" "$coverage_b" "$executions" "$evidence" "$results"

awk -F '\t' '
    FILENAME == ARGV[1] && FNR > 1 { execution[$1]=$2; plan[$1]=$3; next }
    FILENAME == ARGV[2] && FNR > 1 { evidence[$1]="yes"; next }
    FILENAME == ARGV[3] && FNR > 1 { result[$1]=$2; next }
    END {
        for (id in execution) {
            outcome=(result[id] == "" ? "none" : result[id])
            print id "\tactivity=" execution[id] "\tplan=" plan[id] "\tplanned=yes\texecuted=yes\tevidence=" \
                (evidence[id] == "yes" ? "yes" : "no") "\tresult=" outcome "\tpassing=" \
                (outcome == "pass" ? "yes" : "no")
        }
    }
    ' "$executions" "$evidence" "$results" | sort > "$output/execution-states.txt"

awk -F '\t' '
    FILENAME == ARGV[1] && FNR > 1 { planned[$1 SUBSEP $2]=1; next }
    FILENAME == ARGV[2] && FNR > 1 { executed[$3 SUBSEP $2]=1; next }
    END {
        for (key in planned) if (!(key in executed)) {
            split(key, fields, SUBSEP)
            print fields[1] "\tactivity=" fields[2] "\tplanned=yes\texecuted=no"
        }
    }
    ' "$coverage_a" "$executions" | sort > "$output/planned-not-executed.txt"
grep -Fqx $'PLAN-A\tactivity=ACT-LEVEL-ANALYSIS\tplanned=yes\texecuted=no' "$output/planned-not-executed.txt"

execution_count=$(awk 'END { print NR - 1 }' "$executions")
evidenced_count=$(awk 'END { print NR - 1 }' "$evidence")
passing_count=$(awk -F '\t' 'FNR > 1 && $2 == "pass" { count++ } END { print count + 0 }' "$results")
[[ $execution_count -eq 2 && $evidenced_count -eq 1 && $passing_count -eq 1 ]]

diff -u --label baseline-a/requirements.mreq --label baseline-b/requirements.mreq \
    "$requirements_a" "$requirements_b" > "$output/requirements.diff" || [[ $? -eq 1 ]]
diff -u --label baseline-a/plan.tsv --label baseline-b/plan.tsv \
    "$plan_a" "$plan_b" > "$output/plan.diff" || [[ $? -eq 1 ]]
diff -u --label baseline-a/coverage.tsv --label baseline-b/coverage.tsv \
    "$coverage_a" "$coverage_b" > "$output/coverage.diff" || [[ $? -eq 1 ]]

{
    echo "requirement_source_rewritten_by_execution=no"
    echo "activity_source_reused_between_plans=yes"
    echo "activity_source_sha256=$(source_digest "$activities")"
    echo "baseline_a_planned=$planned_a"
    echo "baseline_a_uncovered=$uncovered_a"
    echo "baseline_b_planned=$planned_b"
    echo "baseline_b_uncovered=$uncovered_b"
    echo "executions=$execution_count"
    echo "evidenced_executions=$evidenced_count"
    echo "passing_executions=$passing_count"
    echo "passing_requirements=not-derived-by-this-experiment"
} > "$output/model-summary.txt"

(
    cd "$output"
    sha256sum *.diff *.txt > SHA256SUMS
)
echo "Verification-model evidence written to $output"
