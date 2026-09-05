#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
experiment=experiments/0025-yaml-source-comparison
comparison_build=build/yaml-comparison
mkdir -p "$comparison_build/classes"
if [[ ! -x "$comparison_build/venv/bin/python" ]]; then
    python3 -m venv "$comparison_build/venv"
fi
"$comparison_build/venv/bin/python" -m pip install -r "$experiment/requirements.txt"
python3 - <<'PY'
from pathlib import Path
import hashlib, urllib.request
p=Path('build/yaml-comparison/snakeyaml-engine-3.1.1.jar')
expected='59d73655cf077f154137e2d6f6f92c041a954c0b1c534c63800047a0d70a6947'
if not p.exists():
    p.write_bytes(urllib.request.urlopen('https://repo.maven.apache.org/maven2/org/snakeyaml/snakeyaml-engine/3.1.1/snakeyaml-engine-3.1.1.jar').read())
if hashlib.sha256(p.read_bytes()).hexdigest()!=expected:
    raise SystemExit('SnakeYAML jar checksum mismatch')
PY
mapfile -t comparison_sources < <(find src/main/java "$experiment/src" -name '*.java' -type f | LC_ALL=C sort)
"${COMPARISON_JAVAC:-javac}" --release 21 -Xlint:all -Werror \
    -cp "$comparison_build/snakeyaml-engine-3.1.1.jar" -d "$comparison_build/classes" "${comparison_sources[@]}"
