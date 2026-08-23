package mundanereq.source;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/** Focused checks for the physical and concrete source representation. */
public final class SourceDocumentTest {
    private static final Path ROOT = Path.of(".").toAbsolutePath().normalize();

    private SourceDocumentTest() {}

    public static void run() throws Exception {
        preservesConformanceSource();
        distinguishesTriviaWithoutAttachingIt();
        retainsLineEndingsAndScalarSpans();
        rejectsUndecodableOrUnterminatedInput();
        rejectsNonScalarConstructedText();
    }

    private static void preservesConformanceSource() throws Exception {
        Path fixture = ROOT.resolve("conformance/0.2/valid/requirements.mreq");
        byte[] bytes = Files.readAllBytes(fixture);
        SourceDocument document = SourceDocument.read(fixture.toString(), bytes);
        assertTrue(Arrays.equals(bytes, document.originalBytes()), "original byte retention");
        assertTrue(Arrays.equals(bytes, document.renderedBytes()), "lossless rendering");
        assertEquals(12, document.comments().size(), "comment count");
    }

    private static void distinguishesTriviaWithoutAttachingIt() throws Exception {
        SourceDocument plain = readFixture("conformance/0.1/valid/requirements.mreq");
        SourceDocument commented = readFixture("conformance/0.2/valid/requirements.mreq");
        assertEquals(contentLines(plain), contentLines(commented), "semantic-bearing source lines");
        assertTrue(plain.comments().isEmpty(), "plain fixture comments");
        assertTrue(!commented.comments().isEmpty(), "commented fixture comments");

        byte[] expected01 = Files.readAllBytes(ROOT.resolve("conformance/0.1/valid/expected.inventory"));
        byte[] expected02 = Files.readAllBytes(ROOT.resolve("conformance/0.2/valid/expected.inventory"));
        assertTrue(Arrays.equals(expected01, expected02), "normative semantic inventories");
    }

    private static void retainsLineEndingsAndScalarSpans() throws Exception {
        String text = "A😀\r\n# note\n\n";
        SourceDocument document = SourceDocument.read("mixed.mreq", text.getBytes(StandardCharsets.UTF_8));
        assertEquals(3, document.lines().size(), "physical line count");
        assertEquals(LineEnding.CRLF, document.lines().get(0).physicalLine().lineEnding(), "CRLF spelling");
        assertEquals(3, document.lines().get(0).physicalLine().span().end().column(), "scalar span end");
        assertEquals(ConcreteLine.Kind.COMMENT, document.lines().get(1).kind(), "comment kind");
        assertEquals(ConcreteLine.Kind.BLANK, document.lines().get(2).kind(), "blank kind");
        assertTrue(Arrays.equals(text.getBytes(StandardCharsets.UTF_8), document.renderedBytes()), "mixed rendering");
    }

    private static void rejectsUndecodableOrUnterminatedInput() {
        assertThrows(() -> SourceDocument.read("invalid.mreq", new byte[] {(byte) 0xc3, '('}), "invalid UTF-8");
        assertThrows(
                () -> SourceDocument.read("unterminated.mreq", "text".getBytes(StandardCharsets.UTF_8)),
                "unterminated source");
    }

    private static void rejectsNonScalarConstructedText() {
        SourcePosition start = new SourcePosition("invalid.mreq", 1, 1);
        SourcePosition end = new SourcePosition("invalid.mreq", 1, 2);
        SourceSpan span = new SourceSpan(start, end);
        assertThrows(() -> new PhysicalLine(span, "\ud800", LineEnding.LF), "unpaired high surrogate");
        assertThrows(() -> new PhysicalLine(span, "\udc00", LineEnding.LF), "unpaired low surrogate");
    }

    private static SourceDocument readFixture(String name) throws Exception {
        Path path = ROOT.resolve(name);
        return SourceDocument.read(path.toString(), Files.readAllBytes(path));
    }

    private static List<String> contentLines(SourceDocument document) {
        return document.lines().stream()
                .filter(line -> line.kind() == ConcreteLine.Kind.CONTENT)
                .map(line -> line.physicalLine().text())
                .toList();
    }

    private static void assertThrows(CheckedRunnable action, String description) {
        try {
            action.run();
        } catch (Exception expected) {
            return;
        }
        throw new AssertionError("expected failure: " + description);
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("%s: expected <%s> but was <%s>".formatted(description, expected, actual));
        }
    }

    private static void assertTrue(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
