# Maintained YAML parser dependency

SnakeYAML Engine 3.1.1, Apache-2.0, is the sole runtime Java library dependency.
The Maven artifact is fetched and SHA-256 checked by scripts/fetch-yaml-parser.sh:
`59d73655cf077f154137e2d6f6f92c041a954c0b1c534c63800047a0d70a6947`.

Source and provenance: [versioned Maven POM](https://repo.maven.apache.org/maven2/org/snakeyaml/snakeyaml-engine/3.1.1/snakeyaml-engine-3.1.1.pom),
[source artifact](https://repo.maven.apache.org/maven2/org/snakeyaml/snakeyaml-engine/3.1.1/snakeyaml-engine-3.1.1-sources.jar).
The POM identifies upstream tag `snakeyaml-engine-3.1.1` at Codeberg.
The library includes Google escaping code (Copyright (c) 2008 Google Inc.), also
Apache-2.0. Preserve this notice with the included Apache license in native packages.
No dependency jar is checked in. Builds with the verified cached jar work offline;
a missing jar requires curl/network access. Hash mismatch stops the build.

Native executables contain reachable parser code; they do not require Java, a
Python schema validator, or a dependency jar at runtime. JSON Schema is implemented
by explicit node validation and independently cross-checked during conformance
verification. Upgrading the parser requires rerunning source and native tests.
