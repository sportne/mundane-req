package mundanereq.cli;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import mundanereq.Interpreter;
import mundanereq.SourceFormat;

/** Source contracts and complete commands compared across JVM and native execution. */
public final class YamlVerificationTest {
    private YamlVerificationTest() {}
    private static void check(boolean value, Object message) { YamlWorkflowTest.require(value, message); }

    public static void main(String[] args) throws Exception {
        check(args.length == 4, "expected four native tool paths");
        Map<String, Path> natives = Map.of("ValidatorMain", Path.of(args[0]).toAbsolutePath(),
                "FormatterMain", Path.of(args[1]).toAbsolutePath(), "TraceMain", Path.of(args[2]).toAbsolutePath(),
                "MigrateMain", Path.of(args[3]).toAbsolutePath());
        Path temp = Files.createTempDirectory("mundanereq-yaml-native-");
        int records = 0;
        try {
            int set = 0;
            for (String row : Files.readAllLines(Path.of("conformance/0.3/migration-corpus.tsv"))) {
                String[] columns = row.split("\t");
                var legacy = Interpreter.interpretInputs(List.of(Path.of(columns[0])));
                var yaml = Interpreter.interpretInputs(List.of(Path.of(columns[1])), SourceFormat.YAML_03);
                check(legacy.valid() && yaml.valid() && legacy.byId().equals(yaml.byId()), row + yaml.diagnostics());
                records += yaml.requirements().size();
                equal(natives, "ValidatorMain", 0, "--source=yaml-0.3", columns[1]);
                equal(natives, "FormatterMain", 0, "--source=yaml-0.3", "--check", columns[1]);
                String id = yaml.byId().keySet().stream().sorted().findFirst().orElseThrow();
                equal(natives, "TraceMain", 0, "--source=yaml-0.3", "impact", id, columns[1]);
                Path out = temp.resolve("dry-" + set);
                equal(natives, "MigrateMain", 0, "--dry-run", out.toString(), columns[0]);
                check(!Files.exists(out), "dry-run created output");
                // Native migration reproduces checked-in source bytes and preserves comment sequence.
                var run = process(List.of(natives.get("MigrateMain").toString(), out.toString(), columns[0]));
                check(run.status == 0, run);
                try (var generatedFiles = Files.list(out)) {
                for (Path generated : generatedFiles.toList()) {
                    check(Arrays.equals(Files.readAllBytes(generated), Files.readAllBytes(Path.of(columns[1]).resolve(generated.getFileName()))), generated);
                }
                }
                var sourceSet = Interpreter.selectInputs(List.of(Path.of(columns[0])));
                for (var source : sourceSet.sources()) {
                    String name = Path.of(source.file()).getFileName().toString(); name = name.substring(0, name.length() - 5) + ".mreq.yaml";
                    List<String> before = new String(source.bytes(), StandardCharsets.UTF_8).lines().filter(l -> l.startsWith("#")).toList();
                    List<String> after = Files.readString(out.resolve(name)).lines().filter(l -> l.startsWith("#")).toList();
                    check(before.equals(after), "migration comments: " + source.file());
                }
                set++;
            }
            int invalid = 0;
            for (String row : Files.readAllLines(Path.of("conformance/0.3/invalid-cases.tsv"))) {
                String[] c = row.split("\t"); Path input = Path.of("conformance/0.3/invalid").resolve(c[0]);
                var result = Interpreter.interpretInputs(List.of(input), SourceFormat.YAML_03);
                check(!result.valid(), row);
                check(result.diagnostics().stream().anyMatch(d -> d.code().equals(c[1]) && d.line() == Integer.parseInt(c[2]) && d.column() == Integer.parseInt(c[3])), row + " actual " + result.diagnostics());
                equal(natives, "ValidatorMain", 1, "--source=yaml-0.3", input.toString());
                equal(natives, "FormatterMain", 2, "--source=yaml-0.3", "--check", input.toString());
                equal(natives, "TraceMain", 2, "--source=yaml-0.3", "impact", "A", input.toString());
                invalid++;
            }
            equal(natives, "ValidatorMain", 0, "--source=yaml-0.3", "conformance/0.3/authoring");
            equal(natives, "TraceMain", 0, "--source=yaml-0.3", "impact", "TOP", "conformance/0.3/authoring");
            Path format = temp.resolve("format.mreq.yaml");
            String original = Files.readString(Path.of("conformance/0.3/authoring/requirements.mreq.yaml"));
            // Inline comments, block scalars, and exact text survive physical formatting on both runtimes.

            Files.writeString(format, original.replace("\n", "\r\n"));
            equal(natives, "FormatterMain", 1, "--source=yaml-0.3", "--check", format.toString());
            equal(natives, "FormatterMain", 0, "--source=yaml-0.3", format.toString());
            check(process(List.of(natives.get("FormatterMain").toString(), "--source=yaml-0.3", "--write", format.toString())).status == 0, "native write");
            check(Files.readString(format).equals(original), "source trivia changed");
            equal(natives, "FormatterMain", 0, "--source=yaml-0.3", "--check", format.toString());
            for (String tool : natives.keySet()) {
                for (boolean nativeRun : List.of(false, true)) {
                    List<String> command = new ArrayList<>(List.of("sh", "-c", "exec \"$@\" >&-", "closed-stdout"));
                    command.addAll(command(natives, tool, nativeRun)); command.add("--version");
                    check(process(command).status != 0, "closed descriptor returned success: " + tool);
                }
            }
            Path large = temp.resolve("large.mreq.yaml");
            Files.writeString(large, "format: \"mundanereq-yaml-0.3\"\nrequirements:\n  - id: \"A\"\n    title: \"T\"\n    statement: \"" + "x".repeat(1024 * 1024) + "\"\n");
            for (boolean nativeRun : List.of(false, true)) {
                List<String> command = new ArrayList<>(command(natives, "FormatterMain", nativeRun));
                command.addAll(List.of("--source=yaml-0.3", large.toString()));
                Process process = new ProcessBuilder(command).start(); process.getInputStream().close();
                byte[] errors = process.getErrorStream().readAllBytes();
                check(process.waitFor(30, TimeUnit.SECONDS), "pipe process timeout");
                check(process.exitValue() != 0, "broken pipe success: " + new String(errors, StandardCharsets.UTF_8));
            }
            System.out.printf("PASS %d equivalent requirements across 3 migration corpora; %d golden invalid cases; JVM/native validation, trace, formatting, migration, closed descriptors and broken pipes%n", records, invalid);
        } finally { YamlWorkflowTest.delete(temp); }
    }

    private static void equal(Map<String, Path> natives, String tool, int status, String... args) throws Exception {
        List<String> jvm = new ArrayList<>(command(natives, tool, false)); jvm.addAll(List.of(args));
        List<String> nativeCommand = new ArrayList<>(command(natives, tool, true)); nativeCommand.addAll(List.of(args));
        Result a = process(jvm); Result b = process(nativeCommand);
        check(a.status == status && a.equals(b), tool + List.of(args) + " expected " + status + " JVM " + a + " native " + b);
    }

    private static List<String> command(Map<String, Path> natives, String tool, boolean nativeRun) {
        return nativeRun ? List.of(natives.get(tool).toString()) : List.of(
                Path.of(System.getProperty("java.home"), "bin/java").toString(), "-cp", System.getProperty("java.class.path"), "mundanereq.cli." + tool);
    }
    private record Result(int status, String out, String err) {}
    private static Result process(List<String> command) throws Exception {
        Process p = new ProcessBuilder(command).start();
        try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var out = pool.submit(() -> p.getInputStream().readAllBytes());
            var err = pool.submit(() -> p.getErrorStream().readAllBytes());
            if (!p.waitFor(30, TimeUnit.SECONDS)) { p.destroyForcibly(); throw new AssertionError("timeout " + command); }
            return new Result(p.exitValue(), new String(out.get(5, TimeUnit.SECONDS), StandardCharsets.UTF_8), new String(err.get(5, TimeUnit.SECONDS), StandardCharsets.UTF_8));
        }
    }
}
