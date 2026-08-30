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
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import mundanereq.Interpreter;
import mundanereq.format.SourceFormatter;
import mundanereq.source.SourceDocument;

/** JVM/native formatter verification over every maintained valid source set. */
public final class FormatterVerificationTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();
    private static final List<String> MAINTAINED_SOURCE_SETS = List.of(
            "conformance/0.1/valid",
            "conformance/0.2/valid",
            "experiments/0001-source-representations/candidate-a-modules",
            "experiments/0001-source-representations/candidate-b-one-per-file/requirements",
            "experiments/0003-sustained-authoring/requirements",
            "experiments/0004-transferability/requirements",
            "experiments/0008-formatting-policy/input-varied-crlf.mreq",
            "experiments/0008-formatting-policy/candidate-conservative.mreq",
            "experiments/0008-formatting-policy/candidate-prose-reflow.mreq",
            "experiments/0009-trace-workflows/graph-one-file.mreq",
            "experiments/0009-trace-workflows/graph-split",
            "experiments/0010-integrated-toolchain-trial/baseline",
            "experiments/0010-integrated-toolchain-trial/proposed",
            "experiments/0011-operational-corpus/requirements",
            "experiments/0016-identity-continuity/scenario/baseline-a",
            "experiments/0016-identity-continuity/scenario/baseline-b",
            "experiments/0017-verification-evidence/scenario/baseline-a",
            "experiments/0017-verification-evidence/scenario/baseline-b",
            "experiments/0018-safety-classification/scenario",
            "experiments/0020-allocation-model/plain/baseline-a",
            "experiments/0020-allocation-model/plain/baseline-b",
            "experiments/0020-allocation-model/plain/typo",
            "experiments/0020-allocation-model/referenced",
            "experiments/0021-glossary-symbols/scenario/baseline-a",
            "experiments/0021-glossary-symbols/scenario/baseline-b",
            "experiments/0021-glossary-symbols/scenario/baseline-c",
            "experiments/0022-trace-policies/source/pass",
            "experiments/0022-trace-policies/source/coverage-fail",
            "experiments/0022-trace-policies/source/cycle-fail");

    private record Invocation(int status, byte[] out, byte[] err) {}

    private record PreservationCounts(int files, int comments, int mathLines) {
        PreservationCounts plus(PreservationCounts other) {
            return new PreservationCounts(
                    files + other.files(), comments + other.comments(), mathLines + other.mathLines());
        }
    }

    private FormatterVerificationTest() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 1) throw new AssertionError("expected the native formatter path");
        Path nativeFormatter = Path.of(arguments[0]).toAbsolutePath().normalize();
        if (!Files.isExecutable(nativeFormatter)) throw new AssertionError("native formatter is not executable");

        PreservationCounts counts = verifyMaintainedSourceSets(nativeFormatter);
        System.out.printf(
                "PASS semantic preservation and byte-identical idempotence across %d source sets and %d files%n",
                MAINTAINED_SOURCE_SETS.size(),
                counts.files());
        System.out.printf(
                "PASS ordered preservation of %d comments and %d opaque math lines%n",
                counts.comments(),
                counts.mathLines());
        verifyModesAgree(nativeFormatter);
        System.out.println("PASS formatter modes agree between JVM and native image");
        System.out.println("PASS complete formatter JVM/native verification");
    }

    private static PreservationCounts verifyMaintainedSourceSets(Path nativeFormatter) throws Exception {
        PreservationCounts total = new PreservationCounts(0, 0, 0);
        Set<Path> coveredFiles = new TreeSet<>();
        for (String relative : MAINTAINED_SOURCE_SETS) {
            Path input = ROOT.resolve(relative);
            Interpreter.Selection selection = Interpreter.selectInputs(List.of(input));
            assertEquals(List.of(), selection.diagnostics(), relative + " selection diagnostics");
            selection.sources().stream()
                    .map(source -> Path.of(source.file()).toAbsolutePath().normalize())
                    .forEach(coveredFiles::add);
            Interpreter.Result before = Interpreter.interpretSources(selection.sources());
            assertEquals(List.of(), before.diagnostics(), relative + " source diagnostics");

            List<Interpreter.Source> formattedSources = new ArrayList<>();
            PreservationCounts sourceSetCounts = new PreservationCounts(0, 0, 0);
            for (Interpreter.Source source : selection.sources()) {
                SourceDocument original = SourceDocument.read(source.file(), source.bytes());
                byte[] formattedBytes = SourceFormatter.format(original);
                SourceDocument formatted = SourceDocument.read(source.file(), formattedBytes);
                byte[] second = SourceFormatter.format(formatted);
                assertArrayEquals(formattedBytes, second, source.file() + " idempotence");
                assertEquals(nonblankLines(original), nonblankLines(formatted), source.file() + " nonblank lines");
                assertEquals(commentLines(original), commentLines(formatted), source.file() + " comments");
                assertEquals(mathLines(original), mathLines(formatted), source.file() + " math lines");
                formattedSources.add(new Interpreter.Source(source.file(), formattedBytes));
                sourceSetCounts = sourceSetCounts.plus(new PreservationCounts(
                        1, commentLines(original).size(), mathLines(original).size()));
            }

            Interpreter.Result after = Interpreter.interpretSources(formattedSources);
            assertEquals(List.of(), after.diagnostics(), relative + " formatted diagnostics");
            assertEquals(before.byId(), after.byId(), relative + " requirement semantics");
            assertEquals(before.outgoing(), after.outgoing(), relative + " relationship semantics");
            assertAgreement(
                    nativeFormatter,
                    new String[] {"--check", input.toString()},
                    new String[] {"--check", input.toString()});
            total = total.plus(sourceSetCounts);
        }
        assertEquals(discoverMaintainedValidFiles(), coveredFiles, "maintained valid-source completeness");
        return total;
    }

    private static Set<Path> discoverMaintainedValidFiles() throws IOException {
        Set<Path> files = new TreeSet<>();
        for (String root : List.of("conformance", "experiments")) {
            try (var paths = Files.walk(ROOT.resolve(root))) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".mreq"))
                        .filter(path -> !path.toString().contains("/invalid/"))
                        .map(path -> path.toAbsolutePath().normalize())
                        .forEach(files::add);
            }
        }
        return Set.copyOf(files);
    }

    private static List<String> nonblankLines(SourceDocument document) {
        return document.lines().stream()
                .map(line -> line.physicalLine().text())
                .filter(text -> !text.isEmpty())
                .toList();
    }

    private static List<String> commentLines(SourceDocument document) {
        return document.comments().stream().map(line -> line.physicalLine().text()).toList();
    }

    private static List<String> mathLines(SourceDocument document) {
        List<String> result = new ArrayList<>();
        boolean inside = false;
        for (var line : document.lines()) {
            String text = line.physicalLine().text();
            if (text.equals("  math latex")) inside = true;
            if (inside) result.add(text);
            if (inside && text.equals("  end math")) inside = false;
        }
        if (inside) throw new AssertionError(document.name() + ": unterminated maintained math fixture");
        return List.copyOf(result);
    }

    private static void verifyModesAgree(Path nativeFormatter) throws Exception {
        Path temporary = Files.createTempDirectory("mundanereq-formatter-verification-");
        try {
            Path experiment = ROOT.resolve("experiments/0008-formatting-policy");
            Path source = temporary.resolve("source.mreq");
            Files.copy(experiment.resolve("input-varied-crlf.mreq"), source);

            assertAgreement(nativeFormatter, source.toString(), source.toString());
            Invocation unformatted = assertAgreement(
                    nativeFormatter,
                    new String[] {"--check", source.toString()},
                    new String[] {"--check", source.toString()});
            assertEquals(1, unformatted.status(), "unformatted check status");

            Path jvmWrite = temporary.resolve("jvm-write.mreq");
            Path nativeWrite = temporary.resolve("native-write.mreq");
            Files.copy(source, jvmWrite);
            Files.copy(source, nativeWrite);
            Invocation jvmResult = invokeJvm("--write", jvmWrite.toString());
            Invocation nativeResult = invokeNative(nativeFormatter, "--write", nativeWrite.toString());
            assertEquivalent(jvmResult, nativeResult, "write mode");
            assertEquals(0, jvmResult.status(), "write status");
            assertArrayEquals(Files.readAllBytes(jvmWrite), Files.readAllBytes(nativeWrite), "write output agreement");
            assertArrayEquals(
                    Files.readAllBytes(experiment.resolve("candidate-conservative.mreq")),
                    Files.readAllBytes(jvmWrite),
                    "write golden output");
            assertEquals(0, invokeJvm("--check", jvmWrite.toString()).status(), "formatted JVM check status");
            assertEquals(
                    0,
                    invokeNative(nativeFormatter, "--check", nativeWrite.toString()).status(),
                    "formatted native check status");

            Path invalid = temporary.resolve("invalid.mreq");
            Files.writeString(invalid, "content outside a record\n", StandardCharsets.UTF_8);
            byte[] invalidBytes = Files.readAllBytes(invalid);
            Invocation invalidResult = assertAgreement(
                    nativeFormatter,
                    new String[] {"--write", invalid.toString()},
                    new String[] {"--write", invalid.toString()});
            assertEquals(2, invalidResult.status(), "invalid write status");
            assertArrayEquals(invalidBytes, Files.readAllBytes(invalid), "invalid source nonmutation");

            Path child = temporary.resolve("child.mreq");
            Path parent = temporary.resolve("parent.mreq");
            Files.writeString(
                    child,
                    "requirement CHILD\r\ntitle: Child\r\nstatement:\r\n  Required.\r\n"
                            + "decomposes: PARENT\r\nend requirement\r\n",
                    StandardCharsets.UTF_8);
            Files.writeString(
                    parent,
                    "requirement PARENT\ntitle: Parent\nstatement:\n  Required.\nend requirement\n",
                    StandardCharsets.UTF_8);
            assertAgreement(
                    nativeFormatter,
                    new String[] {child.toString(), parent.toString()},
                    new String[] {child.toString(), parent.toString()});
        } finally {
            deleteTemporaryTree(temporary);
        }
    }

    private static Invocation assertAgreement(Path nativeFormatter, String jvmArgument, String nativeArgument)
            throws Exception {
        return assertAgreement(
                nativeFormatter, new String[] {jvmArgument}, new String[] {nativeArgument});
    }

    private static Invocation assertAgreement(
            Path nativeFormatter, String[] jvmArguments, String[] nativeArguments) throws Exception {
        Invocation jvm = invokeJvm(jvmArguments);
        Invocation nativeResult = invokeNative(nativeFormatter, nativeArguments);
        assertEquivalent(jvm, nativeResult, "arguments " + List.of(jvmArguments));
        return jvm;
    }

    private static void assertEquivalent(Invocation expected, Invocation actual, String description) {
        assertEquals(expected.status(), actual.status(), description + " status");
        assertArrayEquals(expected.out(), actual.out(), description + " stdout");
        assertArrayEquals(expected.err(), actual.err(), description + " stderr");
    }

    private static Invocation invokeJvm(String... arguments) {
        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream standardError = new ByteArrayOutputStream();
        int status;
        try (PrintStream out = new PrintStream(standardOutput, true, StandardCharsets.UTF_8);
                PrintStream err = new PrintStream(standardError, true, StandardCharsets.UTF_8)) {
            status = FormatterMain.run(arguments, out, err);
        }
        return new Invocation(status, standardOutput.toByteArray(), standardError.toByteArray());
    }

    private static Invocation invokeNative(Path nativeFormatter, String... arguments) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(nativeFormatter.toString());
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).start();
        try (var readers = Executors.newFixedThreadPool(2)) {
            Future<byte[]> standardOutput = readers.submit(() -> process.getInputStream().readAllBytes());
            Future<byte[]> standardError = readers.submit(() -> process.getErrorStream().readAllBytes());
            int status = process.waitFor();
            return new Invocation(status, standardOutput.get(), standardError.get());
        }
    }

    private static void deleteTemporaryTree(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }

    private static void assertArrayEquals(byte[] expected, byte[] actual, String description) {
        if (!Arrays.equals(expected, actual)) throw new AssertionError(description + ": byte sequences differ");
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("%s: expected <%s> but was <%s>".formatted(description, expected, actual));
        }
    }
}
