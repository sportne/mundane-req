#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/dependencies
parser_jar=build/dependencies/snakeyaml-engine-3.1.1.jar
parser_sha=59d73655cf077f154137e2d6f6f92c041a954c0b1c534c63800047a0d70a6947
if [[ ! -f "$parser_jar" ]]; then
    parser_temp=$(mktemp build/dependencies/parser.XXXXXX)
    trap 'rm -f "$parser_temp"' EXIT
    curl --fail --location --silent --show-error \
      https://repo.maven.apache.org/maven2/org/snakeyaml/snakeyaml-engine/3.1.1/snakeyaml-engine-3.1.1.jar -o "$parser_temp"
    printf '%s  %s\n' "$parser_sha" "$parser_temp" | sha256sum --check --status
    mv "$parser_temp" "$parser_jar"
fi
printf '%s  %s\n' "$parser_sha" "$parser_jar" | sha256sum --check --status
