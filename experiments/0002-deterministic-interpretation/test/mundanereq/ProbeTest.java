package mundanereq;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Dependency-free experiment tests; run with assertions enabled. */
public final class ProbeTest {
    private static final Path REPOSITORY_ROOT = Path.of("../..").toAbsolutePath().normalize();
    private static final Path CANDIDATE_A = REPOSITORY_ROOT.resolve(
            "experiments/0001-source-representations/candidate-a-modules");
    private static final Path CANDIDATE_B = REPOSITORY_ROOT.resolve(
            "experiments/0001-source-representations/candidate-b-one-per-file/requirements");
    private static final Path CONFORMANCE_01 = REPOSITORY_ROOT.resolve("conformance/0.1/valid");
    private static final Path CONFORMANCE_02_ROOT = REPOSITORY_ROOT.resolve("conformance/0.2");
    private static final Path CONFORMANCE_02 = CONFORMANCE_02_ROOT.resolve("valid");

    private record TestCase(String name, CheckedRunnable action) {}

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }

    private ProbeTest() {}

    public static void main(String[] args) throws Exception {
        List<TestCase> tests = List.of(
                new TestCase("equivalent layouts", ProbeTest::equivalentLayouts),
                new TestCase("prose and math interpretation", ProbeTest::proseAndMathInterpretation),
                new TestCase("optional fields", ProbeTest::optionalFields),
                new TestCase("source comments", ProbeTest::sourceComments),
                new TestCase("line-ending equivalence", ProbeTest::lineEndingEquivalence),
                new TestCase("source discovery", ProbeTest::sourceDiscovery),
                new TestCase("explicit file discovery", ProbeTest::explicitFileDiscovery),
                new TestCase("physical source diagnostics", ProbeTest::physicalSourceDiagnostics),
                new TestCase("Unicode conformance edges", ProbeTest::unicodeConformanceEdges),
                new TestCase("record and field diagnostics", ProbeTest::recordAndFieldDiagnostics),
                new TestCase("math diagnostics", ProbeTest::mathDiagnostics),
                new TestCase("identity and relationship diagnostics", ProbeTest::identityAndRelationshipDiagnostics),
                new TestCase("incoming trace query", ProbeTest::incomingTraceQuery),
                new TestCase("command interface", ProbeTest::commandInterface));

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
        System.out.printf("Passed %d experiment tests.%n", passed);
    }

    private static void equivalentLayouts() {
        Probe.Result modules = Probe.interpretInputs(List.of(CANDIDATE_A));
        Probe.Result individual = Probe.interpretInputs(List.of(CANDIDATE_B));

        assertEquals(List.of(), modules.diagnostics(), "Candidate A diagnostics");
        assertEquals(List.of(), individual.diagnostics(), "Candidate B diagnostics");
        assertEquals(3, modules.fileCount(), "Candidate A file count");
        assertEquals(20, individual.fileCount(), "Candidate B file count");
        assertEquals(20, modules.requirements().size(), "Candidate A requirement count");
        assertEquals(20, individual.requirements().size(), "Candidate B requirement count");
        assertEquals(22, relationshipCount(modules), "Candidate A relationship count");
        assertEquals(22, relationshipCount(individual), "Candidate B relationship count");
        assertEquals(
                Probe.normalizedInventory(modules.requirements()),
                Probe.normalizedInventory(individual.requirements()),
                "semantic inventories");
    }

    private static void proseAndMathInterpretation() {
        Probe.Result result = Probe.interpretInputs(List.of(CANDIDATE_A));
        assertEquals(List.of(), result.diagnostics(), "fixture diagnostics");

        Probe.Requirement sys001 = requirement(result, "SYS-001");
        assertEquals(1, sys001.statement().size(), "SYS-001 statement block count");
        Probe.ProseBlock statement = (Probe.ProseBlock) sys001.statement().getFirst();
        assertEquals(
                "The mission-control system shall associate each registered vehicle with exactly one vehicle identifier that is unique within the mission repository.",
                statement.text(),
                "folded statement");

        Probe.Requirement sys006 = requirement(result, "SYS-006");
        Probe.MathBlock math = (Probe.MathBlock) sys006.statement().stream()
                .filter(block -> block instanceof Probe.MathBlock)
                .findFirst()
                .orElseThrow();
        assertEquals("latex", math.language(), "math language");
        assertTrue(math.payload().startsWith("k^\\ast = \\min"), "math payload start");
        assertTrue(
                math.payload().contains("\n\nT_{\\mathrm{loss}} = 2.0\\,\\mathrm{s},"),
                "math payload blank line");
        assertTrue(!math.payload().startsWith("  "), "math payload de-indentation");
    }

    private static void optionalFields() {
        Probe.Result result = interpretText(minimalRecord("REQ-001"));
        assertEquals(List.of(), result.diagnostics(), "minimal record diagnostics");
        Probe.Requirement requirement = result.requirements().getFirst();
        assertEquals(null, requirement.allocation(), "optional allocation");
        assertEquals(null, requirement.rationale(), "optional rationale");
        assertEquals(null, requirement.source(), "optional source");
        assertEquals(List.of(), requirement.decomposes(), "optional relationships");
    }

    private static void sourceComments() {
        String source = lines(
                "# file header author comment",
                "requirement REQ-001",
                "# comment after opener",
                "title: Hash # remains scalar content",
                "# comment between scalar fields",
                "allocation: System",
                "# comment before prose field",
                "statement:",
                "  The system shall preserve # as prose content.",
                "# comment after statement body",
                "rationale:",
                "  # at body indentation remains rationale content.",
                "# comment before source",
                "source: SOURCE#1",
                "# comment before relationship",
                "decomposes: REQ-002",
                "# comment before record end",
                "end requirement",
                "# this comment alone separates records",
                "requirement REQ-002",
                "title: Mathematical hash content",
                "statement:",
                "  math latex",
                "    x_{#} = 1",
                "  end math",
                "end requirement",
                "# trailing author comment");

        Probe.Result result = interpretText(source);
        assertEquals(List.of(), result.diagnostics(), "commented source diagnostics");
        assertEquals(2, result.requirements().size(), "commented requirement count");

        Probe.Requirement first = requirement(result, "REQ-001");
        assertEquals("Hash # remains scalar content", first.title(), "hash in scalar content");
        assertEquals("SOURCE#1", first.source(), "hash in source scalar");
        assertEquals(
                "The system shall preserve # as prose content.",
                ((Probe.ProseBlock) first.statement().getFirst()).text(),
                "hash in statement prose");
        assertEquals(
                "# at body indentation remains rationale content.",
                ((Probe.ProseBlock) first.rationale().getFirst()).text(),
                "hash in rationale prose");

        Probe.MathBlock math = (Probe.MathBlock) requirement(result, "REQ-002").statement().getFirst();
        assertEquals("x_{#} = 1", math.payload(), "hash in math payload");
        assertTrue(
                !Probe.normalizedInventory(result.requirements()).contains("author comment"),
                "comments absent from semantic inventory");

        Probe.Result conformance01 = Probe.interpretInputs(List.of(CONFORMANCE_01));
        Probe.Result conformance02 = Probe.interpretInputs(List.of(CONFORMANCE_02));
        assertEquals(List.of(), conformance01.diagnostics(), "0.1 conformance diagnostics");
        assertEquals(List.of(), conformance02.diagnostics(), "0.2 conformance diagnostics");
        assertEquals(
                Probe.normalizedInventory(conformance01.requirements()),
                Probe.normalizedInventory(conformance02.requirements()),
                "0.1 and commented 0.2 conformance inventories");

        assertHasCode(
                interpretText(lines(
                        "requirement REQ-001",
                        "title: Split prose",
                        "statement:",
                        "  First line.",
                        "# a comment cannot interrupt a prose body",
                        "  Second line.",
                        "end requirement")),
                "malformed-record");
        assertHasCode(
                interpretText(lines(
                        "requirement REQ-001",
                        "title: Empty statement",
                        "statement:",
                        "# a comment cannot replace body content",
                        "end requirement")),
                "empty-body");
        assertHasCode(interpretText(lines("# comment-only files remain invalid")), "empty-source-file");

        assertHasCode(
                Probe.interpretInputs(List.of(CONFORMANCE_02_ROOT.resolve("invalid/comment-only.mreq"))),
                "empty-source-file");
        assertHasCode(
                Probe.interpretInputs(List.of(CONFORMANCE_02_ROOT.resolve("invalid/comment-before-body.mreq"))),
                "empty-body");
        assertHasCode(
                Probe.interpretInputs(List.of(CONFORMANCE_02_ROOT.resolve("invalid/comment-splits-prose.mreq"))),
                "malformed-record");
        assertHasCode(
                Probe.interpretInputs(List.of(CONFORMANCE_02_ROOT.resolve("invalid/comment-splits-math.mreq"))),
                "unterminated-math");
    }

    private static void lineEndingEquivalence() {
        Probe.Result lf = interpretText(minimalRecord("REQ-001"));
        Probe.Result crlf = interpretBytes(minimalRecord("REQ-001").replace("\n", "\r\n").getBytes(StandardCharsets.UTF_8));
        assertEquals(List.of(), crlf.diagnostics(), "CRLF diagnostics");
        assertEquals(
                Probe.normalizedInventory(lf.requirements()),
                Probe.normalizedInventory(crlf.requirements()),
                "LF and CRLF interpretation");
    }

    private static void sourceDiscovery() throws Exception {
        Path temporary = Files.createTempDirectory("mundanereq-discovery-").toAbsolutePath().normalize();
        assertTrue(temporary.startsWith(Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath()), "safe temporary path");
        try {
            Path nested = Files.createDirectory(temporary.resolve("nested"));
            Path git = Files.createDirectory(temporary.resolve(".git"));
            Path selected = nested.resolve("selected.mreq");
            Files.writeString(selected, minimalRecord("REQ-001"));
            Files.writeString(nested.resolve("ignored.txt"), minimalRecord("REQ-TEXT"));
            Files.writeString(git.resolve("ignored.mreq"), minimalRecord("REQ-GIT"));
            Files.createSymbolicLink(temporary.resolve("alias.mreq"), selected);

            Probe.Result result = Probe.interpretInputs(List.of(temporary, selected));
            assertEquals(List.of(), result.diagnostics(), "discovery diagnostics");
            assertEquals(1, result.fileCount(), "selected and deduplicated file count");
            assertEquals("REQ-001", result.requirements().getFirst().id(), "selected requirement");
        } finally {
            deleteTemporaryTree(temporary);
        }
    }

    private static void explicitFileDiscovery() throws Exception {
        Path temporary = Files.createTempDirectory("mundanereq-explicit-").toAbsolutePath().normalize();
        assertTrue(temporary.startsWith(Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath()), "safe temporary path");
        try {
            Path explicit = temporary.resolve("source.txt");
            Files.writeString(explicit, minimalRecord("REQ-001"));
            assertHasCode(Probe.interpretInputs(List.of(temporary)), "no-source-files");
            Probe.Result result = Probe.interpretInputs(List.of(explicit));
            assertEquals(List.of(), result.diagnostics(), "explicit source diagnostics");
            assertEquals(1, result.fileCount(), "explicit file count");
        } finally {
            deleteTemporaryTree(temporary);
        }
    }

    private static void physicalSourceDiagnostics() {
        byte[] valid = minimalRecord("REQ-001").getBytes(StandardCharsets.UTF_8);
        List<Object[]> cases = List.of(
                new Object[] {concatenate(new byte[] {(byte) 0xef, (byte) 0xbb, (byte) 0xbf}, valid), "byte-order-mark"},
                new Object[] {new byte[] {'r', (byte) 0xc3, '(', '\n'}, "invalid-utf8"},
                new Object[] {concatenate("requirement R".getBytes(StandardCharsets.UTF_8), new byte[] {0, '\n'}), "nul-byte"},
                new Object[] {minimalRecord("REQ-001").replace("title:", "title:\t").getBytes(StandardCharsets.UTF_8), "tab"},
                new Object[] {minimalRecord("REQ-001").replace("Minimal", "Mini\u0007mal").getBytes(StandardCharsets.UTF_8), "control-character"},
                new Object[] {minimalRecord("REQ-001").replaceFirst("\n", "\r").getBytes(StandardCharsets.UTF_8), "line-ending"},
                new Object[] {Arrays.copyOf(valid, valid.length - 1), "final-line-ending"});

        for (Object[] testCase : cases) {
            assertHasCode(interpretBytes((byte[]) testCase[0]), (String) testCase[1]);
        }
    }

    private static void unicodeConformanceEdges() {
        Path invalid = CONFORMANCE_02_ROOT.resolve("invalid");

        Probe.Diagnostic c1 = diagnosticWithCode(
                Probe.interpretInputs(List.of(invalid.resolve("prohibited-c1-control.mreq"))),
                "control-character");
        assertEquals(2, c1.line(), "C1 control line");
        assertEquals(10, c1.column(), "C1 control column");

        Probe.Diagnostic leading = diagnosticWithCode(
                Probe.interpretInputs(List.of(invalid.resolve("leading-non-ascii-whitespace.mreq"))),
                "empty-or-padded-scalar");
        assertEquals(2, leading.line(), "leading whitespace line");
        assertEquals(8, leading.column(), "leading whitespace column");

        Probe.Diagnostic trailing = diagnosticWithCode(
                Probe.interpretInputs(List.of(invalid.resolve("trailing-non-ascii-whitespace.mreq"))),
                "empty-or-padded-scalar");
        assertEquals(2, trailing.line(), "trailing whitespace line");
        assertEquals(36, trailing.column(), "trailing whitespace column");

        Probe.Diagnostic supplementary = diagnosticWithCode(
                Probe.interpretInputs(List.of(invalid.resolve("supplementary-scalar-column.mreq"))),
                "control-character");
        assertEquals(2, supplementary.line(), "supplementary scalar line");
        assertEquals(10, supplementary.column(), "supplementary scalar column");
    }

    private static void recordAndFieldDiagnostics() {
        List<String[]> cases = List.of(
                new String[] {"outside\n" + minimalRecord("REQ-001"), "content-outside-record"},
                new String[] {"end requirement\n", "unmatched-record-end"},
                new String[] {minimalRecord("BAD ID"), "invalid-id"},
                new String[] {lines("requirement REQ-001", "end requirement"), "missing-field"},
                new String[] {lines("requirement REQ-001", "statement:", "  Text.", "end requirement"), "out-of-order-field"},
                new String[] {
                    lines("requirement REQ-001", "title: First", "title: Second", "statement:", "  Text.", "end requirement"),
                    "duplicate-field"},
                new String[] {
                    lines("requirement REQ-001", "title: Title", "statement:", "  Text.", "priority: high", "end requirement"),
                    "unknown-field"},
                new String[] {
                    lines("requirement REQ-001", "title: Title", "source: SRC", "statement:", "  Text.", "end requirement"),
                    "out-of-order-field"},
                new String[] {minimalRecord("REQ-001").replace("title: Minimal requirement", "title: "), "empty-or-padded-scalar"},
                new String[] {minimalRecord("REQ-001").replace("  The system shall provide the required behavior.\n", ""), "empty-body"},
                new String[] {minimalRecord("REQ-001").replace("  The system", " The system"), "body-indentation"},
                new String[] {
                    lines(
                            "requirement REQ-001",
                            "title: Title",
                            "statement:",
                            "  Text.",
                            "requirement REQ-002",
                            "title: Other",
                            "statement:",
                            "  Other text.",
                            "end requirement"),
                    "nested-record"},
                new String[] {
                    minimalRecord("REQ-001") + minimalRecord("REQ-002").stripLeading(),
                    "record-separation"},
                new String[] {
                    lines("requirement REQ-001", "title: Title", "statement:", "  Text."),
                    "missing-record-end"});

        for (String[] testCase : cases) assertHasCode(interpretText(testCase[0]), testCase[1]);
    }

    private static void mathDiagnostics() {
        List<String[]> cases = List.of(
                new String[] {mathRecord("  math latex", "  end math"), "empty-math"},
                new String[] {mathRecord("  math latex", "    x = 1"), "unterminated-math"},
                new String[] {mathRecord("  math latex", "   x = 1", "  end math"), "math-indentation"},
                new String[] {mathRecord("  end math"), "unexpected-math-end"});
        for (String[] testCase : cases) assertHasCode(interpretText(testCase[0]), testCase[1]);
    }

    private static void identityAndRelationshipDiagnostics() {
        Probe.Result duplicate = Probe.interpretSources(List.of(
                new Probe.Source("one.mreq", minimalRecord("REQ-001").getBytes(StandardCharsets.UTF_8)),
                new Probe.Source("two.mreq", minimalRecord("REQ-001").getBytes(StandardCharsets.UTF_8))));
        assertHasCode(duplicate, "duplicate-id");

        assertHasCode(
                interpretText(minimalRecord("REQ-001").replace(
                        "end requirement", "decomposes: MISSING\nend requirement")),
                "dangling-reference");

        String repeated = lines(
                "requirement REQ-001",
                "title: Child",
                "statement:",
                "  Text.",
                "decomposes: REQ-002",
                "decomposes: REQ-002",
                "end requirement",
                "",
                minimalRecord("REQ-002").stripTrailing());
        assertHasCode(interpretText(repeated), "duplicate-relationship");

        assertHasCode(
                interpretText(minimalRecord("REQ-001").replace(
                        "end requirement", "decomposes: BAD ID\nend requirement")),
                "invalid-reference-id");
    }

    private static void incomingTraceQuery() {
        String source = String.join("\n\n",
                record("TOP"),
                record("LEFT", "TOP"),
                record("RIGHT", "TOP"),
                record("LEAF", "LEFT", "RIGHT"),
                record("OTHER")) + "\n";
        Probe.Result result = interpretText(source);
        assertEquals(List.of(), result.diagnostics(), "trace fixture diagnostics");
        assertEquals(
                List.of(
                        new Probe.TraceHit("LEFT", List.of("LEFT", "TOP")),
                        new Probe.TraceHit("RIGHT", List.of("RIGHT", "TOP")),
                        new Probe.TraceHit("LEAF", List.of("LEAF", "LEFT", "TOP"))),
                Probe.incomingTrace(result.requirements(), "TOP"),
                "breadth-first shortest trace paths");
        assertEquals(
                "Incoming decomposition trace for TOP:\n"
                        + "1 LEFT -> TOP\n"
                        + "1 RIGHT -> TOP\n"
                        + "2 LEAF -> LEFT -> TOP\n",
                Probe.formatIncomingTrace("TOP", Probe.incomingTrace(result.requirements(), "TOP")),
                "trace output");
        assertEquals(
                "Incoming decomposition trace for OTHER:\n(none)\n",
                Probe.formatIncomingTrace("OTHER", Probe.incomingTrace(result.requirements(), "OTHER")),
                "empty trace output");
    }

    private static void commandInterface() throws Exception {
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(outputBytes, true, StandardCharsets.UTF_8);
                PrintStream error = new PrintStream(errorBytes, true, StandardCharsets.UTF_8)) {
            int status = Probe.run(new String[] {CANDIDATE_A.toString()}, output, error);
            assertEquals(0, status, "summary status");
            assertEquals(
                    "Parsed 20 requirements and 22 decomposition relationships from 3 files.\n",
                    outputBytes.toString(StandardCharsets.UTF_8),
                    "summary output");
            assertEquals("", errorBytes.toString(StandardCharsets.UTF_8), "summary errors");
        }

        outputBytes.reset();
        errorBytes.reset();
        try (PrintStream output = new PrintStream(outputBytes, true, StandardCharsets.UTF_8);
                PrintStream error = new PrintStream(errorBytes, true, StandardCharsets.UTF_8)) {
            int status = Probe.run(new String[] {"--incoming", "OPS-001", CANDIDATE_A.toString()}, output, error);
            assertEquals(0, status, "incoming query status");
            assertTrue(
                    outputBytes.toString(StandardCharsets.UTF_8).startsWith(
                            "Incoming decomposition trace for OPS-001:\n1 SYS-002 -> OPS-001\n"),
                    "incoming query output");
            assertEquals("", errorBytes.toString(StandardCharsets.UTF_8), "incoming query errors");
        }

        outputBytes.reset();
        errorBytes.reset();
        try (PrintStream output = new PrintStream(outputBytes, true, StandardCharsets.UTF_8);
                PrintStream error = new PrintStream(errorBytes, true, StandardCharsets.UTF_8)) {
            int status = Probe.run(new String[] {"--incoming", "MISSING", CANDIDATE_A.toString()}, output, error);
            assertEquals(1, status, "missing query target status");
            assertEquals("", outputBytes.toString(StandardCharsets.UTF_8), "missing query target output");
            assertEquals(
                    "query: requirement 'MISSING' does not exist in the selected source set\n",
                    errorBytes.toString(StandardCharsets.UTF_8),
                    "missing query target error");
        }

        outputBytes.reset();
        errorBytes.reset();
        try (PrintStream output = new PrintStream(outputBytes, true, StandardCharsets.UTF_8);
                PrintStream error = new PrintStream(errorBytes, true, StandardCharsets.UTF_8)) {
            int status = Probe.run(new String[] {"--inventory", CANDIDATE_B.toString()}, output, error);
            assertEquals(0, status, "inventory status");
            assertTrue(outputBytes.toString(StandardCharsets.UTF_8).startsWith("requirement GCA-001\n"), "inventory order");
        }

        outputBytes.reset();
        errorBytes.reset();
        try (PrintStream output = new PrintStream(outputBytes, true, StandardCharsets.UTF_8);
                PrintStream error = new PrintStream(errorBytes, true, StandardCharsets.UTF_8)) {
            Path missing = REPOSITORY_ROOT.resolve("does-not-exist.mreq");
            int status = Probe.run(new String[] {missing.toString()}, output, error);
            assertEquals(1, status, "diagnostic status");
            assertTrue(errorBytes.toString(StandardCharsets.UTF_8).contains("does-not-exist.mreq:1:1: input-unavailable:"), "diagnostic output");
        }
    }

    private static Probe.Result interpretText(String text) {
        return interpretBytes(text.getBytes(StandardCharsets.UTF_8));
    }

    private static Probe.Result interpretBytes(byte[] bytes) {
        return Probe.interpretSources(List.of(new Probe.Source("fixture.mreq", bytes)));
    }

    private static Probe.Requirement requirement(Probe.Result result, String id) {
        return result.requirements().stream()
                .filter(requirement -> requirement.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private static int relationshipCount(Probe.Result result) {
        return result.requirements().stream()
                .mapToInt(requirement -> requirement.decomposes().size())
                .sum();
    }

    private static String minimalRecord(String id) {
        return lines(
                "requirement " + id,
                "title: Minimal requirement",
                "statement:",
                "  The system shall provide the required behavior.",
                "end requirement");
    }

    private static String record(String id, String... parents) {
        List<String> lines = new ArrayList<>();
        lines.add("requirement " + id);
        lines.add("title: " + id);
        lines.add("statement:");
        lines.add("  The system shall provide the required behavior.");
        for (String parent : parents) lines.add("decomposes: " + parent);
        lines.add("end requirement");
        return String.join("\n", lines);
    }

    private static String mathRecord(String... body) {
        List<String> lines = new ArrayList<>();
        lines.add("requirement REQ-001");
        lines.add("title: Math");
        lines.add("statement:");
        lines.addAll(List.of(body));
        lines.add("end requirement");
        return String.join("\n", lines) + "\n";
    }

    private static String lines(String... values) {
        return String.join("\n", values) + "\n";
    }

    private static byte[] concatenate(byte[] left, byte[] right) {
        byte[] result = Arrays.copyOf(left, left.length + right.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return result;
    }

    private static void assertHasCode(Probe.Result result, String code) {
        Probe.Diagnostic diagnostic = diagnosticWithCode(result, code);
        assertTrue(!diagnostic.file().isEmpty(), code + " file");
        assertTrue(diagnostic.line() >= 1, code + " line");
        assertTrue(diagnostic.column() >= 1, code + " column");
        assertTrue(!diagnostic.message().isEmpty(), code + " message");
    }

    private static Probe.Diagnostic diagnosticWithCode(Probe.Result result, String code) {
        return result.diagnostics().stream()
                .filter(candidate -> candidate.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "expected %s; received %s".formatted(code, result.diagnostics().stream()
                                .map(Probe.Diagnostic::code)
                                .toList())));
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
}
