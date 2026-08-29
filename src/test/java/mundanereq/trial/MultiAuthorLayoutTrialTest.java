package mundanereq.trial;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Controlled two-author Git histories over two semantically equivalent layouts. */
public final class MultiAuthorLayoutTrialTest {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();
    private static final Path CORPUS = ROOT.resolve("experiments/0011-operational-corpus/requirements");
    private static final Pattern RECORD = Pattern.compile(
            "(?ms)^requirement ([A-Za-z0-9][A-Za-z0-9._-]*)\\n.*?^end requirement\\n");
    private static final String QUEUE_RECORD = """
            requirement EDGE-ALERT-QUEUE-001
            title: Retain local hazards until acknowledgement
            statement:
              The station processor shall retain each detected local hazard until a gateway acknowledges it.
            decomposes: SYS-ALERT-NOTIFY-001
            end requirement
            """;
    private static final Map<String, String> ISOLATED_GIT = Map.of(
            "GIT_CONFIG_NOSYSTEM", "1",
            "GIT_CONFIG_GLOBAL", "/dev/null",
            "GIT_DEFAULT_HASH", "sha1");

    private enum Layout { SUBJECT, ONE_RECORD }

    private enum Scenario { SEPARATED, OVERLAP, MOVE_EDIT, RETARGET }

    private record Invocation(int status, String out, String err) {}

    private record Verification(Invocation formatter, Invocation validation, Invocation trace) {}

    private record Outcome(
            String fingerprint,
            int baselineFiles,
            int finalFiles,
            int changedPaths,
            int conflictedFiles,
            int conflictHunks) {}

    private MultiAuthorLayoutTrialTest() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length < 3 || arguments.length > 4) {
            throw new AssertionError("expected formatter, validator, trace, and optional evidence path");
        }
        Path formatter = executable(arguments[0]);
        Path validator = executable(arguments[1]);
        Path trace = executable(arguments[2]);
        StringBuilder evidence = new StringBuilder();
        Path temporary = Files.createTempDirectory("mundanereq-layout-trial-");
        try {
            for (Scenario scenario : Scenario.values()) {
                Map<Layout, Outcome> outcomes = new EnumMap<>(Layout.class);
                for (Layout layout : Layout.values()) {
                    Outcome outcome = runScenario(
                            temporary.resolve(scenario.name().toLowerCase() + "-" + layout.name().toLowerCase()),
                            scenario, layout, formatter, validator, trace, evidence);
                    assertObservedShape(scenario, layout, outcome);
                    outcomes.put(layout, outcome);
                }
                assertEquals(outcomes.get(Layout.SUBJECT).fingerprint(),
                        outcomes.get(Layout.ONE_RECORD).fingerprint(), scenario + " semantic equivalence");
                printOutcome(scenario, Layout.SUBJECT, outcomes.get(Layout.SUBJECT));
                printOutcome(scenario, Layout.ONE_RECORD, outcomes.get(Layout.ONE_RECORD));
            }
        } finally {
            deleteTree(temporary);
        }
        if (arguments.length == 4) {
            Files.writeString(Path.of(arguments[3]), evidence, StandardCharsets.UTF_8);
        }
        System.out.println("PASS controlled two-author histories and equivalent final semantics");
    }

    private static Outcome runScenario(
            Path repository,
            Scenario scenario,
            Layout layout,
            Path formatter,
            Path validator,
            Path trace,
            StringBuilder evidence) throws Exception {
        Files.createDirectories(repository);
        prepareLayout(repository.resolve("requirements"), layout);
        git(repository, "Administrator", "init", "-b", "main");
        git(repository, "Administrator", "add", "requirements");
        git(repository, "Administrator", "commit", "-m", "Establish " + layout + " baseline");
        if (scenario == Scenario.RETARGET) {
            seedQueue(repository.resolve("requirements"), layout);
            git(repository, "Administrator", "add", "requirements");
            git(repository, "Administrator", "commit", "-m", "Seed retarget scenario baseline");
        }
        git(repository, "Administrator", "tag", "-a", "before", "-m", "Before " + scenario);
        int baselineFiles = countSources(repository.resolve("requirements"));

        git(repository, "Author A", "checkout", "-b", "author-a");
        applyAuthorA(repository.resolve("requirements"), scenario, layout);
        Verification authorA = verifyBranch(repository, formatter, validator, trace, traceId(scenario));
        git(repository, "Author A", "add", "-A", "requirements");
        git(repository, "Author A", "commit", "-m", "Author A: " + scenario);

        git(repository, "Administrator", "checkout", "main");
        git(repository, "Author B", "checkout", "-b", "author-b");
        applyAuthorB(repository.resolve("requirements"), scenario);
        Verification authorB = verifyBranch(repository, formatter, validator, trace, traceId(scenario));
        git(repository, "Author B", "add", "-A", "requirements");
        git(repository, "Author B", "commit", "-m", "Author B: " + scenario);

        Path expected = repository.getParent().resolve(repository.getFileName() + "-expected");
        prepareLayout(expected.resolve("requirements"), layout);
        if (scenario == Scenario.RETARGET) seedQueue(expected.resolve("requirements"), layout);
        applyResolution(expected.resolve("requirements"), scenario, layout);

        git(repository, "Administrator", "checkout", "main");
        git(repository, "Administrator", "merge", "--no-ff", "author-a", "-m", "Merge author A");
        Invocation secondMerge = gitAllowFailure(
                repository, "Administrator", "merge", "--no-ff", "author-b", "-m", "Merge author B");
        int conflictedFiles = 0;
        int conflictHunks = 0;
        String conflictDiff = "(none)\n";
        String resolution = "(not required)";
        if (secondMerge.status() != 0) {
            assertTrue(Files.isRegularFile(repository.resolve(".git/MERGE_HEAD")),
                    "failed merge must leave MERGE_HEAD");
            List<String> conflicts = git(repository, "Administrator", "diff", "--name-only", "--diff-filter=U")
                    .out().lines().filter(line -> !line.isBlank()).toList();
            assertTrue(!conflicts.isEmpty(), "failed merge must have unmerged paths");
            assertTrue(expectsConflict(scenario, layout),
                    "unexpected merge conflict in " + scenario + " " + layout);
            conflictedFiles = conflicts.size();
            for (String conflict : conflicts) {
                conflictHunks += countOccurrences(
                        Files.readString(repository.resolve(conflict), StandardCharsets.UTF_8), "<<<<<<<");
            }
            conflictDiff = git(repository, "Administrator", "diff", "--cc", "--", "requirements").out();
            resolveConflicts(repository, scenario, conflicts);
            resolution = "git checkout --ours -- " + String.join(" ", conflicts)
                    + (scenario == Scenario.MOVE_EDIT || scenario == Scenario.RETARGET
                    ? "; apply Author B source operation to the checked-out merged tree" : "");
            git(repository, "Administrator", "add", "-A", "requirements");
            git(repository, "Administrator", "commit", "-m", "Resolve " + scenario + " with ordinary source edits");
        } else {
            assertTrue(!Files.exists(repository.resolve(".git/MERGE_HEAD")),
                    "successful merge must not leave MERGE_HEAD");
            String unmerged = git(repository, "Administrator", "diff", "--name-only", "--diff-filter=U").out();
            assertEquals("", unmerged, "successful merge unmerged paths");
            assertTrue(!expectsConflict(scenario, layout),
                    "expected merge conflict absent in " + scenario + " " + layout);
        }

        Verification finalVerification = verifyBranch(repository, formatter, validator, trace, traceId(scenario));
        String actualFingerprint = OperationalCorpusVerificationTest.semanticFingerprint(
                repository.resolve("requirements"));
        String expectedFingerprint = OperationalCorpusVerificationTest.semanticFingerprint(
                expected.resolve("requirements"));
        assertEquals(expectedFingerprint, actualFingerprint, scenario + " " + layout + " resolved semantics");
        git(repository, "Administrator", "tag", "-a", "after", "-m", "After " + scenario);
        String diff = git(repository, "Administrator", "diff", "before..after", "--", "requirements").out();
        assertDiff(scenario, diff);
        int changedPaths = (int) git(repository, "Administrator", "diff", "--name-only", "before..after", "--", "requirements")
                .out().lines().filter(line -> !line.isBlank()).count();
        String changed = git(repository, "Administrator", "diff", "--name-status", "before..after", "--", "requirements").out();
        String history = git(repository, "Administrator", "log", "--reverse", "--format=%an | %s", "before..after").out();
        assertEquals("", git(repository, "Administrator", "status", "--porcelain").out(), "clean result");
        appendEvidence(evidence, scenario, layout, baselineFiles, authorA, authorB, secondMerge,
                conflictedFiles, conflictHunks, conflictDiff, resolution, finalVerification, changed, diff, history);
        return new Outcome(actualFingerprint, baselineFiles, countSources(repository.resolve("requirements")),
                changedPaths, conflictedFiles, conflictHunks);
    }

    private static void prepareLayout(Path target, Layout layout) throws IOException {
        Files.createDirectories(target);
        if (layout == Layout.SUBJECT) {
            try (var files = Files.list(CORPUS)) {
                for (Path source : files.sorted().toList()) Files.copy(source, target.resolve(source.getFileName()));
            }
            return;
        }
        try (var files = Files.list(CORPUS)) {
            for (Path source : files.sorted().toList()) {
                Matcher matcher = RECORD.matcher(Files.readString(source, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    Files.writeString(target.resolve(matcher.group(1) + ".mreq"), matcher.group(), StandardCharsets.UTF_8);
                }
            }
        }
        assertEquals(60, countSources(target), "one-record source count");
        assertEquals(OperationalCorpusVerificationTest.semanticFingerprint(CORPUS),
                OperationalCorpusVerificationTest.semanticFingerprint(target), "initial layout fingerprint");
    }

    private static void seedQueue(Path requirements, Layout layout) throws IOException {
        if (layout == Layout.ONE_RECORD) {
            Files.writeString(requirements.resolve("EDGE-ALERT-QUEUE-001.mreq"), QUEUE_RECORD, StandardCharsets.UTF_8);
        } else {
            Path alerting = requirements.resolve("02-alerting.mreq");
            Files.writeString(alerting, Files.readString(alerting, StandardCharsets.UTF_8) + "\n" + QUEUE_RECORD,
                    StandardCharsets.UTF_8);
        }
    }

    private static void applyAuthorA(Path requirements, Scenario scenario, Layout layout) throws IOException {
        switch (scenario) {
            case SEPARATED -> replaceInRecord(requirements, "SENSOR-LEVEL-001",
                    "provide a water-level measurement", "provide a compensated water-level measurement");
            case OVERLAP -> replaceInRecord(requirements, "SYS-ALERT-NOTIFY-001", "within 60 seconds", "within 45 seconds");
            case MOVE_EDIT -> moveRecord(requirements, layout, "EDGE-STORE-001");
            case RETARGET -> replaceInRecord(requirements, "EDGE-ALERT-QUEUE-001",
                    "each detected local hazard", "each detected and qualified local hazard");
        }
    }

    private static void applyAuthorB(Path requirements, Scenario scenario) throws IOException {
        switch (scenario) {
            case SEPARATED -> replaceInRecord(requirements, "AUDIT-QUERY-001",
                    "time, actor, action, or target", "time, actor, action, target, or outcome");
            case OVERLAP -> replaceInRecord(requirements, "SYS-ALERT-NOTIFY-001", "within 60 seconds", "within 30 seconds");
            case MOVE_EDIT -> replaceInRecord(requirements, "EDGE-STORE-001",
                    "nonvolatile local storage", "redundant nonvolatile local storage");
            case RETARGET -> replaceInRecord(requirements, "EDGE-ALERT-QUEUE-001",
                    "decomposes: SYS-ALERT-NOTIFY-001", "decomposes: SYS-RESILIENT-STORE-001");
        }
    }

    private static void applyResolution(Path requirements, Scenario scenario, Layout layout) throws IOException {
        applyAuthorA(requirements, scenario, layout);
        if (scenario != Scenario.OVERLAP) applyAuthorB(requirements, scenario);
    }

    private static void moveRecord(Path requirements, Layout layout, String id) throws IOException {
        Path source = findRecord(requirements, id);
        if (layout == Layout.ONE_RECORD) {
            Path destination = requirements.resolve("relocated").resolve(source.getFileName());
            Files.createDirectories(destination.getParent());
            Files.move(source, destination);
            return;
        }
        String text = Files.readString(source, StandardCharsets.UTF_8);
        Matcher matcher = RECORD.matcher(text);
        String record = null;
        StringBuilder remaining = new StringBuilder();
        int previous = 0;
        while (matcher.find()) {
            if (matcher.group(1).equals(id)) {
                record = matcher.group();
                remaining.append(text, previous, matcher.start());
                previous = matcher.end();
            }
        }
        remaining.append(text.substring(previous));
        if (record == null) throw new AssertionError("record not found for move: " + id);
        Files.writeString(source, remaining.toString().replace("\n\n\n", "\n\n"), StandardCharsets.UTF_8);
        Path destination = requirements.resolve("06-records.mreq");
        Files.writeString(destination, Files.readString(destination, StandardCharsets.UTF_8) + "\n" + record,
                StandardCharsets.UTF_8);
    }

    private static void replaceInRecord(Path requirements, String id, String oldText, String newText)
            throws IOException {
        Path file = findRecord(requirements, id);
        String before = Files.readString(file, StandardCharsets.UTF_8);
        Matcher matcher = RECORD.matcher(before);
        int recordStart = -1;
        int recordEnd = -1;
        while (matcher.find()) {
            if (matcher.group(1).equals(id)) {
                recordStart = matcher.start();
                recordEnd = matcher.end();
                break;
            }
        }
        if (recordStart < 0) throw new AssertionError("record disappeared: " + id);
        String record = before.substring(recordStart, recordEnd);
        int withinRecord = record.indexOf(oldText);
        if (withinRecord < 0 || record.indexOf(oldText, withinRecord + 1) >= 0) {
            throw new AssertionError("expected one replacement in " + id + ": " + oldText + "; record <" + record + ">");
        }
        int first = recordStart + withinRecord;
        Files.writeString(file, before.substring(0, first) + newText + before.substring(first + oldText.length()),
                StandardCharsets.UTF_8);
    }

    private static Path findRecord(Path requirements, String id) throws IOException {
        try (var paths = Files.walk(requirements)) {
            List<Path> matches = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".mreq"))
                    .filter(path -> contains(path, "requirement " + id + "\n"))
                    .toList();
            if (matches.size() != 1) throw new AssertionError(id + " record files: " + matches);
            return matches.getFirst();
        }
    }

    private static boolean contains(Path path, String value) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8).contains(value);
        } catch (IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
    }

    private static boolean expectsConflict(Scenario scenario, Layout layout) {
        return switch (scenario) {
            case SEPARATED -> false;
            case OVERLAP, RETARGET -> true;
            case MOVE_EDIT -> layout == Layout.SUBJECT;
        };
    }

    private static void resolveConflicts(Path repository, Scenario scenario, List<String> conflicts)
            throws Exception {
        List<String> checkout = new ArrayList<>(List.of("checkout", "--ours", "--"));
        checkout.addAll(conflicts);
        git(repository, "Administrator", checkout.toArray(String[]::new));
        if (scenario == Scenario.MOVE_EDIT || scenario == Scenario.RETARGET) {
            applyAuthorB(repository.resolve("requirements"), scenario);
        }
    }

    private static Verification verifyBranch(Path repository, Path formatter, Path validator, Path trace, String id)
            throws Exception {
        Path requirements = repository.resolve("requirements");
        Invocation formatted = invoke(repository, Map.of(), formatter, "--check", requirements.toString());
        assertStatus(0, formatted, "format check");
        Invocation validation = invoke(repository, Map.of(), validator, requirements.toString());
        assertStatus(0, validation, "validation");
        int expectedRequirements = Files.exists(requirements.resolve("EDGE-ALERT-QUEUE-001.mreq"))
                || containsRecord(requirements, "EDGE-ALERT-QUEUE-001") ? 61 : 60;
        int expectedRelationships = expectedRequirements == 61 ? 55 : 54;
        int expectedFiles = countSources(requirements);
        assertEquals("Validated %d requirements and %d decomposition relationships from %d files as mundanereq-source-0.2.%n"
                        .formatted(expectedRequirements, expectedRelationships, expectedFiles),
                validation.out(), "exact validation counts");
        Invocation traced = invoke(repository, Map.of(), trace, "higher", id, requirements.toString());
        assertStatus(0, traced, "trace");
        return new Verification(formatted, validation, traced);
    }

    private static void appendEvidence(
            StringBuilder evidence,
            Scenario scenario,
            Layout layout,
            int baselineFiles,
            Verification authorA,
            Verification authorB,
            Invocation secondMerge,
            int conflictedFiles,
            int conflictHunks,
            String conflictDiff,
            String resolution,
            Verification finalVerification,
            String changed,
            String diff,
            String history) {
        evidence.append("=== ").append(scenario).append(" / ").append(layout).append(" ===\n")
                .append("baseline source files: ").append(baselineFiles).append('\n')
                .append("Author A source operation: ").append(authorOperation(scenario, true, layout)).append('\n')
                .append("Author B source operation: ").append(authorOperation(scenario, false, layout)).append('\n')
                .append("Author A tool commands:\n");
        appendToolEvidence(evidence, authorA);
        evidence.append("Author B tool commands:\n");
        appendToolEvidence(evidence, authorB);
        evidence.append("merge Author A status: 0\n")
                .append("merge Author B status: ").append(secondMerge.status()).append('\n')
                .append("merge Author B stdout:\n").append(secondMerge.out().isEmpty() ? "(empty)\n" : secondMerge.out())
                .append("merge Author B stderr:\n").append(secondMerge.err().isEmpty() ? "(empty)\n" : secondMerge.err())
                .append("conflicted files: ").append(conflictedFiles)
                .append("; conflict hunks: ").append(conflictHunks).append('\n')
                .append("conflict diff:\n").append(conflictDiff)
                .append("resolution: ").append(resolution).append('\n')
                .append("final tool commands:\n");
        appendToolEvidence(evidence, finalVerification);
        evidence.append("changed paths (git diff --name-status before..after):\n").append(changed)
                .append("ordinary source diff (git diff before..after):\n").append(diff)
                .append("history (git log --reverse --format=%an | %s before..after):\n").append(history)
                .append('\n');
    }

    private static void appendToolEvidence(StringBuilder evidence, Verification verification) {
        appendInvocationEvidence(evidence, "mundanereq-format --check requirements", verification.formatter());
        appendInvocationEvidence(evidence, "mundanereq-validate requirements", verification.validation());
        appendInvocationEvidence(evidence, "mundanereq-trace higher <scenario-id> requirements", verification.trace());
    }

    private static void appendInvocationEvidence(StringBuilder evidence, String command, Invocation invocation) {
        evidence.append("$ ").append(command).append('\n')
                .append("status: ").append(invocation.status()).append('\n')
                .append("stdout:\n").append(invocation.out().isEmpty() ? "(empty)\n" : invocation.out())
                .append("stderr:\n").append(invocation.err().isEmpty() ? "(empty)\n" : invocation.err());
    }

    private static String authorOperation(Scenario scenario, boolean authorA, Layout layout) {
        return switch (scenario) {
            case SEPARATED -> authorA
                    ? "replace 'provide a water-level measurement' with 'provide a compensated water-level measurement' in SENSOR-LEVEL-001"
                    : "add 'outcome' to the AUDIT-QUERY-001 query dimensions";
            case OVERLAP -> authorA
                    ? "replace 'within 60 seconds' with 'within 45 seconds' in SYS-ALERT-NOTIFY-001"
                    : "replace 'within 60 seconds' with 'within 30 seconds' in SYS-ALERT-NOTIFY-001";
            case MOVE_EDIT -> authorA
                    ? (layout == Layout.SUBJECT
                    ? "move EDGE-STORE-001 from 04-storage.mreq to 06-records.mreq"
                    : "move EDGE-STORE-001.mreq to relocated/EDGE-STORE-001.mreq")
                    : "replace 'nonvolatile local storage' with 'redundant nonvolatile local storage' in EDGE-STORE-001";
            case RETARGET -> authorA
                    ? "qualify 'each detected local hazard' in EDGE-ALERT-QUEUE-001"
                    : "retarget EDGE-ALERT-QUEUE-001 from SYS-ALERT-NOTIFY-001 to SYS-RESILIENT-STORE-001";
        };
    }

    private static boolean containsRecord(Path requirements, String id) throws IOException {
        try (var paths = Files.walk(requirements)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                if (contains(path, "requirement " + id + "\n")) return true;
            }
        }
        return false;
    }

    private static String traceId(Scenario scenario) {
        return switch (scenario) {
            case SEPARATED -> "SENSOR-LEVEL-001";
            case OVERLAP -> "SYS-ALERT-NOTIFY-001";
            case MOVE_EDIT -> "EDGE-STORE-001";
            case RETARGET -> "EDGE-ALERT-QUEUE-001";
        };
    }

    private static void assertDiff(Scenario scenario, String diff) {
        switch (scenario) {
            case SEPARATED -> {
                assertContains(diff, "compensated water-level", "separated author A diff");
                assertContains(diff, "target, or outcome", "separated author B diff");
            }
            case OVERLAP -> assertContains(diff, "within 45 seconds", "selected overlapping value");
            case MOVE_EDIT -> assertContains(diff, "redundant nonvolatile", "move/edit combined diff");
            case RETARGET -> {
                assertContains(diff, "detected and qualified", "retarget author A diff");
                assertContains(diff, "SYS-RESILIENT-STORE-001", "retarget author B diff");
            }
        }
    }

    private static Invocation git(Path directory, String author, String... arguments) throws Exception {
        Invocation result = gitAllowFailure(directory, author, arguments);
        assertStatus(0, result, "git " + String.join(" ", arguments));
        return result;
    }

    private static Invocation gitAllowFailure(Path directory, String author, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of(
                "-c", "commit.gpgSign=false",
                "-c", "tag.gpgSign=false",
                "-c", "core.autocrlf=false",
                "-c", "core.hooksPath=/dev/null"));
        command.addAll(List.of(arguments));
        Map<String, String> environment = new java.util.HashMap<>(ISOLATED_GIT);
        String token = author.replace(" ", "").toLowerCase();
        environment.put("GIT_AUTHOR_NAME", author);
        environment.put("GIT_AUTHOR_EMAIL", token + "@example.invalid");
        environment.put("GIT_COMMITTER_NAME", author);
        environment.put("GIT_COMMITTER_EMAIL", token + "@example.invalid");
        environment.put("GIT_AUTHOR_DATE", "2026-08-29T12:00:00Z");
        environment.put("GIT_COMMITTER_DATE", "2026-08-29T12:00:00Z");
        return invoke(directory, environment, Path.of("git"), command.toArray(String[]::new));
    }

    private static Invocation invoke(
            Path directory, Map<String, String> environment, Path executable, String... arguments) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(executable.toString());
        command.addAll(List.of(arguments));
        ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile());
        builder.environment().putAll(environment);
        Process process = builder.start();
        try (var readers = Executors.newFixedThreadPool(2)) {
            Future<byte[]> out = readers.submit(() -> process.getInputStream().readAllBytes());
            Future<byte[]> err = readers.submit(() -> process.getErrorStream().readAllBytes());
            int status = process.waitFor();
            return new Invocation(status, new String(out.get(), StandardCharsets.UTF_8),
                    new String(err.get(), StandardCharsets.UTF_8));
        }
    }

    private static Path executable(String value) {
        Path path = Path.of(value).toAbsolutePath().normalize();
        if (!Files.isExecutable(path)) throw new AssertionError("not executable: " + path);
        return path;
    }

    private static int countSources(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            return (int) paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".mreq")).count();
        }
    }

    private static int countOccurrences(String value, String token) {
        int count = 0;
        for (int index = value.indexOf(token); index >= 0; index = value.indexOf(token, index + token.length())) count++;
        return count;
    }

    private static void printOutcome(Scenario scenario, Layout layout, Outcome outcome) {
        System.out.printf("OBSERVED %s %s baseline-files=%d final-files=%d changed-paths=%d conflicted-files=%d conflict-hunks=%d fingerprint=%s%n",
                scenario, layout, outcome.baselineFiles(), outcome.finalFiles(), outcome.changedPaths(),
                outcome.conflictedFiles(), outcome.conflictHunks(), outcome.fingerprint());
    }

    private static void assertObservedShape(Scenario scenario, Layout layout, Outcome actual) {
        int[] expected = switch (scenario) {
            case SEPARATED -> layout == Layout.SUBJECT
                    ? new int[] {6, 6, 2, 0, 0} : new int[] {60, 60, 2, 0, 0};
            case OVERLAP -> layout == Layout.SUBJECT
                    ? new int[] {6, 6, 1, 1, 1} : new int[] {60, 60, 1, 1, 1};
            case MOVE_EDIT -> layout == Layout.SUBJECT
                    ? new int[] {6, 6, 2, 1, 1} : new int[] {60, 60, 2, 0, 0};
            case RETARGET -> layout == Layout.SUBJECT
                    ? new int[] {6, 6, 1, 1, 1} : new int[] {61, 61, 1, 1, 1};
        };
        assertEquals(expected[0], actual.baselineFiles(), scenario + " " + layout + " baseline files");
        assertEquals(expected[1], actual.finalFiles(), scenario + " " + layout + " final files");
        assertEquals(expected[2], actual.changedPaths(), scenario + " " + layout + " changed paths");
        assertEquals(expected[3], actual.conflictedFiles(), scenario + " " + layout + " conflicted files");
        assertEquals(expected[4], actual.conflictHunks(), scenario + " " + layout + " conflict hunks");
    }

    private static void assertStatus(int expected, Invocation actual, String description) {
        if (expected != actual.status()) throw new AssertionError(
                "%s: expected %d, got %d; stdout <%s>; stderr <%s>".formatted(
                        description, expected, actual.status(), actual.out(), actual.err()));
    }

    private static void assertContains(String actual, String expected, String description) {
        if (!actual.contains(expected)) throw new AssertionError(
                "%s: expected <%s> in <%s>".formatted(description, expected, actual));
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!expected.equals(actual)) throw new AssertionError(
                "%s: expected <%s>, got <%s>".formatted(description, expected, actual));
    }

    private static void assertTrue(boolean actual, String description) {
        if (!actual) throw new AssertionError(description);
    }

    private static void deleteTree(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }
}
