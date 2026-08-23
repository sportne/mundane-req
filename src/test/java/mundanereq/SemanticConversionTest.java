package mundanereq;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import mundanereq.source.SourceDocument;

/** Normative semantic conversion checks for the maintained interpreter. */
public final class SemanticConversionTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();

    private SemanticConversionTest() {}

    public static void run() throws Exception {
        String plain = inventoryThroughConcreteSource("conformance/0.1/valid/requirements.mreq");
        String commented = inventoryThroughConcreteSource("conformance/0.2/valid/requirements.mreq");
        String expected = Files.readString(
                        ROOT.resolve("conformance/0.2/valid/expected.inventory"), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");

        if (!plain.equals(commented)) {
            throw new AssertionError("commented and comment-free concrete sources differ semantically");
        }
        if (!expected.equals(commented)) {
            throw new AssertionError("concrete-to-semantic conversion differs from normative inventory");
        }
    }

    private static String inventoryThroughConcreteSource(String relativePath) throws Exception {
        Path path = ROOT.resolve(relativePath);
        SourceDocument document = SourceDocument.read(path.toString(), Files.readAllBytes(path));
        Interpreter.Result result = Interpreter.interpretSources(
                List.of(new Interpreter.Source(path.toString(), document.renderedBytes())));
        if (!result.diagnostics().isEmpty()) {
            throw new AssertionError("oracle rejected concrete source: " + result.diagnostics());
        }
        return Interpreter.normalizedInventory(result.requirements());
    }
}
