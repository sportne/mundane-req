package mundanereq;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/** Dependency-free tests for the bounded ReqIF profile. */
public final class ReqifProbeTest {
    private static final Path REPOSITORY_ROOT = Path.of("../..").toAbsolutePath().normalize();
    private static final Path CONFORMANCE_01 = REPOSITORY_ROOT.resolve("conformance/0.1/valid");
    private static final Path CONFORMANCE_02 = REPOSITORY_ROOT.resolve("conformance/0.2/valid");
    private static final Path UAS = REPOSITORY_ROOT.resolve("experiments/0003-sustained-authoring/requirements");
    private static final Path FRET = REPOSITORY_ROOT.resolve("experiments/0004-transferability/requirements");

    private record TestCase(String name, CheckedRunnable action) {}

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }

    private ReqifProbeTest() {}

    public static void main(String[] args) throws Exception {
        List<TestCase> tests = List.of(
                new TestCase("conformance roundtrip", ReqifProbeTest::conformanceRoundtrip),
                new TestCase("UAS rich-content roundtrip", ReqifProbeTest::uasRoundtrip),
                new TestCase("FRET transfer roundtrip", ReqifProbeTest::fretRoundtrip),
                new TestCase("deterministic export and profile rejection", ReqifProbeTest::profileBehavior),
                new TestCase("command interface", ReqifProbeTest::commandInterface));
        int passed = 0;
        for (TestCase test : tests) {
            try {
                test.action().run();
                System.out.println("PASS " + test.name());
                passed++;
            } catch (Throwable failure) {
                System.err.println("FAIL " + test.name() + ": " + failure.getMessage());
                failure.printStackTrace(System.err);
                System.exit(1);
            }
        }
        System.out.printf("Passed %d ReqIF experiment tests.%n", passed);
    }

    private static void conformanceRoundtrip() throws Exception {
        Probe.Result source = valid(CONFORMANCE_01);
        Probe.Result commented = valid(CONFORMANCE_02);
        assertEquals(3, source.requirements().size(), "conformance requirement count");
        assertEquals(3, relationshipCount(source.requirements()), "conformance relationship count");
        assertEquals(
                Probe.normalizedInventory(source.requirements()),
                Probe.normalizedInventory(commented.requirements()),
                "0.1 and commented 0.2 semantic inventories");
        assertRoundtrip(source.requirements(), "conformance");
        assertRoundtrip(commented.requirements(), "commented conformance");
        String commentedXml = new String(
                ReqifProbe.exportReqif(commented.requirements(), ReqifProbe.FIXED_ROUNDTRIP_TIME),
                StandardCharsets.UTF_8);
        assertTrue(!commentedXml.contains("File-level author comment"), "comments absent from ReqIF");
    }

    private static void uasRoundtrip() throws Exception {
        Probe.Result source = valid(UAS);
        assertEquals(21, source.requirements().size(), "UAS requirement count");
        assertEquals(25, relationshipCount(source.requirements()), "UAS relationship count");
        assertRoundtrip(source.requirements(), "UAS");
        Probe.Requirement sys006 = requirement(source.requirements(), "SYS-006");
        Probe.MathBlock sourceMath = (Probe.MathBlock) sys006.statement().stream()
                .filter(block -> block instanceof Probe.MathBlock)
                .findFirst()
                .orElseThrow();
        List<Probe.Requirement> imported = ReqifProbe.importReqif(
                ReqifProbe.exportReqif(source.requirements(), ReqifProbe.FIXED_ROUNDTRIP_TIME), "<test>");
        Probe.MathBlock importedMath = (Probe.MathBlock) requirement(imported, "SYS-006").statement().stream()
                .filter(block -> block instanceof Probe.MathBlock)
                .findFirst()
                .orElseThrow();
        assertEquals(sourceMath, importedMath, "LaTeX block");
    }

    private static void fretRoundtrip() throws Exception {
        Probe.Result source = valid(FRET);
        assertEquals(19, source.requirements().size(), "FRET requirement count");
        assertEquals(0, relationshipCount(source.requirements()), "FRET relationship count");
        assertRoundtrip(source.requirements(), "FRET");
    }

    private static void profileBehavior() throws Exception {
        Probe.Result source = valid(CONFORMANCE_01);
        byte[] first = ReqifProbe.exportReqif(source.requirements(), "2026-08-22T12:34:56Z");
        byte[] second = ReqifProbe.exportReqif(source.requirements(), "2026-08-22T12:34:56Z");
        assertEquals(new String(first, StandardCharsets.UTF_8), new String(second, StandardCharsets.UTF_8),
                "deterministic export");
        String xml = new String(first, StandardCharsets.UTF_8);
        assertTrue(xml.contains("http://www.omg.org/spec/ReqIF/20110401/reqif.xsd"), "ReqIF namespace");
        assertTrue(xml.contains("REQ-IF-VERSION>1.0</REQ-IF-VERSION>"), "schema header version");
        assertTrue(xml.contains("mundanereq-math-latex"), "math profile marker");
        assertTrue(xml.contains("MR_REQ_4348494C44"), "hexadecimal transport identifier");

        byte[] foreign = xml.replaceFirst(ReqifProbe.PROFILE, "foreign-profile").getBytes(StandardCharsets.UTF_8);
        assertThrows(
                () -> ReqifProbe.importReqif(foreign, "<foreign>"),
                "unsupported ReqIF tool profile",
                "foreign profile rejection");
        byte[] wrongVersion = xml.replace(
                        "REQ-IF-VERSION>1.0</REQ-IF-VERSION>",
                        "REQ-IF-VERSION>2.0</REQ-IF-VERSION>")
                .getBytes(StandardCharsets.UTF_8);
        assertThrows(
                () -> ReqifProbe.importReqif(wrongVersion, "<wrong-version>"),
                "unsupported ReqIF schema header version",
                "schema header rejection");
    }

    private static void commandInterface() throws Exception {
        Path temporary = Files.createTempDirectory("mundanereq-reqif-").toAbsolutePath().normalize();
        assertTrue(temporary.startsWith(Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath()),
                "safe temporary path");
        try {
            Path output = temporary.resolve("requirements.reqif");
            CommandResult exported = run("export", "2026-08-22T12:34:56Z", output.toString(), CONFORMANCE_01.toString());
            assertEquals(0, exported.status, "export status");
            assertTrue(exported.output.contains("Exported 3 requirements and 3 decomposition relationships"),
                    "export output");
            assertEquals("", exported.error, "export error");
            assertTrue(Files.size(output) > 0, "exported file");

            CommandResult imported = run("import-inventory", output.toString());
            assertEquals(0, imported.status, "import status");
            assertEquals(
                    Probe.normalizedInventory(valid(CONFORMANCE_01).requirements()),
                    imported.output,
                    "import inventory");

            CommandResult roundtrip = run("roundtrip", UAS.toString());
            assertEquals(0, roundtrip.status, "roundtrip status");
            assertEquals(
                    "Roundtrip preserved 21 requirements and 25 decomposition relationships.\n",
                    roundtrip.output,
                    "roundtrip output");
        } finally {
            deleteTemporaryTree(temporary);
        }
    }

    private static void assertRoundtrip(List<Probe.Requirement> source, String name) throws Exception {
        byte[] xml = ReqifProbe.exportReqif(source, ReqifProbe.FIXED_ROUNDTRIP_TIME);
        List<Probe.Requirement> imported = ReqifProbe.importReqif(xml, "<" + name + ">");
        assertEquals(
                Probe.normalizedInventory(source),
                Probe.normalizedInventory(imported),
                name + " semantic inventory");
    }

    private static Probe.Result valid(Path path) {
        Probe.Result result = Probe.interpretInputs(List.of(path));
        assertEquals(List.of(), result.diagnostics(), path + " diagnostics");
        return result;
    }

    private static Probe.Requirement requirement(List<Probe.Requirement> requirements, String id) {
        return requirements.stream().filter(value -> value.id().equals(id)).findFirst().orElseThrow();
    }

    private static int relationshipCount(List<Probe.Requirement> requirements) {
        return requirements.stream().mapToInt(value -> value.decomposes().size()).sum();
    }

    private static CommandResult run(String... args) throws Exception {
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(outputBytes, true, StandardCharsets.UTF_8);
                PrintStream error = new PrintStream(errorBytes, true, StandardCharsets.UTF_8)) {
            int status = ReqifProbe.run(args, output, error);
            return new CommandResult(
                    status,
                    outputBytes.toString(StandardCharsets.UTF_8),
                    errorBytes.toString(StandardCharsets.UTF_8));
        }
    }

    private static void assertThrows(
            CheckedRunnable action, String expectedMessage, String description) throws Exception {
        try {
            action.run();
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains(expectedMessage), description + " message");
            return;
        }
        throw new AssertionError(description + ": expected IllegalArgumentException");
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("%s: expected <%s> but was <%s>".formatted(description, expected, actual));
        }
    }

    private static void assertTrue(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }

    private static void deleteTemporaryTree(Path root) throws IOException {
        Path safeRoot = root.toAbsolutePath().normalize();
        Path temporaryRoot = Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize();
        if (!safeRoot.startsWith(temporaryRoot) || safeRoot.equals(temporaryRoot)) {
            throw new IOException("refusing to delete unsafe test path: " + safeRoot);
        }
        try (var paths = Files.walk(safeRoot)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }

    private record CommandResult(int status, String output, String error) {}
}
