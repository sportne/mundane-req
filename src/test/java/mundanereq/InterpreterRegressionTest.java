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

/** Ported dependency-free regression tests; run with assertions enabled. */
public final class InterpreterRegressionTest {
    private static final Path REPOSITORY_ROOT = Path.of(".").toAbsolutePath().normalize();
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

    private InterpreterRegressionTest() {}

    public static void main(String[] args) throws Exception {
        List<TestCase> tests = List.of(
                new TestCase("equivalent layouts", InterpreterRegressionTest::equivalentLayouts),
                new TestCase("prose and math interpretation", InterpreterRegressionTest::proseAndMathInterpretation),
                new TestCase("optional fields", InterpreterRegressionTest::optionalFields),
                new TestCase("source comments", InterpreterRegressionTest::sourceComments),
                new TestCase("line-ending equivalence", InterpreterRegressionTest::lineEndingEquivalence),
                new TestCase("source discovery", InterpreterRegressionTest::sourceDiscovery),
                new TestCase("explicit file discovery", InterpreterRegressionTest::explicitFileDiscovery),
                new TestCase("physical source diagnostics", InterpreterRegressionTest::physicalSourceDiagnostics),
                new TestCase("Unicode conformance edges", InterpreterRegressionTest::unicodeConformanceEdges),
                new TestCase("record and field diagnostics", InterpreterRegressionTest::recordAndFieldDiagnostics),
                new TestCase("math diagnostics", InterpreterRegressionTest::mathDiagnostics),
                new TestCase("identity and relationship diagnostics", InterpreterRegressionTest::identityAndRelationshipDiagnostics));

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
        System.out.printf("Passed %d maintained interpreter regression tests.%n", passed);
    }

    private static void equivalentLayouts() {
        Interpreter.Result modules = Interpreter.interpretInputs(List.of(CANDIDATE_A));
        Interpreter.Result individual = Interpreter.interpretInputs(List.of(CANDIDATE_B));

        assertEquals(List.of(), modules.diagnostics(), "Candidate A diagnostics");
        assertEquals(List.of(), individual.diagnostics(), "Candidate B diagnostics");
        assertEquals(3, modules.fileCount(), "Candidate A file count");
        assertEquals(20, individual.fileCount(), "Candidate B file count");
        assertEquals(20, modules.requirements().size(), "Candidate A requirement count");
        assertEquals(20, individual.requirements().size(), "Candidate B requirement count");
        assertEquals(22, relationshipCount(modules), "Candidate A relationship count");
        assertEquals(22, relationshipCount(individual), "Candidate B relationship count");
        assertEquals(
                Interpreter.normalizedInventory(modules.requirements()),
                Interpreter.normalizedInventory(individual.requirements()),
                "semantic inventories");
    }

    private static void proseAndMathInterpretation() {
        Interpreter.Result result = Interpreter.interpretInputs(List.of(CANDIDATE_A));
        assertEquals(List.of(), result.diagnostics(), "fixture diagnostics");

        Interpreter.Requirement sys001 = requirement(result, "SYS-001");
        assertEquals(1, sys001.statement().size(), "SYS-001 statement block count");
        Interpreter.ProseBlock statement = (Interpreter.ProseBlock) sys001.statement().getFirst();
        assertEquals(
                "The mission-control system shall associate each registered vehicle with exactly one vehicle identifier that is unique within the mission repository.",
                statement.text(),
                "folded statement");

        Interpreter.Requirement sys006 = requirement(result, "SYS-006");
        Interpreter.MathBlock math = (Interpreter.MathBlock) sys006.statement().stream()
                .filter(block -> block instanceof Interpreter.MathBlock)
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
        Interpreter.Result result = interpretText(minimalRecord("REQ-001"));
        assertEquals(List.of(), result.diagnostics(), "minimal record diagnostics");
        Interpreter.Requirement requirement = result.requirements().getFirst();
        assertEquals(null, requirement.allocation(), "optional allocation");
        assertEquals(null, requirement.rationale(), "optional rationale");
        assertEquals(null, requirement.source(), "optional source");
        assertEquals(java.util.Set.of(), requirement.decomposes(), "optional relationships");
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

        Interpreter.Result result = interpretText(source);
        assertEquals(List.of(), result.diagnostics(), "commented source diagnostics");
        assertEquals(2, result.requirements().size(), "commented requirement count");

        Interpreter.Requirement first = requirement(result, "REQ-001");
        assertEquals("Hash # remains scalar content", first.title(), "hash in scalar content");
        assertEquals("SOURCE#1", first.source(), "hash in source scalar");
        assertEquals(
                "The system shall preserve # as prose content.",
                ((Interpreter.ProseBlock) first.statement().getFirst()).text(),
                "hash in statement prose");
        assertEquals(
                "# at body indentation remains rationale content.",
                ((Interpreter.ProseBlock) first.rationale().getFirst()).text(),
                "hash in rationale prose");

        Interpreter.MathBlock math = (Interpreter.MathBlock) requirement(result, "REQ-002").statement().getFirst();
        assertEquals("x_{#} = 1", math.payload(), "hash in math payload");
        assertTrue(
                !Interpreter.normalizedInventory(result.requirements()).contains("author comment"),
                "comments absent from semantic inventory");

        Interpreter.Result conformance01 = Interpreter.interpretInputs(List.of(CONFORMANCE_01));
        Interpreter.Result conformance02 = Interpreter.interpretInputs(List.of(CONFORMANCE_02));
        assertEquals(List.of(), conformance01.diagnostics(), "0.1 conformance diagnostics");
        assertEquals(List.of(), conformance02.diagnostics(), "0.2 conformance diagnostics");
        assertEquals(
                Interpreter.normalizedInventory(conformance01.requirements()),
                Interpreter.normalizedInventory(conformance02.requirements()),
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
                Interpreter.interpretInputs(List.of(CONFORMANCE_02_ROOT.resolve("invalid/comment-only.mreq"))),
                "empty-source-file");
        assertHasCode(
                Interpreter.interpretInputs(List.of(CONFORMANCE_02_ROOT.resolve("invalid/comment-before-body.mreq"))),
                "empty-body");
        assertHasCode(
                Interpreter.interpretInputs(List.of(CONFORMANCE_02_ROOT.resolve("invalid/comment-splits-prose.mreq"))),
                "malformed-record");
        assertHasCode(
                Interpreter.interpretInputs(List.of(CONFORMANCE_02_ROOT.resolve("invalid/comment-splits-math.mreq"))),
                "unterminated-math");
    }

    private static void lineEndingEquivalence() {
        Interpreter.Result lf = interpretText(minimalRecord("REQ-001"));
        Interpreter.Result crlf = interpretBytes(minimalRecord("REQ-001").replace("\n", "\r\n").getBytes(StandardCharsets.UTF_8));
        assertEquals(List.of(), crlf.diagnostics(), "CRLF diagnostics");
        assertEquals(
                Interpreter.normalizedInventory(lf.requirements()),
                Interpreter.normalizedInventory(crlf.requirements()),
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

            Interpreter.Result result = Interpreter.interpretInputs(List.of(temporary, selected));
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
            assertHasCode(Interpreter.interpretInputs(List.of(temporary)), "no-source-files");
            Interpreter.Result result = Interpreter.interpretInputs(List.of(explicit));
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

        byte[] malformedAfterMultibyte = new byte[] {
            'x', '\n', 'A',
            (byte) 0xf0, (byte) 0x9f, (byte) 0x98, (byte) 0x80,
            'B', (byte) 0xc3, '('
        };
        Interpreter.Diagnostic malformed = diagnosticWithCode(
                interpretBytes(malformedAfterMultibyte), "invalid-utf8");
        assertEquals(2, malformed.line(), "malformed UTF-8 line");
        assertEquals(7, malformed.column(), "malformed UTF-8 raw-byte column");
    }

    private static void unicodeConformanceEdges() {
        Path invalid = CONFORMANCE_02_ROOT.resolve("invalid");

        Interpreter.Diagnostic c1 = diagnosticWithCode(
                Interpreter.interpretInputs(List.of(invalid.resolve("prohibited-c1-control.mreq"))),
                "control-character");
        assertEquals(2, c1.line(), "C1 control line");
        assertEquals(10, c1.column(), "C1 control column");

        Interpreter.Diagnostic leading = diagnosticWithCode(
                Interpreter.interpretInputs(List.of(invalid.resolve("leading-non-ascii-whitespace.mreq"))),
                "empty-or-padded-scalar");
        assertEquals(2, leading.line(), "leading whitespace line");
        assertEquals(8, leading.column(), "leading whitespace column");

        Interpreter.Diagnostic trailing = diagnosticWithCode(
                Interpreter.interpretInputs(List.of(invalid.resolve("trailing-non-ascii-whitespace.mreq"))),
                "empty-or-padded-scalar");
        assertEquals(2, trailing.line(), "trailing whitespace line");
        assertEquals(36, trailing.column(), "trailing whitespace column");

        Interpreter.Diagnostic supplementary = diagnosticWithCode(
                Interpreter.interpretInputs(List.of(invalid.resolve("supplementary-scalar-column.mreq"))),
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
        Interpreter.Result duplicate = Interpreter.interpretSources(List.of(
                new Interpreter.Source("one.mreq", minimalRecord("REQ-001").getBytes(StandardCharsets.UTF_8)),
                new Interpreter.Source("two.mreq", minimalRecord("REQ-001").getBytes(StandardCharsets.UTF_8))));
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

        String cycles = String.join("\n\n",
                record("SELF", "SELF"),
                record("A", "B"),
                record("B", "C"),
                record("C", "A")) + "\n";
        Interpreter.Result cyclic = interpretText(cycles);
        assertEquals(List.of(), cyclic.diagnostics(), "cyclic decomposition diagnostics");
        assertEquals(4, cyclic.requirements().size(), "cyclic decomposition requirement count");
        assertEquals(4, relationshipCount(cyclic), "cyclic decomposition relationship count");
    }

    private static Interpreter.Result interpretText(String text) {
        return interpretBytes(text.getBytes(StandardCharsets.UTF_8));
    }

    private static Interpreter.Result interpretBytes(byte[] bytes) {
        return Interpreter.interpretSources(List.of(new Interpreter.Source("fixture.mreq", bytes)));
    }

    private static Interpreter.Requirement requirement(Interpreter.Result result, String id) {
        return result.requirements().stream()
                .filter(requirement -> requirement.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private static int relationshipCount(Interpreter.Result result) {
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

    private static void assertHasCode(Interpreter.Result result, String code) {
        Interpreter.Diagnostic diagnostic = diagnosticWithCode(result, code);
        assertTrue(!diagnostic.file().isEmpty(), code + " file");
        assertTrue(diagnostic.line() >= 1, code + " line");
        assertTrue(diagnostic.column() >= 1, code + " column");
        assertTrue(!diagnostic.message().isEmpty(), code + " message");
    }

    private static Interpreter.Diagnostic diagnosticWithCode(Interpreter.Result result, String code) {
        return result.diagnostics().stream()
                .filter(candidate -> candidate.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "expected %s; received %s".formatted(code, result.diagnostics().stream()
                                .map(Interpreter.Diagnostic::code)
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
