package mundanereq.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mundanereq.Interpreter;
import mundanereq.SourceFormat;
import mundanereq.Versions;

/** Separate-output migration; the original source is never written. */
public final class MigrateMain {
    private MigrateMain() {}

    public static void main(String[] arguments) {
        System.exit(run(arguments, System.out, System.err));
    }

    static int run(String[] arguments, PrintStream out, PrintStream err) {
        int status;
        try {
            status = migrate(arguments, out, err);
        } catch (IOException | IllegalArgumentException exception) {
            err.println("migration-failed: " + exception.getMessage());
            status = 2;
        }
        return CommandOutput.finish(out, err, status);
    }

    private static int migrate(String[] args, PrintStream out, PrintStream err) throws IOException {
        if (args.length == 1 && args[0].equals("--version")) {
            out.printf("mundanereq-migrate %s; %s -> %s%n", Versions.MIGRATE_VERSION, Versions.SOURCE_CUSTOM, Versions.SOURCE_YAML);
            return 0;
        }
        if (args.length == 1 && args[0].equals("--help")) { out.print(usage()); return 0; }
        boolean dryRun = args.length > 0 && args[0].equals("--dry-run");
        int start = dryRun ? 1 : 0;
        if (args.length - start < 2) { err.print(usage()); return 2; }
        Path destination = Path.of(args[start]).toAbsolutePath().normalize();
        if (Files.exists(destination, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("output directory must not exist: " + destination);
        }
        List<Path> inputs = new ArrayList<>();
        for (int i = start + 1; i < args.length; i++) inputs.add(Path.of(args[i]));
        Interpreter.Selection selection = Interpreter.selectInputs(inputs);
        Interpreter.Result original = selection.valid() ? Interpreter.interpretSources(selection.sources()) : null;
        List<Interpreter.Diagnostic> diagnostics = original == null ? selection.diagnostics() : original.diagnostics();
        if (!diagnostics.isEmpty()) {
            for (var d : diagnostics) err.printf("%s:%d:%d: %s: %s%n", d.file(), d.line(), d.column(), d.code(), d.message());
            return 2;
        }
        Map<Path, byte[]> outputs = new LinkedHashMap<>();
        for (var source : selection.sources()) {
            String name = Path.of(source.file()).getFileName().toString();
            if (name.endsWith(".mreq")) name = name.substring(0, name.length() - 5);
            Path target = destination.resolve(name + ".mreq.yaml");
            if (outputs.putIfAbsent(target, encode(source, original.byId())) != null) {
                throw new IOException("output filename collision: " + target);
            }
        }
        List<Interpreter.Source> candidates = outputs.entrySet().stream()
                .map(e -> new Interpreter.Source(e.getKey().toString(), e.getValue())).toList();
        var converted = Interpreter.interpretSources(candidates, SourceFormat.YAML_03);
        if (!converted.valid() || !original.byId().equals(converted.byId())) {
            throw new IOException("conversion failed whole-source-set semantic equality: " + converted.diagnostics());
        }
        if (dryRun) {
            out.printf("Verified conversion of %d requirements into %d files; no files written.%n",
                    original.requirements().size(), outputs.size());
            outputs.keySet().forEach(path -> out.println("Would create: " + path));
            return 0;
        }
        Files.createDirectory(destination);
        writeOutputs(outputs, out);
        out.printf("Migrated %d requirements into %d %s; originals unchanged.%n",
                original.requirements().size(), outputs.size(), outputs.size() == 1 ? "file" : "files");
        return 0;
    }

    static void writeOutputs(Map<Path, byte[]> outputs, PrintStream out) throws IOException {
        for (var output : outputs.entrySet()) {
            // CREATE_NEW prevents collisions, including a file appearing after directory creation.
            Files.write(output.getKey(), output.getValue(), StandardOpenOption.CREATE_NEW);
            out.println("Created: " + output.getKey());
        }
    }

    private static byte[] encode(Interpreter.Source source, Map<String, Interpreter.Requirement> byId) {
        String text = new String(source.bytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        StringBuilder out = new StringBuilder("format: \"mundanereq-yaml-0.3\"\n");
        // Source 0.2 comments have no semantic attachment. Preserve exact comment lines in order at the file header.
        text.lines().filter(line -> line.startsWith("#")).forEach(line -> out.append(line).append('\n'));
        out.append("requirements:\n");
        for (String line : text.lines().filter(value -> value.startsWith("requirement ")).toList()) {
            var r = byId.get(line.substring("requirement ".length()));
            if (r == null) throw new IllegalArgumentException("unable to locate validated record");
            out.append("  - id: ").append(quote(r.id())).append('\n');
            field(out, "title", r.title());
            field(out, "allocation", r.allocation());
            out.append("    statement:\n");
            for (var block : r.statement()) {
                if (block instanceof Interpreter.ProseBlock prose) {
                    out.append("      - prose: ").append(quote(prose.text())).append('\n');
                } else if (block instanceof Interpreter.MathBlock math) {
                    out.append("      - math:\n          language: ").append(quote(math.language()))
                            .append("\n          payload: ").append(quote(math.payload())).append('\n');
                }
            }
            if (r.rationale() != null) {
                out.append("    rationale:\n");
                for (var block : r.rationale()) out.append("      - ").append(quote(((Interpreter.ProseBlock) block).text())).append('\n');
            }
            field(out, "source", r.source());
            if (!r.decomposes().isEmpty()) {
                out.append("    decomposes:\n");
                r.decomposes().stream().sorted().forEach(target -> out.append("      - ").append(quote(target)).append('\n'));
            }
        }
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void field(StringBuilder out, String key, String value) {
        if (value != null) out.append("    ").append(key).append(": ").append(quote(value)).append('\n');
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }

    private static String usage() {
        return "Usage: mundanereq-migrate [--dry-run] NEW_OUTPUT_DIRECTORY SOURCE...\n"
                + "       mundanereq-migrate --help | --version\n";
    }
}
