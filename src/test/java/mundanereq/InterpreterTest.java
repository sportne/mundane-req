package mundanereq;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Focused acceptance checks for maintained interpretation and discovery. */
public final class InterpreterTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();

    private InterpreterTest() {}

    public static void run() throws Exception {
        matchesNormativeInventories();
        keepsConcreteDifferencesOutOfSemanticEquality();
        buildsIdentityAndOutgoingIndexes();
        reportsConformingUnicodeCoordinates();
        countsScalarsInNulCoordinates();
        acceptsInternalUnicodeSeparators();
        rejectsEmptyDirectSourceSet();
        discoversExplicitSourceSetsDeterministically();
    }

    private static void matchesNormativeInventories() throws Exception {
        for (String version : List.of("0.1", "0.2")) {
            Path valid = ROOT.resolve("conformance/" + version + "/valid");
            Interpreter.Result result = Interpreter.interpretInputs(List.of(valid));
            assertEquals(List.of(), result.diagnostics(), version + " valid diagnostics");
            String expected = Files.readString(valid.resolve("expected.inventory"), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n");
            assertEquals(expected, Interpreter.normalizedInventory(result.requirements()), version + " inventory");
        }
    }

    private static void keepsConcreteDifferencesOutOfSemanticEquality() {
        Interpreter.Result plain = Interpreter.interpretInputs(List.of(ROOT.resolve("conformance/0.1/valid")));
        Interpreter.Result commented = Interpreter.interpretInputs(List.of(ROOT.resolve("conformance/0.2/valid")));
        assertEquals(plain.byId(), commented.byId(), "comment and location independent requirements");
        assertEquals(plain.outgoing(), commented.outgoing(), "comment and location independent relationships");
    }

    private static void buildsIdentityAndOutgoingIndexes() {
        Path corpus = ROOT.resolve("experiments/0001-source-representations/candidate-a-modules");
        Interpreter.Result result = Interpreter.interpretInputs(List.of(corpus));
        assertEquals(List.of(), result.diagnostics(), "representative corpus diagnostics");
        assertEquals(20, result.requirements().size(), "requirement count");
        assertEquals(20, result.byId().size(), "identity index size");
        assertEquals(result.requirements().getFirst(), result.byId().get(result.requirements().getFirst().id()), "identity lookup");
        int relationshipCount = result.outgoing().values().stream().mapToInt(java.util.Set::size).sum();
        assertEquals(22, relationshipCount, "outgoing relationship count");
        assertEquals(java.util.Set.of("OPS-001", "OPS-002"), result.outgoing().get("SYS-003"), "outgoing lookup");
    }

    private static void reportsConformingUnicodeCoordinates() {
        assertDiagnostic("prohibited-c1-control.mreq", "control-character", 2, 10);
        assertDiagnostic("leading-non-ascii-whitespace.mreq", "empty-or-padded-scalar", 2, 8);
        assertDiagnostic("trailing-non-ascii-whitespace.mreq", "empty-or-padded-scalar", 2, 36);
        assertDiagnostic("supplementary-scalar-column.mreq", "control-character", 2, 10);
    }

    private static void countsScalarsInNulCoordinates() {
        String source = "requirement REQ-001\ntitle: 😀\0\nstatement:\n  Required.\nend requirement\n";
        Interpreter.Result result = Interpreter.interpretSources(List.of(
                new Interpreter.Source("nul.mreq", source.getBytes(StandardCharsets.UTF_8))));
        assertEquals(1, result.diagnostics().size(), "NUL diagnostic count");
        Interpreter.Diagnostic diagnostic = result.diagnostics().getFirst();
        assertEquals("nul-byte", diagnostic.code(), "NUL code");
        assertEquals(2, diagnostic.line(), "NUL line");
        assertEquals(9, diagnostic.column(), "NUL scalar column");
    }

    private static void acceptsInternalUnicodeSeparators() {
        String source = "requirement REQ-001\ntitle: A\u2028B\u2029C\nstatement:\n  Required.\nend requirement\n";
        Interpreter.Result result = Interpreter.interpretSources(List.of(
                new Interpreter.Source("separators.mreq", source.getBytes(StandardCharsets.UTF_8))));
        assertEquals(List.of(), result.diagnostics(), "internal separator diagnostics");
        assertEquals("A\u2028B\u2029C", result.requirements().getFirst().title(), "internal separator value");
    }

    private static void rejectsEmptyDirectSourceSet() {
        Interpreter.Result result = Interpreter.interpretSources(List.of());
        assertEquals(1, result.diagnostics().size(), "empty direct source-set diagnostic count");
        assertEquals("no-source-files", result.diagnostics().getFirst().code(), "empty direct source-set code");
    }

    private static void discoversExplicitSourceSetsDeterministically() {
        Path modules = ROOT.resolve("experiments/0001-source-representations/candidate-a-modules");
        Interpreter.Result forward = Interpreter.interpretInputs(List.of(
                modules.resolve("mission-operations.mreq"),
                modules.resolve("identity-and-dispatch.mreq"),
                modules.resolve("link-and-recovery.mreq")));
        Interpreter.Result reverse = Interpreter.interpretInputs(List.of(
                modules.resolve("link-and-recovery.mreq"),
                modules.resolve("identity-and-dispatch.mreq"),
                modules.resolve("mission-operations.mreq")));
        assertEquals(List.of(), forward.diagnostics(), "forward discovery diagnostics");
        assertEquals(
                Interpreter.normalizedInventory(forward.requirements()),
                Interpreter.normalizedInventory(reverse.requirements()),
                "input-order-independent semantics");
    }

    private static void assertDiagnostic(String name, String code, int line, int column) {
        Path fixture = ROOT.resolve("conformance/0.2/invalid").resolve(name);
        Interpreter.Result result = Interpreter.interpretInputs(List.of(fixture));
        assertEquals(1, result.diagnostics().size(), name + " diagnostic count");
        Interpreter.Diagnostic diagnostic = result.diagnostics().getFirst();
        assertEquals(code, diagnostic.code(), name + " code");
        assertEquals(line, diagnostic.line(), name + " line");
        assertEquals(column, diagnostic.column(), name + " column");
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("%s: expected <%s> but was <%s>".formatted(description, expected, actual));
        }
    }
}
