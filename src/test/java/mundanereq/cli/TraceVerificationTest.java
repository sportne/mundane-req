package mundanereq.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** JVM/native verification of trace graph and authoring-workflow behavior. */
public final class TraceVerificationTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();
    private static final Path EXPERIMENT = ROOT.resolve("experiments/0009-trace-workflows");

    private record Query(String operation, String id, String expectedFile) {}

    private record Invocation(int status, byte[] out, byte[] err) {
        String outText() {
            return new String(out, StandardCharsets.UTF_8);
        }

        String errText() {
            return new String(err, StandardCharsets.UTF_8);
        }
    }

    private TraceVerificationTest() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 1) throw new AssertionError("expected the native trace path");
        Path nativeTrace = Path.of(arguments[0]).toAbsolutePath().normalize();
        if (!Files.isExecutable(nativeTrace)) throw new AssertionError("native trace is not executable");

        trialIdentityAgrees(nativeTrace);
        graphOperationsAgree(nativeTrace);
        failuresAgree(nativeTrace);
        maintainedCorporaAgree(nativeTrace);
        authoringWorkflowsAgree(nativeTrace);
        System.out.println("PASS complete trace JVM/native verification");
    }

    private static void trialIdentityAgrees(Path nativeTrace) throws Exception {
        Invocation version = assertAgreement(nativeTrace, 0, "--version");
        assertEquals(
                "mundanereq-trace trial-0.1; source contract mundanereq-source-0.2\n",
                version.outText(),
                "published trial version");
        assertEquals("", version.errText(), "published trial version standard error");
        System.out.println("PASS published trace trial identity");
    }

    private static void graphOperationsAgree(Path nativeTrace) throws Exception {
        List<Query> queries = List.of(
                new Query("parents", "D", "parents-D.txt"),
                new Query("children", "TOP", "children-TOP.txt"),
                new Query("higher", "D", "higher-D.txt"),
                new Query("impact", "TOP", "impact-TOP.txt"),
                new Query("higher", "SCOPE", "higher-SCOPE.txt"),
                new Query("impact", "CYCLE-A", "impact-CYCLE-A.txt"),
                new Query("parents", "SELF", "parents-SELF.txt"),
                new Query("children", "DISCONNECTED", "children-DISCONNECTED.txt"));
        Path oneFile = EXPERIMENT.resolve("graph-one-file.mreq");
        Path split = EXPERIMENT.resolve("graph-split");
        Path partA = split.resolve("part-a.mreq");
        Path partB = split.resolve("part-b.mreq");

        for (Query query : queries) {
            String expected = Files.readString(
                    EXPERIMENT.resolve("expected").resolve(query.expectedFile()), StandardCharsets.UTF_8);
            Invocation one = assertAgreement(
                    nativeTrace, 0, query.operation(), query.id(), oneFile.toString());
            assertEquals(expected, one.outText(), "independent expected output for " + query.expectedFile());
            assertEquals("", one.errText(), "successful query standard error");

            Invocation directory = assertAgreement(
                    nativeTrace, 0, query.operation(), query.id(), split.toString());
            Invocation forward = assertAgreement(
                    nativeTrace, 0, query.operation(), query.id(), partA.toString(), partB.toString());
            Invocation reverse = assertAgreement(
                    nativeTrace, 0, query.operation(), query.id(), partB.toString(), partA.toString());
            assertInvocationEquals(one, directory, "one-file/directory layout invariance");
            assertInvocationEquals(one, forward, "one-file/explicit layout invariance");
            assertInvocationEquals(one, reverse, "input-order invariance");
        }
        System.out.println("PASS graph operations, layouts, ordering, ties, disconnection, and cycles");
    }

    private static void failuresAgree(Path nativeTrace) throws Exception {
        Path graph = EXPERIMENT.resolve("graph-one-file.mreq");
        Invocation missing = assertAgreement(nativeTrace, 2, "parents", "MISSING", graph.toString());
        assertContains(missing.errText(), "missing-requirement", "missing-query diagnostic");

        Path temporary = Files.createTempDirectory("mundanereq-trace-invalid-");
        try {
            Path invalid = temporary.resolve("invalid.mreq");
            Files.writeString(invalid, "outside a record\n", StandardCharsets.UTF_8);
            Invocation result = assertAgreement(nativeTrace, 2, "parents", "TOP", invalid.toString());
            assertContains(result.errText(), "content-outside-record", "invalid-source diagnostic");
        } finally {
            deleteTemporaryTree(temporary);
        }
        System.out.println("PASS missing-query and invalid-source behavior");
    }

    private static void maintainedCorporaAgree(Path nativeTrace) throws Exception {
        Invocation uas = assertAgreement(
                nativeTrace,
                0,
                "impact",
                "OPS-001",
                ROOT.resolve("experiments/0003-sustained-authoring/requirements").toString());
        assertEquals(
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
                uas.outText(),
                "sustained UAS impact output");

        Invocation fret = assertAgreement(
                nativeTrace,
                0,
                "impact",
                "LPC_TB_STAY_ON_NEXT",
                ROOT.resolve("experiments/0004-transferability/requirements").toString());
        assertEquals(
                "Lower-level impact paths to LPC_TB_STAY_ON_NEXT:\n(none)\n",
                fret.outText(),
                "transferred FRET impact output");
        System.out.println("PASS maintained UAS and transferred FRET corpus traces");
    }

    private static void authoringWorkflowsAgree(Path nativeTrace) throws Exception {
        Path temporary = Files.createTempDirectory("mundanereq-trace-workflows-");
        try {
            addition(nativeTrace, temporary);
            splitAndRetarget(nativeTrace, temporary);
            coordinatedContentChange(nativeTrace, temporary);
            retirement(nativeTrace, temporary);
        } finally {
            deleteTemporaryTree(temporary);
        }
        System.out.println("PASS addition, split/retarget, coordinated-change, and retirement workflows");
    }

    private static void addition(Path nativeTrace, Path root) throws Exception {
        Path source = root.resolve("addition.mreq");
        Files.writeString(source, requirement("PARENT", "Parent", "The parent shall hold.", null), StandardCharsets.UTF_8);
        assertOutput(nativeTrace, "Direct lower-level requirements for PARENT:\n(none)\n", "children", "PARENT", source);
        Files.writeString(
                source,
                requirement("PARENT", "Parent", "The parent shall hold.", null)
                        + requirement("NEW", "New", "The new requirement shall hold.", "PARENT"),
                StandardCharsets.UTF_8);
        assertOutput(nativeTrace, "Direct lower-level requirements for PARENT:\nNEW\n", "children", "PARENT", source);
    }

    private static void splitAndRetarget(Path nativeTrace, Path root) throws Exception {
        Path source = root.resolve("split.mreq");
        Files.writeString(
                source,
                requirement("PARENT", "Parent", "The parent shall hold.", null)
                        + requirement("LEAF", "Leaf", "The leaf shall hold.", "PARENT"),
                StandardCharsets.UTF_8);
        assertOutput(
                nativeTrace,
                "Lower-level impact paths to PARENT:\n1 LEAF -> PARENT\n",
                "impact",
                "PARENT",
                source);
        Files.writeString(
                source,
                requirement("PARENT", "Parent", "The parent shall hold.", null)
                        + requirement("NEW", "New", "The new requirement shall hold.", "PARENT")
                        + requirement("LEAF", "Leaf", "The leaf shall hold.", "NEW"),
                StandardCharsets.UTF_8);
        assertOutput(
                nativeTrace,
                "Lower-level impact paths to PARENT:\n"
                        + "1 NEW -> PARENT\n"
                        + "2 LEAF -> NEW -> PARENT\n",
                "impact",
                "PARENT",
                source);
    }

    private static void coordinatedContentChange(Path nativeTrace, Path root) throws Exception {
        Path source = root.resolve("content-change.mreq");
        Files.writeString(
                source,
                requirement("PARENT", "Parent", "The system shall retain data for one hour.", null)
                        + requirement("LEAF", "Leaf", "The subsystem shall retain logs for one hour.", "PARENT"),
                StandardCharsets.UTF_8);
        Invocation before = assertAgreement(nativeTrace, 0, "impact", "PARENT", source.toString());
        Files.writeString(
                source,
                requirement("PARENT", "Parent", "The system shall retain data for two hours.", null)
                        + requirement("LEAF", "Leaf", "The subsystem shall retain logs for two hours.", "PARENT"),
                StandardCharsets.UTF_8);
        Invocation after = assertAgreement(nativeTrace, 0, "impact", "PARENT", source.toString());
        assertInvocationEquals(before, after, "statement-only change leaves structural trace unchanged");
    }

    private static void retirement(Path nativeTrace, Path root) throws Exception {
        Path source = root.resolve("retirement.mreq");
        Files.writeString(
                source,
                requirement("PARENT", "Parent", "The parent shall hold.", null)
                        + requirement("LEAF", "Leaf", "The leaf shall hold.", "PARENT"),
                StandardCharsets.UTF_8);
        assertOutput(
                nativeTrace,
                "Lower-level impact paths to PARENT:\n1 LEAF -> PARENT\n",
                "impact",
                "PARENT",
                source);
        Files.writeString(source, requirement("PARENT", "Parent", "The parent shall hold.", null), StandardCharsets.UTF_8);
        assertOutput(nativeTrace, "Lower-level impact paths to PARENT:\n(none)\n", "impact", "PARENT", source);
    }

    private static String requirement(String id, String title, String statement, String parent) {
        return "requirement " + id + "\n"
                + "title: " + title + "\n"
                + "statement:\n"
                + "  " + statement + "\n"
                + (parent == null ? "" : "decomposes: " + parent + "\n")
                + "end requirement\n\n";
    }

    private static void assertOutput(
            Path nativeTrace, String expected, String operation, String id, Path source) throws Exception {
        Invocation result = assertAgreement(nativeTrace, 0, operation, id, source.toString());
        assertEquals(expected, result.outText(), operation + " workflow output");
        assertEquals("", result.errText(), operation + " workflow standard error");
    }

    private static Invocation assertAgreement(
            Path nativeTrace, int expectedStatus, String... arguments) throws Exception {
        Invocation jvm = invokeJvm(arguments);
        Invocation nativeResult = invokeNative(nativeTrace, arguments);
        assertEquals(expectedStatus, jvm.status(), "JVM status for " + List.of(arguments));
        assertInvocationEquals(jvm, nativeResult, "JVM/native byte agreement for " + List.of(arguments));
        return jvm;
    }

    private static Invocation invokeJvm(String... arguments) {
        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream standardError = new ByteArrayOutputStream();
        int status;
        try (PrintStream out = new PrintStream(standardOutput, true, StandardCharsets.UTF_8);
                PrintStream err = new PrintStream(standardError, true, StandardCharsets.UTF_8)) {
            status = TraceMain.run(arguments, out, err);
        }
        return new Invocation(
                status,
                standardOutput.toByteArray(),
                standardError.toByteArray());
    }

    private static Invocation invokeNative(Path nativeTrace, String... arguments) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(nativeTrace.toString());
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).start();
        try (var readers = Executors.newFixedThreadPool(2)) {
            Future<byte[]> standardOutput = readers.submit(() -> process.getInputStream().readAllBytes());
            Future<byte[]> standardError = readers.submit(() -> process.getErrorStream().readAllBytes());
            int status = process.waitFor();
            return new Invocation(
                    status,
                    standardOutput.get(),
                    standardError.get());
        }
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

    private static void assertInvocationEquals(Invocation expected, Invocation actual, String description) {
        assertEquals(expected.status(), actual.status(), description + " status");
        if (!Arrays.equals(expected.out(), actual.out())) {
            throw new AssertionError("%s stdout bytes: expected <%s> but was <%s>"
                    .formatted(description, expected.outText(), actual.outText()));
        }
        if (!Arrays.equals(expected.err(), actual.err())) {
            throw new AssertionError("%s stderr bytes: expected <%s> but was <%s>"
                    .formatted(description, expected.errText(), actual.errText()));
        }
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("%s: expected <%s> but was <%s>".formatted(description, expected, actual));
        }
    }
}
