package mundanereq.boundary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/** Removes each generated executable in turn and starts the other two. */
public final class NativeBoundaryIsolationTest {
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
        Process process = new ProcessBuilder(executable.toString(), "--boundary-smoke")
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int status = process.waitFor();
        if (status != 0 || !output.contains(" boundary")) {
            throw new AssertionError("failed boundary start for %s: status %d, output %s"
                    .formatted(executable, status, output));
        }
    }
}
