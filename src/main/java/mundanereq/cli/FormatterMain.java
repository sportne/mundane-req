package mundanereq.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mundanereq.Interpreter;
import mundanereq.format.SourceFormatter;
import mundanereq.source.SourceDocument;

/** Focused formatter for the conservative Experiment 0008 policy. */
public final class FormatterMain {
    static final String TOOL_VERSION = "development";
    static final String SOURCE_CONTRACT = "mundanereq-source-0.2";

    private enum Mode {
        STANDARD_OUTPUT,
        CHECK,
        WRITE
    }

    private record Invocation(Mode mode, Path output, List<Path> inputs) {}

    private FormatterMain() {}

    public static void main(String[] arguments) {
        System.exit(run(arguments, System.out, System.err));
    }

    static int run(String[] arguments, PrintStream out, PrintStream err) {
        if (arguments.length == 1 && arguments[0].equals("--help")) {
            out.print(usage());
            return 0;
        }
        if (arguments.length == 1 && arguments[0].equals("--version")) {
            out.printf("mundanereq-format %s; source contract %s%n", TOOL_VERSION, SOURCE_CONTRACT);
            return 0;
        }

        Invocation invocation = parseInvocation(arguments, err);
        if (invocation == null) return 2;

        Interpreter.Selection selection = Interpreter.selectInputs(invocation.inputs());
        if (!selection.valid()) {
            renderDiagnostics(selection.diagnostics(), err);
            return 2;
        }
        Interpreter.Result semantics = Interpreter.interpretSources(selection.sources());
        if (!semantics.valid()) {
            renderDiagnostics(semantics.diagnostics(), err);
            return 2;
        }

        Map<Path, byte[]> formatted;
        try {
            formatted = format(selection.sources());
        } catch (RuntimeException exception) {
            err.println("formatter-internal: " + exception.getMessage());
            return 2;
        }

        return switch (invocation.mode()) {
            case STANDARD_OUTPUT -> writeStandardOutput(invocation.output(), formatted, out, err);
            case CHECK -> check(selection.sources(), formatted, out);
            case WRITE -> writeFiles(selection.sources(), formatted, out, err);
        };
    }

    private static Invocation parseInvocation(String[] arguments, PrintStream err) {
        Mode mode = Mode.STANDARD_OUTPUT;
        int index = 0;
        if (arguments.length > 0 && arguments[0].equals("--check")) {
            mode = Mode.CHECK;
            index++;
        } else if (arguments.length > 0 && arguments[0].equals("--write")) {
            mode = Mode.WRITE;
            index++;
        }

        boolean optionsEnded = false;
        List<Path> paths = new ArrayList<>();
        try {
            for (; index < arguments.length; index++) {
                String argument = arguments[index];
                if (!optionsEnded && argument.equals("--")) {
                    optionsEnded = true;
                } else if (!optionsEnded && argument.startsWith("-")) {
                    err.println("unknown option: " + argument);
                    err.print(usage());
                    return null;
                } else {
                    paths.add(Path.of(argument));
                }
            }
        } catch (InvalidPathException exception) {
            err.println("invalid input path: " + exception.getMessage());
            return null;
        }

        if (paths.isEmpty()) {
            err.print(usage());
            return null;
        }
        if (mode != Mode.STANDARD_OUTPUT) return new Invocation(mode, null, List.copyOf(paths));

        Path output = paths.getFirst().toAbsolutePath().normalize();
        if (!Files.isRegularFile(output, LinkOption.NOFOLLOW_LINKS)) {
            err.println("output file is not an explicit regular file: " + paths.getFirst());
            return null;
        }
        return new Invocation(mode, output, List.copyOf(paths));
    }

    private static Map<Path, byte[]> format(List<Interpreter.Source> sources) {
        Map<Path, byte[]> formatted = new LinkedHashMap<>();
        for (Interpreter.Source source : sources) {
            try {
                SourceDocument document = SourceDocument.read(source.file(), source.bytes());
                formatted.put(Path.of(source.file()), SourceFormatter.format(document));
            } catch (java.nio.charset.CharacterCodingException exception) {
                throw new IllegalStateException("validated source could not be decoded", exception);
            }
        }
        return Map.copyOf(formatted);
    }

    private static int writeStandardOutput(Path outputFile, Map<Path, byte[]> formatted, PrintStream out, PrintStream err) {
        byte[] bytes = formatted.get(outputFile);
        if (bytes == null) {
            err.println("output file was not selected: " + outputFile);
            return 2;
        }
        out.write(bytes, 0, bytes.length);
        out.flush();
        if (out.checkError()) {
            err.println("standard-output:1:1: output-failed: unable to write formatted source");
            return 2;
        }
        return 0;
    }

    private static int check(List<Interpreter.Source> sources, Map<Path, byte[]> formatted, PrintStream out) {
        int changes = 0;
        for (Interpreter.Source source : sources) {
            Path path = Path.of(source.file());
            if (!java.util.Arrays.equals(source.bytes(), formatted.get(path))) {
                out.println("Needs formatting: " + path);
                changes++;
            }
        }
        return changes == 0 ? 0 : 1;
    }

    private static int writeFiles(
            List<Interpreter.Source> sources, Map<Path, byte[]> formatted, PrintStream out, PrintStream err) {
        int changes = 0;
        for (Interpreter.Source source : sources) {
            Path path = Path.of(source.file());
            byte[] bytes = formatted.get(path);
            if (java.util.Arrays.equals(source.bytes(), bytes)) continue;
            try {
                replace(path, bytes);
                changes++;
            } catch (IOException exception) {
                err.printf("%s:1:1: write-failed: %s%n", path, exception.getMessage());
                return 2;
            }
        }
        out.printf("Formatted %d %s.%n", changes, changes == 1 ? "file" : "files");
        return 0;
    }

    private static void replace(Path path, byte[] bytes) throws IOException {
        Path parent = path.toAbsolutePath().normalize().getParent();
        Path temporary = Files.createTempFile(parent, "." + path.getFileName() + ".", ".tmp");
        try {
            Files.write(temporary, bytes);
            copyPosixPermissions(path, temporary);
            try {
                Files.move(
                        temporary,
                        path,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void copyPosixPermissions(Path source, Path target) throws IOException {
        try {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(source);
            Files.setPosixFilePermissions(target, permissions);
        } catch (UnsupportedOperationException ignored) {
            // The filesystem has no POSIX permission view.
        }
    }

    private static void renderDiagnostics(List<Interpreter.Diagnostic> diagnostics, PrintStream err) {
        diagnostics.forEach(diagnostic -> err.printf(
                "%s:%d:%d: %s: %s%n",
                diagnostic.file(),
                diagnostic.line(),
                diagnostic.column(),
                diagnostic.code(),
                diagnostic.message()));
    }

    private static String usage() {
        return "Usage: mundanereq-format [--] FILE [CONTEXT...]\n"
                + "       mundanereq-format --check [--] FILE_OR_DIRECTORY...\n"
                + "       mundanereq-format --write [--] FILE_OR_DIRECTORY...\n"
                + "       mundanereq-format --help\n"
                + "       mundanereq-format --version\n";
    }
}
