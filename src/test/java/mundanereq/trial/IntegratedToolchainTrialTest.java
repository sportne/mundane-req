package mundanereq.trial;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Reproducible branch-to-baseline trial for the three native tools. */
public final class IntegratedToolchainTrialTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();
    private static final Path EXPERIMENT = ROOT.resolve("experiments/0010-integrated-toolchain-trial");
    private static final Map<String, String> GIT_ENV = Map.ofEntries(
            Map.entry("GIT_AUTHOR_NAME", "Mundane Req Trial"),
            Map.entry("GIT_AUTHOR_EMAIL", "trial@example.invalid"),
            Map.entry("GIT_COMMITTER_NAME", "Mundane Req Trial"),
            Map.entry("GIT_COMMITTER_EMAIL", "trial@example.invalid"),
            Map.entry("GIT_AUTHOR_DATE", "2026-08-29T12:00:00Z"),
            Map.entry("GIT_COMMITTER_DATE", "2026-08-29T12:00:00Z"),
            Map.entry("GIT_CONFIG_NOSYSTEM", "1"),
            Map.entry("GIT_CONFIG_GLOBAL", "/dev/null"),
            Map.entry("GIT_DEFAULT_HASH", "sha1"));

    private record Invocation(int status, String out, String err) {}

    private record TrialResult(String refs, String traceOutput) {}

    private IntegratedToolchainTrialTest() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 3) throw new AssertionError("expected formatter, validator, and trace paths");
        Path formatter = executable(arguments[0]);
        Path validator = executable(arguments[1]);
        Path trace = executable(arguments[2]);
        Path temporary = Files.createTempDirectory("mundanereq-integrated-trial-");
        try {
            Path reduced = temporary.resolve("without-trace");
            Files.createDirectories(reduced);
            Path isolatedFormatter = copy(formatter, reduced);
            Path isolatedValidator = copy(validator, reduced);
            if (Files.exists(reduced.resolve(trace.getFileName()))) throw new AssertionError("trace unexpectedly installed");

            TrialResult complete = runWorkflow(temporary.resolve("complete-repository"), formatter, validator, trace);
            TrialResult degraded = runWorkflow(
                    temporary.resolve("without-trace-repository"), isolatedFormatter, isolatedValidator, null);
            assertEquals(complete.refs(), degraded.refs(), "deterministic refs with isolated Git configuration");
            assertContains(complete.traceOutput(),
                    "2 SW-COMMAND-001 -> SYS-ACTUATE-001 -> OPS-RESPONSE-001", "new impact path");
            assertEquals("", degraded.traceOutput(), "trace output when trace tool is absent");
        } finally {
            deleteTree(temporary);
        }
        System.out.println("PASS two deterministic branch-to-baseline workflows with and without trace");
    }

    private static TrialResult runWorkflow(Path repository, Path formatter, Path validator, Path trace)
            throws Exception {
        Path requirements = repository.resolve("requirements/requirements.mreq");
        Files.createDirectories(requirements.getParent());
        Files.copy(EXPERIMENT.resolve("baseline/requirements.mreq"), requirements);

        git(repository, "init", "-b", "main");
        git(repository, "add", "requirements");
        git(repository, "commit", "-m", "Establish response timing baseline");
        git(repository, "tag", "-a", "trial-baseline-1", "-m", "Trial baseline 1");
        git(repository, "checkout", "-b", "change/timing-budget");

        String proposed = Files.readString(EXPERIMENT.resolve("proposed/requirements.mreq"), StandardCharsets.UTF_8);
        Files.writeString(requirements, proposed.replace("\n", "\r\n"), StandardCharsets.UTF_8);
        assertStatus(0, invoke(repository, formatter, "--write", requirements.toString()), "formatter write");
        assertBytes(Files.readAllBytes(EXPERIMENT.resolve("proposed/requirements.mreq")),
                Files.readAllBytes(requirements), "formatted proposal");
        assertStatus(0, invoke(repository, validator, requirements.getParent().toString()), "branch validation");
        Invocation branchTrace = trace == null
                ? new Invocation(0, "", "")
                : invoke(repository, trace, "impact", "OPS-RESPONSE-001", requirements.getParent().toString());
        assertStatus(0, branchTrace, "branch trace");

        assertReviewSurface(git(repository, "diff", "--", "requirements").out());
        git(repository, "add", "requirements");
        git(repository, "commit", "-m", "Tighten and decompose response timing budget");
        assertReviewSurface(git(repository, "diff", "main...change/timing-budget", "--", "requirements").out());

        git(repository, "checkout", "main");
        git(repository, "merge", "--no-ff", "change/timing-budget", "-m", "Merge timing budget change");
        git(repository, "tag", "-a", "trial-baseline-2", "-m", "Trial baseline 2");
        git(repository, "checkout", "trial-baseline-2");

        assertStatus(0, invoke(repository, formatter, "--check", requirements.getParent().toString()),
                "tagged formatting check");
        assertStatus(0, invoke(repository, validator, requirements.getParent().toString()), "tagged validation");
        if (trace != null) {
            Invocation rebuilt = invoke(repository, trace, "impact", "OPS-RESPONSE-001", requirements.getParent().toString());
            assertInvocation(branchTrace, rebuilt, "tagged trace reconstruction");
            Path report = repository.resolve("impact.txt");
            Files.writeString(report, rebuilt.out(), StandardCharsets.UTF_8);
            Files.delete(report);
            assertInvocation(rebuilt,
                    invoke(repository, trace, "impact", "OPS-RESPONSE-001", requirements.getParent().toString()),
                    "deleted trace-report reconstruction");
        }

        assertEquals("", git(repository, "status", "--porcelain").out(), "clean tagged source");
        assertContains(git(repository, "tag", "-n").out(), "trial-baseline-1", "first annotated baseline");
        assertContains(git(repository, "tag", "-n").out(), "trial-baseline-2", "second annotated baseline");
        return new TrialResult(git(repository, "show-ref", "--heads", "--tags").out(), branchTrace.out());
    }

    private static void assertReviewSurface(String diff) {
        assertContains(diff, "within 250 ms", "old end-to-end budget");
        assertContains(diff, "within 100 ms", "new end-to-end budget");
        assertContains(diff, "+requirement SYS-ACTUATE-001", "new system requirement");
        assertContains(diff, "+requirement SW-COMMAND-001", "new software requirement");
        assertContains(diff, "+decomposes: SYS-ACTUATE-001", "new relationship");
    }

    private static Path executable(String value) {
        Path path = Path.of(value).toAbsolutePath().normalize();
        if (!Files.isExecutable(path)) throw new AssertionError("not executable: " + path);
        return path;
    }

    private static Path copy(Path source, Path directory) throws IOException {
        Path target = directory.resolve(source.getFileName());
        Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
        if (!target.toFile().setExecutable(true, true)) throw new IOException("cannot mark executable: " + target);
        return target;
    }

    private static Invocation git(Path directory, String... arguments) throws Exception {
        List<String> isolated = new ArrayList<>(List.of(
                "-c", "commit.gpgSign=false",
                "-c", "tag.gpgSign=false",
                "-c", "core.autocrlf=false",
                "-c", "core.hooksPath=/dev/null"));
        isolated.addAll(List.of(arguments));
        Invocation result = invoke(directory, Path.of("git"), isolated.toArray(String[]::new));
        assertStatus(0, result, "git " + String.join(" ", arguments));
        return result;
    }

    private static Invocation invoke(Path directory, Path executable, String... arguments) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(executable.toString());
        command.addAll(List.of(arguments));
        ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile());
        builder.environment().putAll(GIT_ENV);
        Process process = builder.start();
        try (var readers = Executors.newFixedThreadPool(2)) {
            Future<byte[]> out = readers.submit(() -> process.getInputStream().readAllBytes());
            Future<byte[]> err = readers.submit(() -> process.getErrorStream().readAllBytes());
            int status = process.waitFor();
            return new Invocation(status, new String(out.get(), StandardCharsets.UTF_8),
                    new String(err.get(), StandardCharsets.UTF_8));
        }
    }

    private static void assertStatus(int expected, Invocation actual, String description) {
        if (expected != actual.status()) throw new AssertionError(
                "%s: expected %d, got %d; stdout <%s>; stderr <%s>".formatted(
                        description, expected, actual.status(), actual.out(), actual.err()));
    }

    private static void assertInvocation(Invocation expected, Invocation actual, String description) {
        assertEquals(expected.status(), actual.status(), description + " status");
        assertEquals(expected.out(), actual.out(), description + " stdout");
        assertEquals(expected.err(), actual.err(), description + " stderr");
    }

    private static void assertContains(String actual, String expected, String description) {
        if (!actual.contains(expected)) throw new AssertionError(
                "%s: expected <%s> in <%s>".formatted(description, expected, actual));
    }

    private static void assertBytes(byte[] expected, byte[] actual, String description) {
        if (!java.util.Arrays.equals(expected, actual)) throw new AssertionError(description);
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!expected.equals(actual)) throw new AssertionError(
                "%s: expected <%s>, got <%s>".formatted(description, expected, actual));
    }

    private static void deleteTree(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }
}
