package mundanereq.boundary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/** Removes each generated executable in turn and starts the other two. */
public final class NativeBoundaryIsolationTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();

    private NativeBoundaryIsolationTest() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 3) throw new AssertionError("expected validator, formatter, and trace paths");
        List<Path> executables = java.util.Arrays.stream(arguments)
                .map(Path::of)
                .map(path -> path.toAbsolutePath().normalize())
                .toList();
        for (int removedIndex = 0; removedIndex < executables.size(); removedIndex++) {
            Path removed = executables.get(removedIndex);
            Path held = removed.resolveSibling(removed.getFileName() + ".removed-for-test");
            Files.move(removed, held, StandardCopyOption.REPLACE_EXISTING);
            try {
                for (int runIndex = 0; runIndex < executables.size(); runIndex++) {
                    if (runIndex != removedIndex) assertStarts(executables.get(runIndex));
                }
            } finally {
                Files.move(held, removed, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        System.out.println("PASS independent native executable boundaries");
    }

    private static void assertStarts(Path executable) throws IOException, InterruptedException {
        String name = executable.getFileName().toString();
        List<String> command = switch (name) {
            case "mundanereq-validate" -> List.of(executable.toString(), "--version");
            case "mundanereq-format" -> List.of(
                    executable.toString(),
                    "--check",
                    ROOT.resolve("experiments/0008-formatting-policy/candidate-conservative.mreq").toString());
            default -> List.of(executable.toString(), "--boundary-smoke");
        };
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int status = process.waitFor();
        String expected = name.equals("mundanereq-validate")
                ? "source contract mundanereq-source-0.2"
                : name.equals("mundanereq-trace") ? " boundary" : "";
        if (status != 0 || !output.contains(expected)) {
            throw new AssertionError("failed boundary start for %s: status %d, output %s"
                    .formatted(executable, status, output));
        }
    }
}
