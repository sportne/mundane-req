#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
schema_env=build/schema-check-venv
schema_requirements=dependencies/schema-test-requirements.txt
if [[ ! -x "$schema_env/bin/python" ]]; then python3 -m venv "$schema_env"; fi
if ! cmp -s "$schema_requirements" "$schema_env/requirements.lock"; then
    "$schema_env/bin/python" -m pip install -r "$schema_requirements"
    cp "$schema_requirements" "$schema_env/requirements.lock"
fi
exec "$schema_env/bin/python" -B scripts/check-yaml-schema.py
