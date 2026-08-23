package mundanereq.format;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import mundanereq.source.SourceDocument;

/** Focused checks for the selected physical rewrite policy. */
public final class SourceFormatterTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();

    private SourceFormatterTest() {}

    public static void run() throws Exception {
        Path experiment = ROOT.resolve("experiments/0008-formatting-policy");
        byte[] input = Files.readAllBytes(experiment.resolve("input-varied-crlf.mreq"));
        byte[] expected = Files.readAllBytes(experiment.resolve("candidate-conservative.mreq"));
        SourceDocument document = SourceDocument.read("input-varied-crlf.mreq", input);
        byte[] formatted = SourceFormatter.format(document);
        assertArrayEquals(expected, formatted, "selected policy output");

        SourceDocument second = SourceDocument.read("formatted.mreq", formatted);
        assertArrayEquals(formatted, SourceFormatter.format(second), "formatter idempotence smoke");
    }

    private static void assertArrayEquals(byte[] expected, byte[] actual, String description) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(description + ": byte sequences differ");
        }
    }
}
