package mundanereq.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Focused JVM checks for the validator process contract. */
public final class ValidatorMainTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();

    private ValidatorMainTest() {}

    public static void run() throws Exception {
        validatesExplicitSource();
        distinguishesInvalidSourceFromOperationalFailure();
        handlesInvocationAndIdentification();
    }

    private static void validatesExplicitSource() {
        Invocation invocation = invoke(ROOT.resolve("conformance/0.2/valid").toString());
        assertEquals(0, invocation.status(), "valid status");
        assertContains(invocation.out(), "Validated 3 requirements and 3 decomposition relationships from 1 file", "summary");
        assertContains(invocation.out(), ValidatorMain.SOURCE_CONTRACT, "source contract");
        assertEquals("", invocation.err(), "valid stderr");
    }

    private static void distinguishesInvalidSourceFromOperationalFailure() throws Exception {
        Path invalid = ROOT.resolve("conformance/0.2/invalid/supplementary-scalar-column.mreq");
        Invocation sourceFailure = invoke(invalid.toString());
        assertEquals(1, sourceFailure.status(), "invalid-source status");
        assertContains(sourceFailure.err(), ":2:10: control-character:", "source-positioned diagnostic");
        assertEquals("", sourceFailure.out(), "invalid-source stdout");

        Invocation unavailable = invoke(ROOT.resolve("does-not-exist.mreq").toString());
        assertEquals(2, unavailable.status(), "unavailable-input status");
        assertContains(unavailable.err(), ":1:1: input-unavailable:", "input diagnostic");

        Path emptyDirectory = Files.createTempDirectory("mundanereq-validator-empty-");
        try {
            Invocation empty = invoke(emptyDirectory.toString());
            assertEquals(2, empty.status(), "empty-selection status");
            assertContains(empty.err(), "no-source-files", "empty-selection diagnostic");
        } finally {
            Files.deleteIfExists(emptyDirectory);
        }
    }

    private static void handlesInvocationAndIdentification() {
        Invocation noArguments = invoke();
        assertEquals(2, noArguments.status(), "no-argument status");
        assertContains(noArguments.err(), "Usage:", "no-argument usage");

        Invocation unknown = invoke("--inventory");
        assertEquals(2, unknown.status(), "unknown-option status");
        assertContains(unknown.err(), "unknown option", "unknown-option message");

        Invocation help = invoke("--help");
        assertEquals(0, help.status(), "help status");
        assertContains(help.out(), "Usage:", "help output");
        assertEquals("", help.err(), "help stderr");

        Invocation version = invoke("--version");
        assertEquals(0, version.status(), "version status");
        assertContains(version.out(), ValidatorMain.SOURCE_CONTRACT, "version contract");
    }

    private static Invocation invoke(String... arguments) {
        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream standardError = new ByteArrayOutputStream();
        int status;
        try (PrintStream out = new PrintStream(standardOutput, true, StandardCharsets.UTF_8);
                PrintStream err = new PrintStream(standardError, true, StandardCharsets.UTF_8)) {
            status = ValidatorMain.run(arguments, out, err);
        }
        return new Invocation(
                status,
                standardOutput.toString(StandardCharsets.UTF_8),
                standardError.toString(StandardCharsets.UTF_8));
    }

    private static void assertContains(String actual, String expected, String description) {
        if (!actual.contains(expected)) {
            throw new AssertionError("%s: expected <%s> in <%s>".formatted(description, expected, actual));
        }
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("%s: expected <%s> but was <%s>".formatted(description, expected, actual));
        }
    }

    private record Invocation(int status, String out, String err) {}
}
