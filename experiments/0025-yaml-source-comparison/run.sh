#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
# Run setup.sh first; normal replay performs no downloads and never updates evidence.
exec build/yaml-comparison/venv/bin/python -B experiments/0025-yaml-source-comparison/run.py "$@"
