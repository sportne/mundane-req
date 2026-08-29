package mundanereq.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** JVM/native validator verification over the complete maintained fixture set. */
public final class ValidatorVerificationTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();

    private record Expected(String path, int status, String diagnosticCode) {}

    private record Invocation(int status, String out, String err, long elapsedNanos) {}

    private ValidatorVerificationTest() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 1) throw new AssertionError("expected the native validator path");
        Path nativeValidator = Path.of(arguments[0]).toAbsolutePath().normalize();
        if (!Files.isExecutable(nativeValidator)) throw new AssertionError("native validator is not executable");

        validCorporaAgree(nativeValidator);
        invalidFixturesAgree(nativeValidator);
        selectionAndInvocationAgree(nativeValidator);
        diagnosticVolumeDoesNotBlock(nativeValidator);
        repairWorkflowsAgree(nativeValidator);
        observeNativeStartup(nativeValidator);
        System.out.println("PASS complete validator JVM/native verification");
    }

    private static void validCorporaAgree(Path nativeValidator) throws Exception {
        List<String> corpora = List.of(
                "conformance/0.1/valid",
                "conformance/0.2/valid",
                "experiments/0001-source-representations/candidate-a-modules",
                "experiments/0001-source-representations/candidate-b-one-per-file/requirements",
                "experiments/0003-sustained-authoring/requirements",
                "experiments/0004-transferability/requirements",
                "experiments/0010-integrated-toolchain-trial/baseline",
                "experiments/0010-integrated-toolchain-trial/proposed");
        for (String path : corpora) {
            assertAgreement(nativeValidator, 0, null, ROOT.resolve(path).toString());
        }
        System.out.printf("PASS %d valid project corpora%n", corpora.size());
    }

    private static void invalidFixturesAgree(Path nativeValidator) throws Exception {
        List<Expected> invalid = List.of(
                new Expected("conformance/0.1/invalid/dangling-reference.mreq", 1, "dangling-reference"),
                new Expected("conformance/0.1/invalid/duplicate-id", 1, "duplicate-id"),
                new Expected("conformance/0.1/invalid/duplicate-relationship.mreq", 1, "duplicate-relationship"),
                new Expected("conformance/0.1/invalid/missing-statement.mreq", 1, "missing-field"),
                new Expected("conformance/0.1/invalid/unknown-field.mreq", 1, "unknown-field"),
                new Expected("conformance/0.1/invalid/unterminated-math.mreq", 1, "unterminated-math"),
                new Expected("conformance/0.2/invalid/comment-before-body.mreq", 1, "empty-body"),
                new Expected("conformance/0.2/invalid/comment-only.mreq", 1, "empty-source-file"),
                new Expected("conformance/0.2/invalid/comment-splits-math.mreq", 1, "unterminated-math"),
                new Expected("conformance/0.2/invalid/comment-splits-prose.mreq", 1, "malformed-record"),
                new Expected("conformance/0.2/invalid/leading-non-ascii-whitespace.mreq", 1, "empty-or-padded-scalar"),
                new Expected("conformance/0.2/invalid/prohibited-c1-control.mreq", 1, "control-character"),
                new Expected("conformance/0.2/invalid/supplementary-scalar-column.mreq", 1, "control-character"),
                new Expected("conformance/0.2/invalid/trailing-non-ascii-whitespace.mreq", 1, "empty-or-padded-scalar"));
        for (Expected expected : invalid) {
            assertAgreement(
                    nativeValidator,
                    expected.status(),
                    expected.diagnosticCode(),
                    ROOT.resolve(expected.path()).toString());
        }
        System.out.printf("PASS %d invalid conformance selections%n", invalid.size());
    }

    private static void selectionAndInvocationAgree(Path nativeValidator) throws Exception {
        assertAgreement(nativeValidator, 0, null, ROOT.resolve("conformance/0.2/valid/requirements.mreq").toString());

        String validDirectory = ROOT.resolve("conformance/0.2/valid").toString();
        Invocation duplicate = assertAgreement(nativeValidator, 0, null, validDirectory, validDirectory);
        assertContains(duplicate.out(), "from 1 file", "duplicate input deduplication");

        assertAgreement(nativeValidator, 2, "input-unavailable", ROOT.resolve("missing-input.mreq").toString());
        assertAgreement(nativeValidator, 2, null);
        assertAgreement(nativeValidator, 2, null, "--inventory");
        assertAgreement(nativeValidator, 0, null, "--help");
        assertAgreement(nativeValidator, 0, null, "--version");

        Path empty = Files.createTempDirectory("mundanereq-empty-selection-");
        try {
            assertAgreement(nativeValidator, 2, "no-source-files", empty.toString());
        } finally {
            Files.deleteIfExists(empty);
        }
        System.out.println("PASS source selection and invocation classes");
    }

    private static void repairWorkflowsAgree(Path nativeValidator) throws Exception {
        Path temporary = Files.createTempDirectory("mundanereq-validator-repair-");
        try {
            Path unicode = temporary.resolve("unicode.mreq");
            String invalidUnicode = Files.readString(
                    ROOT.resolve("conformance/0.2/invalid/supplementary-scalar-column.mreq"),
                    StandardCharsets.UTF_8);
            Files.writeString(unicode, invalidUnicode, StandardCharsets.UTF_8);
            Invocation unicodeFailure = assertAgreement(nativeValidator, 1, "control-character", unicode.toString());
            assertContains(unicodeFailure.err(), ":2:10:", "Unicode repair coordinate");
            Files.writeString(unicode, invalidUnicode.replace("\u007f", ""), StandardCharsets.UTF_8);
            assertAgreement(nativeValidator, 0, null, unicode.toString());

            Path relationship = temporary.resolve("relationship.mreq");
            String child = "requirement CHILD\n"
                    + "title: Child\n"
                    + "statement:\n"
                    + "  The child shall be valid.\n"
                    + "decomposes: PARENT\n"
                    + "end requirement\n";
            Files.writeString(relationship, child, StandardCharsets.UTF_8);
            Invocation dangling = assertAgreement(nativeValidator, 1, "dangling-reference", relationship.toString());
            assertContains(dangling.err(), ":5:13:", "relationship repair coordinate");
            String parent = "\nrequirement PARENT\n"
                    + "title: Parent\n"
                    + "statement:\n"
                    + "  The parent shall be valid.\n"
                    + "end requirement\n";
            Files.writeString(relationship, child + parent, StandardCharsets.UTF_8);
            assertAgreement(nativeValidator, 0, null, relationship.toString());
        } finally {
            deleteTemporaryTree(temporary);
        }
        System.out.println("PASS diagnostic-guided repair workflows");
    }

    private static void diagnosticVolumeDoesNotBlock(Path nativeValidator) throws Exception {
        Path temporary = Files.createTempDirectory("mundanereq-validator-diagnostics-");
        try {
            for (int index = 0; index < 1_200; index++) {
                Files.writeString(
                        temporary.resolve("invalid-%04d.mreq".formatted(index)),
                        "content outside a record\n",
                        StandardCharsets.UTF_8);
            }
            Invocation result = assertAgreement(nativeValidator, 1, "content-outside-record", temporary.toString());
            long diagnosticLines = result.err().lines().count();
            assertEquals(1_200L, diagnosticLines, "large diagnostic-set line count");
        } finally {
            deleteTemporaryTree(temporary);
        }
        System.out.println("PASS bounded high-volume diagnostic capture");
    }

    private static void observeNativeStartup(Path nativeValidator) throws Exception {
        String small = ROOT.resolve("conformance/0.2/valid").toString();
        String representative = ROOT.resolve("experiments/0001-source-representations/candidate-b-one-per-file/requirements")
                .toString();
        long smallMillis = invokeNative(nativeValidator, small).elapsedNanos() / 1_000_000;
        long representativeMillis = invokeNative(nativeValidator, representative).elapsedNanos() / 1_000_000;
        System.out.printf(
                "OBSERVED native process duration: %d ms (one-file corpus), %d ms (20-file corpus); no threshold asserted.%n",
                smallMillis,
                representativeMillis);
    }

    private static Invocation assertAgreement(
            Path nativeValidator, int expectedStatus, String expectedCode, String... arguments) throws Exception {
        Invocation jvm = invokeJvm(arguments);
        Invocation nativeResult = invokeNative(nativeValidator, arguments);
        assertEquals(expectedStatus, jvm.status(), "JVM status for " + List.of(arguments));
        assertEquals(jvm.status(), nativeResult.status(), "native status for " + List.of(arguments));
        assertEquals(jvm.out(), nativeResult.out(), "stdout agreement for " + List.of(arguments));
        assertEquals(jvm.err(), nativeResult.err(), "stderr agreement for " + List.of(arguments));
        if (expectedCode != null) assertContains(jvm.err(), ": " + expectedCode + ": ", "diagnostic code");
        return jvm;
    }

    private static Invocation invokeJvm(String... arguments) {
        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream standardError = new ByteArrayOutputStream();
        long started = System.nanoTime();
        int status;
        try (PrintStream out = new PrintStream(standardOutput, true, StandardCharsets.UTF_8);
                PrintStream err = new PrintStream(standardError, true, StandardCharsets.UTF_8)) {
            status = ValidatorMain.run(arguments, out, err);
        }
        return new Invocation(
                status,
                standardOutput.toString(StandardCharsets.UTF_8),
                standardError.toString(StandardCharsets.UTF_8),
                System.nanoTime() - started);
    }

    private static Invocation invokeNative(Path nativeValidator, String... arguments) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(nativeValidator.toString());
        command.addAll(List.of(arguments));
        long started = System.nanoTime();
        Process process = new ProcessBuilder(command).start();
        try (var readers = Executors.newFixedThreadPool(2)) {
            Future<byte[]> standardOutput = readers.submit(() -> process.getInputStream().readAllBytes());
            Future<byte[]> standardError = readers.submit(() -> process.getErrorStream().readAllBytes());
            int status = process.waitFor();
            String out = new String(standardOutput.get(), StandardCharsets.UTF_8);
            String err = new String(standardError.get(), StandardCharsets.UTF_8);
            return new Invocation(status, out, err, System.nanoTime() - started);
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

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("%s: expected <%s> but was <%s>".formatted(description, expected, actual));
        }
    }
}
