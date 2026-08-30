#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "usage: $0 VALIDATOR NEW_OUTPUT_DIRECTORY" >&2
    exit 2
fi

validator=$(readlink -f "$1")
output=$(readlink -m "$2")
experiment=$(cd "$(dirname "$0")" && pwd)
temporary=$(mktemp -d)
trap 'rm -rf "$temporary"' EXIT

if [[ -e $output ]]; then
    echo "output already exists: $output" >&2
    exit 2
fi
mkdir -p "$output"

check_companion() {
    local source=$1 definitions=$2 uses=$3 display=$4
    awk -F '\t' -v display="$display" '
        FILENAME == ARGV[1] {
            if (/^#/) next
            if (/^requirement /) { split($0, f, " "); current=f[2]; requirements[current]=1 }
            if (current != "") bodies[current]=bodies[current] "\n" $0
            next
        }
        FILENAME == ARGV[2] && FNR == 1 {
            definition_header=1
            if ($0 != "definition_id\tpreferred_text\taliases\tmeaning") malformed=1
            next
        }
        FILENAME == ARGV[2] {
            if (NF != 4 || $1 == "" || $2 == "" || $4 == "" || definitions[$1]++) { malformed=1; next }
            allowed[$1 SUBSEP $2]=1
            allowed_text[$1]=allowed_text[$1] SUBSEP $2
            count=split($3, alias, "|")
            for (i=1; i<=count; i++) if (alias[i] != "") {
                allowed[$1 SUBSEP alias[i]]=1
                allowed_text[$1]=allowed_text[$1] SUBSEP alias[i]
            }
            next
        }
        FILENAME == ARGV[3] && FNR == 1 {
            use_header=1
            if ($0 != "requirement_id\tdefinition_id\tliteral") malformed=1
            next
        }
        FILENAME == ARGV[3] {
            if (NF != 3 || $1 == "" || $2 == "" || $3 == "" || uses[$1 SUBSEP $2 SUBSEP $3]++) { malformed=1; next }
            if (!($1 in requirements)) { print display ":" FNR ": glossary-policy: unresolved requirement " $1 > "/dev/stderr"; semantic=1 }
            if (!($2 in definitions)) { print display ":" FNR ": glossary-policy: undefined term " $2 > "/dev/stderr"; semantic=1 }
            else if (!(($2 SUBSEP $3) in allowed)) { print display ":" FNR ": glossary-policy: literal is not a preferred term or alias for " $2 > "/dev/stderr"; semantic=1 }
            else definition_uses[$2]++
            if (index(bodies[$1], $3) == 0) { print display ":" FNR ": glossary-policy: literal not present in " $1 ": " $3 > "/dev/stderr"; semantic=1 }
        }
        END {
            for (definition in definitions) {
                if (!definition_uses[definition]) {
                    print display ": glossary-policy: unused definition " definition > "/dev/stderr"
                    semantic=1
                }
                count=split(allowed_text[definition], texts, SUBSEP)
                for (requirement in requirements) for (i=2; i<=count; i++) {
                    literal=texts[i]
                    if (index(bodies[requirement], literal) && !uses[requirement SUBSEP definition SUBSEP literal]) {
                        print display ": glossary-policy: unmapped use in " requirement ": " literal > "/dev/stderr"
                        semantic=1
                    }
                }
            }
            if (malformed || !definition_header || !use_header) exit 2
            exit semantic ? 3 : 0
        }
    ' "$source" "$definitions" "$uses"
}

for baseline in a b c; do
    source="$experiment/scenario/baseline-$baseline/requirements.mreq"
    definitions="$experiment/companion/baseline-$baseline/definitions.tsv"
    uses="$experiment/companion/baseline-$baseline/uses.tsv"
    "$validator" "$source" > "$output/validate-$baseline.txt"
    grep -Fqx 'Validated 5 requirements and 0 decomposition relationships from 1 file as mundanereq-source-0.2.' "$output/validate-$baseline.txt"
    check_companion "$source" "$definitions" "$uses" "companion/baseline-$baseline/uses.tsv"
done

source_a="$experiment/scenario/baseline-a/requirements.mreq"
source_b="$experiment/scenario/baseline-b/requirements.mreq"
source_c="$experiment/scenario/baseline-c/requirements.mreq"
definitions_a="$experiment/companion/baseline-a/definitions.tsv"
definitions_b="$experiment/companion/baseline-b/definitions.tsv"
definitions_c="$experiment/companion/baseline-c/definitions.tsv"
uses_a="$experiment/companion/baseline-a/uses.tsv"
uses_b="$experiment/companion/baseline-b/uses.tsv"
uses_c="$experiment/companion/baseline-c/uses.tsv"

diff -u --label scenario/baseline-a/requirements.mreq --label scenario/baseline-b/requirements.mreq "$source_a" "$source_b" > "$output/rename-requirements.diff" || [[ $? -eq 1 ]]
diff -u --label companion/baseline-a/definitions.tsv --label companion/baseline-b/definitions.tsv "$definitions_a" "$definitions_b" > "$output/rename-definitions.diff" || [[ $? -eq 1 ]]
diff -u --label companion/baseline-b/definitions.tsv --label companion/baseline-c/definitions.tsv "$definitions_b" "$definitions_c" > "$output/meaning-change.diff" || [[ $? -eq 1 ]]
diff -u --label prose/baseline-a/definitions.txt --label prose/baseline-b/definitions.txt "$experiment/prose/baseline-a/definitions.txt" "$experiment/prose/baseline-b/definitions.txt" > "$output/prose-rename.diff" || [[ $? -eq 1 ]]
diff -u --label prose/baseline-b/definitions.txt --label prose/baseline-c/definitions.txt "$experiment/prose/baseline-b/definitions.txt" "$experiment/prose/baseline-c/definitions.txt" > "$output/prose-meaning-change.diff" || [[ $? -eq 1 ]]
diff -u --label tool-only/conventions-a.awk --label tool-only/conventions-b.awk "$experiment/tool-only/conventions-a.awk" "$experiment/tool-only/conventions-b.awk" > "$output/tool-update.diff" || [[ $? -eq 1 ]]
diff -u --label tool-only/conventions-b.awk --label tool-only/conventions-c.awk "$experiment/tool-only/conventions-b.awk" "$experiment/tool-only/conventions-c.awk" > "$output/tool-meaning-update.diff" || [[ $? -eq 1 ]]
for diff_file in "$output"/*.diff; do sed -i 's/[[:space:]]\+$//' "$diff_file"; done
[[ $(grep -Ec '^[+-]TERM-LINK-LOSS' "$output/rename-definitions.diff") -eq 2 ]]
[[ $(grep -Ec '^[+-]TERM-LINK-LOSS' "$output/meaning-change.diff") -eq 2 ]]

IFS=$'\t' read -r _ preferred_a aliases_a meaning_a < <(sed -n '2p' "$definitions_a")
IFS=$'\t' read -r _ preferred_b aliases_b meaning_b < <(sed -n '2p' "$definitions_b")
IFS=$'\t' read -r _ preferred_c aliases_c meaning_c < <(sed -n '2p' "$definitions_c")
[[ $preferred_a != "$preferred_b" && $aliases_a != "$aliases_b" && $meaning_a == "$meaning_b" ]]
[[ $preferred_b == "$preferred_c" && $aliases_b == "$aliases_c" && $meaning_b != "$meaning_c" ]]
for baseline in a b c; do
    definitions_variable="definitions_$baseline"
    definitions_file=${!definitions_variable}
    prose_file="$experiment/prose/baseline-$baseline/definitions.txt"
    awk -F '\t' 'FNR == 2 { print $2 " — " $4 }' "$definitions_file" | grep -Fixf - "$prose_file" > /dev/null
done
cmp "$source_b" "$source_c"
cmp "$uses_b" "$uses_c"
sed -e 's/command link unavailable/command link lost/g' \
    -e 's/command link is declared unavailable/command link is declared lost/g' \
    "$source_a" > "$temporary/expected-rename-source.mreq"
cmp "$temporary/expected-rename-source.mreq" "$source_b"

extract_math() {
    awk '/^  math latex$/ { inside=1 } inside { print } /^  end math$/ { inside=0 }' "$1"
}
extract_math "$source_a" > "$temporary/math-a.txt"
extract_math "$source_b" > "$temporary/math-b.txt"
cmp "$temporary/math-a.txt" "$temporary/math-b.txt"

changed_ids() {
    awk -F '\t' 'NR == FNR { if (FNR > 1) old[$1]=$0; next } FNR > 1 && old[$1] != $0 { print $1 }' "$1" "$2"
}
changed_ids "$definitions_a" "$definitions_b" > "$temporary/rename-ids.txt"
changed_ids "$definitions_b" "$definitions_c" > "$temporary/meaning-ids.txt"
grep -Fqx 'TERM-LINK-LOSS' "$temporary/rename-ids.txt"
grep -Fqx 'TERM-LINK-LOSS' "$temporary/meaning-ids.txt"
[[ $(wc -l < "$temporary/rename-ids.txt") -eq 1 && $(wc -l < "$temporary/meaning-ids.txt") -eq 1 ]]

affected_uses() {
    awk -F '\t' 'NR == FNR { changed[$1]=1; next } FNR > 1 && ($2 in changed) { print $1 "\t" $2 "\t" $3 }' "$1" "$2"
}
affected_uses "$temporary/rename-ids.txt" "$uses_b" > "$output/rename-impact.txt"
affected_uses "$temporary/meaning-ids.txt" "$uses_c" > "$output/meaning-impact.txt"
rename_impact_rows=$(wc -l < "$output/rename-impact.txt")
meaning_impact_rows=$(wc -l < "$output/meaning-impact.txt")
rename_affected_requirements=$(cut -f1 "$output/rename-impact.txt" | sort -u | wc -l)
meaning_affected_requirements=$(cut -f1 "$output/meaning-impact.txt" | sort -u | wc -l)
[[ $rename_impact_rows -eq 4 && $meaning_impact_rows -eq 4 ]]
[[ $rename_affected_requirements -eq 3 && $meaning_affected_requirements -eq 3 ]]

prose_impact() {
    awk '!/^#/ && /^requirement / { current=$2 } !/^#/ && /command link/ && /(unavailable|lost)/ { seen[current]=1 } END { for (id in seen) print id }' "$1" | sort
}
prose_impact "$source_b" > "$output/prose-rename-impact.txt"
prose_impact "$source_c" > "$output/prose-meaning-impact.txt"
cmp "$output/prose-rename-impact.txt" "$output/prose-meaning-impact.txt"
[[ $(wc -l < "$output/prose-rename-impact.txt") -eq 3 ]]
cmp "$output/prose-rename-impact.txt" <(cut -f1 "$output/rename-impact.txt" | sort -u)

awk -F '\t' 'BEGIN { OFS="\t" } { print } END { print "SYS-006", "TERM-UNDEFINED", "command link lost" }' "$uses_b" > "$temporary/undefined-uses.tsv"
set +e
check_companion "$source_b" "$definitions_b" "$temporary/undefined-uses.tsv" "mutated/uses.tsv" 2> "$output/undefined-use.txt"
undefined_status=$?
set -e
[[ $undefined_status -eq 3 ]]
grep -Fq 'glossary-policy: undefined term TERM-UNDEFINED' "$output/undefined-use.txt"

awk -F '\t' 'BEGIN { OFS="\t" } NR == 2 { $3="T_{\\mathrm{loss}}" } { print }' "$uses_b" > "$temporary/wrong-literal.tsv"
set +e
check_companion "$source_b" "$definitions_b" "$temporary/wrong-literal.tsv" "mutated/uses.tsv" 2> "$output/wrong-literal.txt"
wrong_status=$?
set -e
[[ $wrong_status -eq 3 ]]
grep -Fq 'literal is not a preferred term or alias' "$output/wrong-literal.txt"

awk -F '\t' '$1 != "SYS-009"' "$uses_b" > "$temporary/missing-use.tsv"
set +e
check_companion "$source_b" "$definitions_b" "$temporary/missing-use.tsv" "mutated/uses.tsv" 2> "$output/missing-use.txt"
missing_status=$?
set -e
[[ $missing_status -eq 3 ]]
grep -Fq 'glossary-policy: unmapped use in SYS-009' "$output/missing-use.txt"

awk -F '\t' 'BEGIN { OFS="\t" } { print } END { print "TERM-UNUSED", "unused term", "", "Unused meaning." }' "$definitions_b" > "$temporary/unused-definition.tsv"
set +e
check_companion "$source_b" "$temporary/unused-definition.tsv" "$uses_b" "mutated/uses.tsv" 2> "$output/unused-definition.txt"
unused_status=$?
set -e
[[ $unused_status -eq 3 ]]
grep -Fq 'glossary-policy: unused definition TERM-UNUSED' "$output/unused-definition.txt"

awk '
    /^requirement / { current=$2 }
    /^  math latex$/ { in_math=1; next }
    /^  end math$/ { in_math=0; next }
    in_math && /(^|[^[:alnum:]_])T([^[:alnum:]_]|$)/ { equation[current]=1 }
    /^  In this requirement, T is / { definition[current]=1 }
    END {
        for (id in equation) if (definition[id]) print id
        for (id in equation) if (!definition[id]) exit 3
        for (id in definition) if (!equation[id]) exit 3
    }
' "$source_c" | sort > "$output/local-symbol-scopes.txt"
grep -Fqx 'ENV-001' "$output/local-symbol-scopes.txt"
grep -Fqx 'MON-001' "$output/local-symbol-scopes.txt"

awk '
    /^  Within 1 s after the command link is declared lost,/ {
        print "  Within 1 s after loss declaration, the mission-control system shall"; next
    }
    /^requirement SYS-009$/ { print "# command link is declared lost" }
    { print }
' "$source_b" > "$temporary/comment-only-term.mreq"
set +e
check_companion "$temporary/comment-only-term.mreq" "$definitions_b" "$uses_b" "mutated/uses.tsv" 2> "$output/comment-only-use.txt"
comment_status=$?
set -e
[[ $comment_status -eq 3 ]]
grep -Fq 'literal not present in SYS-009' "$output/comment-only-use.txt"

awk -v display='scenario/baseline-a/requirements.mreq' -f "$experiment/tool-only/conventions-a.awk" "$source_a" > "$output/tool-a-on-a.txt"
set +e
awk -v display='scenario/baseline-b/requirements.mreq' -f "$experiment/tool-only/conventions-a.awk" "$source_b" > "$output/tool-a-on-b.txt" 2> "$output/tool-drift.txt"
tool_drift_status=$?
set -e
[[ $tool_drift_status -eq 3 ]]
awk -v display='scenario/baseline-b/requirements.mreq' -f "$experiment/tool-only/conventions-b.awk" "$source_b" > "$output/tool-b-on-b.txt"
awk -v display='scenario/baseline-c/requirements.mreq' -f "$experiment/tool-only/conventions-b.awk" "$source_c" > "$output/tool-b-on-c.txt"
awk -v display='scenario/baseline-c/requirements.mreq' -f "$experiment/tool-only/conventions-c.awk" "$source_c" > "$output/tool-c-on-c.txt"
grep -Fqx 'recognized_uses=4' "$output/tool-a-on-a.txt"
grep -Fqx 'recognized_uses=0' "$output/tool-a-on-b.txt"
grep -Fqx 'recognized_uses=4' "$output/tool-b-on-b.txt"
grep -Fqx 'recognized_uses=4' "$output/tool-b-on-c.txt"
grep -Fqx 'recognized_uses=4' "$output/tool-c-on-c.txt"
[[ $(wc -l < "$output/tool-drift.txt") -eq 4 ]]
tool_b_meaning=$(sed -n 's/^tool_owned_meaning=//p' "$output/tool-b-on-c.txt")
tool_c_meaning=$(sed -n 's/^tool_owned_meaning=//p' "$output/tool-c-on-c.txt")
tool_a_meaning=$(sed -n 's/^tool_owned_meaning=//p' "$output/tool-a-on-a.txt")
[[ $tool_a_meaning == "$meaning_a" && $tool_b_meaning == "$meaning_b" && $tool_b_meaning != "$meaning_c" && $tool_c_meaning == "$meaning_c" ]]

term_definitions=$(($(wc -l < "$definitions_c") - 1))
term_uses=$(($(wc -l < "$uses_c") - 1))
rename_changed_ids=$(wc -l < "$temporary/rename-ids.txt")
meaning_changed_ids=$(wc -l < "$temporary/meaning-ids.txt")
local_symbol_scopes=$(wc -l < "$output/local-symbol-scopes.txt")
formal_symbol_definitions=$(grep -Ec 'SYMBOL-|\\\\' "$definitions_c" || true)
[[ $term_definitions -eq 1 && $term_uses -eq 4 && $local_symbol_scopes -eq 2 && $formal_symbol_definitions -eq 0 ]]

{
    echo "term_definitions=$term_definitions"
    echo "term_uses=$term_uses"
    echo "rename_changed_definition_ids=$rename_changed_ids"
    echo "meaning_changed_definition_ids=$meaning_changed_ids"
    echo "rename_affected_requirements=$rename_affected_requirements"
    echo "meaning_affected_requirements=$meaning_affected_requirements"
    echo 'prose_search_matches_companion_impact=yes'
    echo 'math_payload_changed_during_term_rename=no'
    echo 'term_companion_selected=no'
    echo 'formal_symbol_companion_selected=no'
    echo "formal_symbol_definitions=$formal_symbol_definitions"
    echo "local_symbol_T_scopes=$local_symbol_scopes"
    echo 'latex_payload_interpreted=no'
    echo 'tool_only_rename_requires_tool_change=yes'
    echo 'tool_only_meaning_change_requires_tool_change=yes'
} > "$output/model-summary.txt"

(cd "$output" && sha256sum *.diff *.txt > SHA256SUMS)
echo "Glossary and symbol evidence written to $output"
