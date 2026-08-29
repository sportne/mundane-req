#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "usage: $0 VALIDATOR NEW_OUTPUT_DIRECTORY" >&2
    exit 2
fi

validator=$(readlink -f "$1")
output=$(readlink -m "$2")
root=$(git rev-parse --show-toplevel)
experiment="$root/experiments/0018-safety-classification"
requirements="$experiment/scenario/requirements.mreq"
scheme="$experiment/scheme/wmscs-r1.tsv"
external_a="$experiment/candidates/external-assessment/baseline-a/assessments.tsv"
external_b="$experiment/candidates/external-assessment/baseline-b/assessments.tsv"
inline_a="$experiment/candidates/inline/baseline-a/requirements.mreq-candidate"
inline_b="$experiment/candidates/inline/baseline-b/requirements.mreq-candidate"
hazard_a="$experiment/scenario/hazard-analysis-a.tsv"
hazard_b="$experiment/scenario/hazard-analysis-b.tsv"

if [[ -e $output ]]; then
    echo "output already exists: $output" >&2
    exit 2
fi
mkdir -p "$output"

requirements_sha256=$(sha256sum "$requirements" | awk '{ print $1 }')
scheme_sha256=$(sha256sum "$scheme" | awk '{ print $1 }')

validate_assessments() {
    local assessments=$1
    awk -F '\t' -v digest="$requirements_sha256" -v scheme_digest="$scheme_sha256" -v scheme_file="$scheme" '
        FILENAME == scheme_file && FNR == 1 {
            if ($0 != "scheme_id\tlevel\tmeaning") exit 2
            next
        }
        FILENAME == scheme_file {
            if (NF != 3 || $1 == "" || $2 == "" || $3 == "" || levels[$1 SUBSEP $2]++) exit 3
            next
        }
        FILENAME == ARGV[2] && /^requirement / {
            split($0, fields, " ")
            requirements[fields[2]]=1
            next
        }
        FILENAME == ARGV[3] && FNR == 1 {
            if ($0 != "assertion_id\trequirement_id\trequirement_sha256\tscheme_id\tscheme_sha256\tcontext\tlevel\trationale\tsource\tassessor_role") exit 2
            next
        }
        FILENAME == ARGV[3] {
            key=$2 SUBSEP $4 SUBSEP $6
            if (NF != 10 || $1 == "" || $2 == "" || $3 != digest || $4 == "" || $5 != scheme_digest \
                    || $6 == "" || $7 == "" || $8 == "" || $9 == "" \
                    || $10 != "project-safety-authority" || assertions[$1]++ || facts[key]++ \
                    || !($2 in requirements) || !(($4 SUBSEP $7) in levels)) exit 3
            count++
        }
        END { if (count != 4) exit 3 }
    ' "$scheme" "$requirements" "$assessments"
}

normalize_inline() {
    awk '
        /^requirement / { requirement=$2; next }
        /^safety-assessment: / {
            value=substr($0, length("safety-assessment: ") + 1)
            count=split(value, fields, " \\| ")
            if (count != 8) exit 3
            print fields[1] "\t" requirement "\t" fields[2] "\t" fields[3] "\t" fields[4] \
                "\t" fields[5] "\t" fields[8] "\t" fields[7] "\t" fields[6]
        }
    ' "$1"
}

"$validator" "$requirements" > "$output/validate.txt"
grep -Fqx 'Validated 3 requirements and 2 decomposition relationships from 1 file as mundanereq-source-0.2.' "$output/validate.txt"
grep -Fqx $'WMSCS-r1\tSC0\tNo credited role in a declared project hazard control.' "$scheme"
grep -Fqx $'WMSCS-r1\tSC1\tSupports detection or mitigation of a declared project hazard.' "$scheme"
grep -Fqx $'WMSCS-r1\tSC2\tRequired control whose loss contributes directly to a declared hazardous outcome.' "$scheme"
validate_assessments "$external_a"
validate_assessments "$external_b"

grep -Fqx $'analysis_ref\thazard_id\tcontext\trequirement_id\tcontrol_credit' "$hazard_a"
grep -Fqx $'analysis_ref\thazard_id\tcontext\trequirement_id\tcontrol_credit' "$hazard_b"
[[ $(wc -l < "$hazard_a") -eq 2 && $(wc -l < "$hazard_b") -eq 2 ]]
IFS=$'\t' read -r analysis_a hazard_id_a context_a requirement_a credit_a < <(sed -n '2p' "$hazard_a")
IFS=$'\t' read -r analysis_b hazard_id_b context_b requirement_b credit_b < <(sed -n '2p' "$hazard_b")
source_a=$(awk -F '\t' '$1 == "SA-002" { print $9 }' "$external_a")
source_b=$(awk -F '\t' '$1 == "SA-002" { print $9 }' "$external_b")
level_a=$(awk -F '\t' '$1 == "SA-002" { print $7 }' "$external_a")
level_b=$(awk -F '\t' '$1 == "SA-002" { print $7 }' "$external_b")
rationale_a=$(awk -F '\t' '$1 == "SA-002" { print $8 }' "$external_a")
rationale_b=$(awk -F '\t' '$1 == "SA-002" { print $8 }' "$external_b")
[[ $analysis_a#$hazard_id_a == "$source_a" && $analysis_b#$hazard_id_b == "$source_b" ]]
[[ $context_a == potable-water-control && $context_b == "$context_a" ]]
[[ $requirement_a == SYS-ALARM-001 && $requirement_b == "$requirement_a" ]]
[[ $credit_a == supporting-control && $level_a == SC1 ]]
[[ $credit_b == required-control && $level_b == SC2 ]]
[[ $source_a != "$source_b" && $rationale_a != "$rationale_b" ]]

normalize_inline "$inline_a" > "$output/inline-a-facts.txt"
normalize_inline "$inline_b" > "$output/inline-b-facts.txt"
cut -f 1,2,4,5,6,7,8,9,10 "$external_a" | tail -n +2 > "$output/external-a-facts.txt"
cut -f 1,2,4,5,6,7,8,9,10 "$external_b" | tail -n +2 > "$output/external-b-facts.txt"
cmp "$output/inline-a-facts.txt" "$output/external-a-facts.txt"
cmp "$output/inline-b-facts.txt" "$output/external-b-facts.txt"

grep -v '^safety-assessment: ' "$inline_a" > "$output/inline-a-requirements.mreq"
grep -v '^safety-assessment: ' "$inline_b" > "$output/inline-b-requirements.mreq"
cmp "$requirements" "$output/inline-a-requirements.mreq"
cmp "$requirements" "$output/inline-b-requirements.mreq"

diff -u --label baseline-a/requirements.mreq-candidate --label baseline-b/requirements.mreq-candidate \
    "$inline_a" "$inline_b" > "$output/inline.diff" || [[ $? -eq 1 ]]
diff -u --label baseline-a/assessments.tsv --label baseline-b/assessments.tsv \
    "$external_a" "$external_b" > "$output/external-assessment.diff" || [[ $? -eq 1 ]]
diff -u --label baseline-a/requirements.mreq --label baseline-b/requirements.mreq \
    "$requirements" "$requirements" > "$output/requirements.diff" || true

awk -F '\t' 'FNR > 1 {
    print $2 "\tcriticality[" $4 "/" $6 "]=" $7 "\tderived-from=" $1 "\tsource=" $9
}' "$external_b" | sort > "$output/derived-inline-b.txt"
awk -F '\t' 'FNR > 1 && $7 == "SC2" { print $2 "\t" $4 "\t" $6 "\t" $7 "\t" $1 }' \
    "$external_b" > "$output/query-sc2.txt"
awk -F '\t' 'FNR > 1 && $2 == "SYS-ALARM-001" { print $6 "\t" $7 "\t" $1 "\t" $9 }' \
    "$external_b" | sort > "$output/query-sys-alarm.txt"
grep -Fqx $'SYS-ALARM-001\tWMSCS-r1\tpotable-water-control\tSC2\tSA-002' "$output/query-sc2.txt"
grep -Fqx $'potable-water-control\tSC2\tSA-002\thazard-analysis.git@b729a30#HAZ-017' "$output/query-sys-alarm.txt"
grep -Fqx $'uncrewed-rural\tSC1\tSA-001\thazard-analysis.git@a014c2e#HAZ-004' "$output/query-sys-alarm.txt"

changed_assertions=$(diff -U0 "$external_a" "$external_b" | grep -Ec '^[+-]SA-' || true)
[[ $changed_assertions -eq 2 ]]
{
    echo "scheme=WMSCS-r1"
    echo "scheme_kind=project-defined-experimental"
    echo "scheme_sha256=$scheme_sha256"
    echo "requirement_sha256=$requirements_sha256"
    echo "requirement_text_changed=no"
    echo "changed_assertion=SA-002"
    echo "change_cause=hazard-analysis-revision"
    echo "contexts_for_SYS-ALARM-001=2"
    echo "baseline_b_levels_for_SYS-ALARM-001=SC2,SC1"
    echo "inline_authority=inline-safety-assessment-lines"
    echo "external_authority=assessments.tsv"
    echo "derived_inline_display_authoritative=no"
    echo "descriptive_safety_related_tag_equivalent_to_assessed_level=no"
    echo "certification_claim=none"
} > "$output/model-summary.txt"

rm "$output/inline-a-requirements.mreq" "$output/inline-b-requirements.mreq"
(
    cd "$output"
    sha256sum *.diff *.txt > SHA256SUMS
)
echo "Safety-classification evidence written to $output"
