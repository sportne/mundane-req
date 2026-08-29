package mundanereq.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import mundanereq.trace.TraceAnalyzerTest;

/** Focused JVM checks for the trace command contract. */
public final class TraceMainTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();

    private TraceMainTest() {}

    public static void run() throws Exception {
        Path temporary = Files.createTempDirectory("mundanereq-trace-main-");
        try {
            Path graph = temporary.resolve("graph.mreq");
            Files.writeString(graph, TraceAnalyzerTest.graphSource(), StandardCharsets.UTF_8);
            rendersDirectAndTransitiveOperations(graph);
            handlesCyclesAndEmptyResults(graph);
            enforcesErrorPrecedence(graph, temporary);
            handlesIdentificationAndOutputFailure(graph);
            matchesMaintainedCorpusTraces();
        } finally {
            deleteTemporaryTree(temporary);
        }
    }

    private static void rendersDirectAndTransitiveOperations(Path graph) {
        assertInvocation(
                0,
                "Direct higher-level requirements for D:\nA-NEAR-D\nZ-NEAR-D\n",
                "",
                "parents",
                "D",
                graph.toString());
        assertInvocation(
                0,
                "Direct lower-level requirements for TOP:\nA-NEAR-TOP\nZ-NEAR-TOP\n",
                "",
                "children",
                "TOP",
                graph.toString());

        assertInvocation(
                0,
                "Higher-level decomposition paths from D:\n"
                        + "1 D -> A-NEAR-D\n"
                        + "1 D -> Z-NEAR-D\n"
                        + "2 D -> Z-NEAR-D -> A-NEAR-TOP\n"
                        + "2 D -> A-NEAR-D -> Z-NEAR-TOP\n"
                        + "3 D -> A-NEAR-D -> Z-NEAR-TOP -> TOP\n",
                "",
                "higher",
                "D",
                graph.toString());

        assertInvocation(
                0,
                "Lower-level impact paths to TOP:\n"
                        + "1 A-NEAR-TOP -> TOP\n"
                        + "1 Z-NEAR-TOP -> TOP\n"
                        + "2 A-NEAR-D -> Z-NEAR-TOP -> TOP\n"
                        + "2 Z-NEAR-D -> A-NEAR-TOP -> TOP\n"
                        + "3 D -> A-NEAR-D -> Z-NEAR-TOP -> TOP\n",
                "",
                "impact",
                "TOP",
                graph.toString());
    }

    private static void handlesCyclesAndEmptyResults(Path graph) {
        assertInvocation(
                0,
                "Higher-level decomposition paths from CYCLE-A:\n"
                        + "1 CYCLE-A -> CYCLE-B\n"
                        + "Cycle observed among: CYCLE-A CYCLE-B\n",
                "",
                "higher",
                "CYCLE-A",
                graph.toString());
        assertInvocation(
                0,
                "Higher-level decomposition paths from SELF:\n"
                        + "(none)\n"
                        + "Cycle observed among: SELF\n",
                "",
                "higher",
                "SELF",
                graph.toString());
        assertInvocation(
                0,
                "Direct lower-level requirements for DISCONNECTED:\n(none)\n",
                "",
                "children",
                "DISCONNECTED",
                graph.toString());
    }

    private static void enforcesErrorPrecedence(Path graph, Path temporary) throws Exception {
        assertEquals(2, invoke().status(), "omitted invocation status");
        assertEquals(2, invoke("unknown", "TOP", graph.toString()).status(), "unknown operation status");

        Invocation malformed = invoke("parents", "bad id", temporary.resolve("missing.mreq").toString());
        assertEquals(2, malformed.status(), "invalid query status");
        assertContains(malformed.err(), "invalid-query-id", "invalid query diagnostic");
        assertNotContains(malformed.err(), "input-unavailable", "invalid query precedes input selection");

        Path invalid = temporary.resolve("invalid.mreq");
        Files.writeString(invalid, "outside a record\n", StandardCharsets.UTF_8);
        Invocation invalidSource = invoke("parents", "MISSING", invalid.toString());
        assertContains(invalidSource.err(), "content-outside-record", "source diagnostic precedence");
        assertNotContains(invalidSource.err(), "missing-requirement", "lookup follows validation");

        Invocation missing = invoke("parents", "MISSING", graph.toString());
        assertContains(missing.err(), "missing-requirement", "missing query diagnostic");
        assertEquals("", missing.out(), "missing query stdout");
    }

    private static void handlesIdentificationAndOutputFailure(Path graph) {
        assertEquals(0, invoke("--help").status(), "help status");
        Invocation version = invoke("--version");
        assertEquals(0, version.status(), "version status");
        assertEquals(
                "mundanereq-trace trial-0.1; source contract mundanereq-source-0.2\n",
                version.out(),
                "version output");

        ByteArrayOutputStream standardError = new ByteArrayOutputStream();
        int status;
        try (PrintStream out = new PrintStream(new FailingOutputStream());
                PrintStream err = new PrintStream(standardError, true, StandardCharsets.UTF_8)) {
            status = TraceMain.run(new String[] {"parents", "D", graph.toString()}, out, err);
        }
        assertEquals(2, status, "output failure status");
        assertContains(
                standardError.toString(StandardCharsets.UTF_8),
                "output-failed",
                "output failure diagnostic");
    }

    private static void matchesMaintainedCorpusTraces() {
        Path uas = ROOT.resolve("experiments/0003-sustained-authoring/requirements");
        assertInvocation(
                0,
                "Lower-level impact paths to OPS-001:\n"
                        + "1 SYS-002 -> OPS-001\n"
                        + "1 SYS-003 -> OPS-001\n"
                        + "1 SYS-004 -> OPS-001\n"
                        + "1 SYS-005 -> OPS-001\n"
                        + "1 SYS-007 -> OPS-001\n"
                        + "1 SYS-011 -> OPS-001\n"
                        + "2 GCA-001 -> SYS-011 -> OPS-001\n"
                        + "2 GCA-003 -> SYS-007 -> OPS-001\n"
                        + "2 GCA-004 -> SYS-007 -> OPS-001\n"
                        + "2 VM-002 -> SYS-002 -> OPS-001\n",
                "",
                "impact",
                "OPS-001",
                uas.toString());

        Path fret = ROOT.resolve("experiments/0004-transferability/requirements");
        assertInvocation(
                0,
                "Lower-level impact paths to LPC_TB_STAY_ON_NEXT:\n(none)\n",
                "",
                "impact",
                "LPC_TB_STAY_ON_NEXT",
                fret.toString());
    }

    private static void assertInvocation(int status, String out, String err, String... arguments) {
        Invocation invocation = invoke(arguments);
        assertEquals(status, invocation.status(), "invocation status");
        assertEquals(out, invocation.out(), "invocation stdout");
        assertEquals(err, invocation.err(), "invocation stderr");
    }

    private static Invocation invoke(String... arguments) {
        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream standardError = new ByteArrayOutputStream();
        int status;
        try (PrintStream out = new PrintStream(standardOutput, true, StandardCharsets.UTF_8);
                PrintStream err = new PrintStream(standardError, true, StandardCharsets.UTF_8)) {
            status = TraceMain.run(arguments, out, err);
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

    private static void assertNotContains(String actual, String unexpected, String description) {
        if (actual.contains(unexpected)) {
            throw new AssertionError("%s: did not expect <%s> in <%s>".formatted(description, unexpected, actual));
        }
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
