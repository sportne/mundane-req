package mundanereq.ci;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Verifies the documented clean-checkout CI invocations and their boundaries. */
public final class CiWorkflowVerificationTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();

    private record Invocation(int status, String out, String err) {}

    private CiWorkflowVerificationTest() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 3) {
            throw new AssertionError("expected formatter, validator, and trace executable paths");
        }
        Path formatter = executable(arguments[0]);
        Path validator = executable(arguments[1]);
        Path trace = executable(arguments[2]);
        Path valid = ROOT.resolve("conformance/0.2/valid");

        byte[] authoritativeBefore = Files.readAllBytes(valid.resolve("requirements.mreq"));
        assertStatus(0, invoke(formatter, "--check", valid.toString()), "formatting step");
        assertStatus(0, invoke(validator, valid.toString()), "validation step");
        assertStatus(0, invoke(trace, "parents", "LEAF", valid.toString()), "trace step");
        assertBytes(authoritativeBefore, Files.readAllBytes(valid.resolve("requirements.mreq")),
                "successful checks must not alter authoritative source");

        Path temporary = Files.createTempDirectory("mundanereq-ci-workflow-");
        try {
            Path unformatted = temporary.resolve("unformatted.mreq");
            Files.copy(ROOT.resolve("experiments/0008-formatting-policy/input-varied-crlf.mreq"), unformatted);
            Invocation formatFailure = invoke(formatter, "--check", unformatted.toString());
            assertStatus(1, formatFailure, "deliberate formatter failure");
            assertContains(formatFailure.out(), "Needs formatting", "formatter failure attribution");

            Path invalid = temporary.resolve("invalid.mreq");
            Files.writeString(invalid, "content outside a record\n", StandardCharsets.UTF_8);
            Invocation validationFailure = invoke(validator, invalid.toString());
            assertStatus(1, validationFailure, "deliberate validator failure");
            assertContains(validationFailure.err(), "content-outside-record", "validator failure attribution");

            Invocation traceFailure = invoke(trace, "parents", "ABSENT", valid.toString());
            assertStatus(2, traceFailure, "deliberate trace failure");
            assertContains(traceFailure.err(), "missing-requirement", "trace failure attribution");

            Path reducedInstallation = temporary.resolve("without-trace");
            Files.createDirectories(reducedInstallation);
            Path isolatedFormatter = copyExecutable(formatter, reducedInstallation);
            Path isolatedValidator = copyExecutable(validator, reducedInstallation);
            if (Files.exists(reducedInstallation.resolve(trace.getFileName()))) {
                throw new AssertionError("trace executable unexpectedly present in reduced installation");
            }
            assertStatus(0, invoke(isolatedFormatter, "--check", valid.toString()),
                    "formatting without trace executable");
            assertStatus(0, invoke(isolatedValidator, valid.toString()),
                    "validation without trace executable");
        } finally {
            deleteTree(temporary);
        }
        System.out.println("PASS clean-checkout commands, attributed failures, disposable output, and trace removal");
    }

    private static Path executable(String value) {
        Path path = Path.of(value).toAbsolutePath().normalize();
        if (!Files.isExecutable(path)) throw new AssertionError("not executable: " + path);
        return path;
    }

    private static Path copyExecutable(Path source, Path directory) throws IOException {
        Path target = directory.resolve(source.getFileName());
        Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
        if (!target.toFile().setExecutable(true, true)) throw new IOException("cannot mark executable: " + target);
        return target;
    }

    private static Invocation invoke(Path executable, String... arguments) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(executable.toString());
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).start();
        try (var readers = Executors.newFixedThreadPool(2)) {
            Future<byte[]> out = readers.submit(() -> process.getInputStream().readAllBytes());
            Future<byte[]> err = readers.submit(() -> process.getErrorStream().readAllBytes());
            int status = process.waitFor();
            return new Invocation(
                    status,
                    new String(out.get(), StandardCharsets.UTF_8),
                    new String(err.get(), StandardCharsets.UTF_8));
        }
    }

    private static void assertStatus(int expected, Invocation actual, String description) {
        if (expected != actual.status()) {
            throw new AssertionError("%s: expected status %d, got %d; stdout <%s>; stderr <%s>"
                    .formatted(description, expected, actual.status(), actual.out(), actual.err()));
        }
    }

    private static void assertContains(String actual, String expected, String description) {
        if (!actual.contains(expected)) {
            throw new AssertionError("%s: expected <%s> in <%s>".formatted(description, expected, actual));
        }
    }

    private static void assertBytes(byte[] expected, byte[] actual, String description) {
        if (!java.util.Arrays.equals(expected, actual)) throw new AssertionError(description);
    }

    private static void deleteTree(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }
}
