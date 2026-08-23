package mundanereq.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/** Focused JVM checks for formatter modes and all-input prevalidation. */
public final class FormatterMainTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();

    private FormatterMainTest() {}

    public static void run() throws Exception {
        formatsOneFileWithSourceSetContext();
        checksAndWritesExplicitSelections();
        rejectsInvalidSelectionsBeforeWriting();
        reportsStandardOutputFailure();
        leavesSourceUntouchedWhenReplacementCannotStart();
        handlesInvocationAndIdentification();
    }

    private static void formatsOneFileWithSourceSetContext() throws Exception {
        Path temporary = Files.createTempDirectory("mundanereq-format-context-");
        try {
            Path child = temporary.resolve("child.mreq");
            Path parent = temporary.resolve("parent.mreq");
            Files.writeString(
                    child,
                    "requirement CHILD\r\ntitle: Child\r\nstatement:\r\n  Required.\r\n"
                            + "decomposes: PARENT\r\nend requirement\r\n",
                    StandardCharsets.UTF_8);
            Files.writeString(
                    parent,
                    "requirement PARENT\ntitle: Parent\nstatement:\n  Required.\nend requirement\n",
                    StandardCharsets.UTF_8);

            Invocation withoutContext = invoke(child.toString());
            assertEquals(2, withoutContext.status(), "standalone dangling-reference status");
            assertContains(withoutContext.err(), "dangling-reference", "standalone diagnostic");

            Invocation withContext = invoke(child.toString(), parent.toString());
            assertEquals(0, withContext.status(), "context source-set status");
            assertEquals("", withContext.err(), "context stderr");
            assertEquals(
                    Files.readString(child, StandardCharsets.UTF_8).replace("\r\n", "\n"),
                    withContext.out(),
                    "context stdout");
            assertContains(Files.readString(child, StandardCharsets.UTF_8), "\r\n", "source remains unchanged");
        } finally {
            deleteTemporaryTree(temporary);
        }
    }

    private static void checksAndWritesExplicitSelections() throws Exception {
        Path temporary = Files.createTempDirectory("mundanereq-format-write-");
        try {
            Path source = temporary.resolve("requirements.mreq");
            Path experiment = ROOT.resolve("experiments/0008-formatting-policy");
            Files.copy(experiment.resolve("input-varied-crlf.mreq"), source);
            Set<PosixFilePermission> expectedPermissions = Set.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.GROUP_READ);
            Files.setPosixFilePermissions(source, expectedPermissions);

            Invocation before = invoke("--check", temporary.toString());
            assertEquals(1, before.status(), "unformatted check status");
            assertContains(before.out(), "Needs formatting:", "check output");

            Invocation write = invoke("--write", temporary.toString());
            assertEquals(0, write.status(), "write status");
            assertContains(write.out(), "Formatted 1 file.", "write summary");
            assertArrayEquals(
                    Files.readAllBytes(experiment.resolve("candidate-conservative.mreq")),
                    Files.readAllBytes(source),
                    "written conservative output");
            assertEquals(expectedPermissions, Files.getPosixFilePermissions(source), "preserved POSIX permissions");

            Invocation after = invoke("--check", temporary.toString());
            assertEquals(0, after.status(), "formatted check status");
            assertEquals("", after.out(), "formatted check output");
        } finally {
            deleteTemporaryTree(temporary);
        }
    }

    private static void reportsStandardOutputFailure() throws Exception {
        Path source = ROOT.resolve("experiments/0008-formatting-policy/candidate-conservative.mreq");
        ByteArrayOutputStream standardError = new ByteArrayOutputStream();
        int status;
        try (PrintStream out = new PrintStream(new FailingOutputStream());
                PrintStream err = new PrintStream(standardError, true, StandardCharsets.UTF_8)) {
            status = FormatterMain.run(new String[] {source.toString()}, out, err);
        }
        assertEquals(2, status, "failed standard-output status");
        assertContains(
                standardError.toString(StandardCharsets.UTF_8),
                "output-failed",
                "failed standard-output diagnostic");
    }

    private static void leavesSourceUntouchedWhenReplacementCannotStart() throws Exception {
        Path temporary = Files.createTempDirectory("mundanereq-format-unwritable-");
        if (!Files.getFileStore(temporary).supportsFileAttributeView("posix")) {
            deleteTemporaryTree(temporary);
            return;
        }
        Set<PosixFilePermission> directoryPermissions = Files.getPosixFilePermissions(temporary);
        try {
            Path source = temporary.resolve("requirements.mreq");
            Files.copy(ROOT.resolve("experiments/0008-formatting-policy/input-varied-crlf.mreq"), source);
            byte[] original = Files.readAllBytes(source);
            Files.setPosixFilePermissions(
                    temporary,
                    Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE));

            Invocation write = invoke("--write", source.toString());
            assertEquals(2, write.status(), "unwritable-directory status");
            assertContains(write.err(), "write-failed", "unwritable-directory diagnostic");
            assertArrayEquals(original, Files.readAllBytes(source), "failed replacement source bytes");
            try (var entries = Files.list(temporary)) {
                assertEquals(List.of(source), entries.toList(), "temporary-file cleanup");
            }
        } finally {
            Files.setPosixFilePermissions(temporary, directoryPermissions);
            deleteTemporaryTree(temporary);
        }
    }

    private static void rejectsInvalidSelectionsBeforeWriting() throws Exception {
        Path temporary = Files.createTempDirectory("mundanereq-format-invalid-");
        try {
            Path valid = temporary.resolve("valid.mreq");
            Path invalid = temporary.resolve("invalid.mreq");
            Files.writeString(
                    valid,
                    "requirement VALID\r\ntitle: Valid\r\nstatement:\r\n  Required.\r\nend requirement\r\n",
                    StandardCharsets.UTF_8);
            Files.writeString(invalid, "content outside a record\n", StandardCharsets.UTF_8);
            byte[] original = Files.readAllBytes(valid);

            Invocation write = invoke("--write", temporary.toString());
            assertEquals(2, write.status(), "invalid write status");
            assertContains(write.err(), "content-outside-record", "invalid diagnostic");
            assertArrayEquals(original, Files.readAllBytes(valid), "valid peer remains untouched");
        } finally {
            deleteTemporaryTree(temporary);
        }
    }

    private static void handlesInvocationAndIdentification() {
        assertEquals(2, invoke().status(), "no-argument status");
        assertEquals(2, invoke("--unknown").status(), "unknown-option status");
        assertEquals(0, invoke("--help").status(), "help status");
        Invocation version = invoke("--version");
        assertEquals(0, version.status(), "version status");
        assertContains(version.out(), FormatterMain.SOURCE_CONTRACT, "source contract");
    }

    private static Invocation invoke(String... arguments) {
        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream standardError = new ByteArrayOutputStream();
        int status;
        try (PrintStream out = new PrintStream(standardOutput, true, StandardCharsets.UTF_8);
                PrintStream err = new PrintStream(standardError, true, StandardCharsets.UTF_8)) {
            status = FormatterMain.run(arguments, out, err);
        }
        return new Invocation(
                status,
                standardOutput.toString(StandardCharsets.UTF_8),
                standardError.toString(StandardCharsets.UTF_8));
    }

    private static void deleteTemporaryTree(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }

    private static void assertContains(String actual, String expected, String description) {
        if (!actual.contains(expected)) {
            throw new AssertionError("%s: expected <%s> in <%s>".formatted(description, expected, actual));
        }
    }

    private static void assertArrayEquals(byte[] expected, byte[] actual, String description) {
        if (!Arrays.equals(expected, actual)) throw new AssertionError(description + ": byte sequences differ");
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("%s: expected <%s> but was <%s>".formatted(description, expected, actual));
        }
    }

    private record Invocation(int status, String out, String err) {}

    private static final class FailingOutputStream extends OutputStream {
        @Override
        public void write(int value) throws IOException {
            throw new IOException("closed output");
        }
    }
}
