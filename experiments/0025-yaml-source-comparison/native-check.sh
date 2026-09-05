#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
comparison_build=build/yaml-comparison
"${COMPARISON_NATIVE_IMAGE:-native-image}" -O0 --no-fallback -march=compatibility \
    -cp "$comparison_build/classes:$comparison_build/snakeyaml-engine-3.1.1.jar" \
    -o "$comparison_build/yaml-probe" YamlProbe > "$comparison_build/native-build.log" 2>&1
exec "$comparison_build/venv/bin/python" -B experiments/0025-yaml-source-comparison/native-check.py "$@"
