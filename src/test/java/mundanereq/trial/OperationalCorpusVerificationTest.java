package mundanereq.trial;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;
import mundanereq.Interpreter;

/** Verifies the frozen scale and trace shape selected by TC-0701. */
public final class OperationalCorpusVerificationTest {
    private static final Path CORPUS = Path.of("experiments/0011-operational-corpus/requirements");
    private static final String EXPECTED_SEMANTIC_SHA256 =
            "ad21fd88cd3a10d0dfcb66ccdf6ab05b282737ee94f925b61751e7c8acf390c3";

    private OperationalCorpusVerificationTest() {}

    public static void run() {
        Interpreter.Result result = Interpreter.interpretInputs(java.util.List.of(CORPUS));
        assertEquals(java.util.List.of(), result.diagnostics(), "corpus diagnostics");
        assertEquals(6, result.fileCount(), "source files");
        assertEquals(60, result.requirements().size(), "requirements");
        int relationships = result.outgoing().values().stream().mapToInt(Set::size).sum();
        assertEquals(54, relationships, "relationships");

        long roots = result.requirements().stream().filter(requirement -> requirement.id().startsWith("OPS-")).count();
        long systems = result.requirements().stream().filter(requirement -> requirement.id().startsWith("SYS-")).count();
        assertEquals(6L, roots, "operational roots");
        assertEquals(18L, systems, "system requirements");
        assertEquals(36L, 60L - roots - systems, "component requirements");

        java.util.Map<String, Integer> childCounts = new java.util.HashMap<>();
        result.outgoing().forEach((child, parents) ->
                parents.forEach(parent -> childCounts.merge(parent, 1, Integer::sum)));
        int maximumDepth = 0;
        for (Interpreter.Requirement requirement : result.requirements()) {
            Set<String> parents = result.outgoing().getOrDefault(requirement.id(), Set.of());
            assertEquals(requirement.id().startsWith("OPS-") ? 0 : 1, parents.size(),
                    requirement.id() + " parent count");
            if (requirement.id().startsWith("OPS-")) {
                assertEquals(3, childCounts.getOrDefault(requirement.id(), 0), requirement.id() + " child count");
            } else if (requirement.id().startsWith("SYS-")) {
                assertEquals(2, childCounts.getOrDefault(requirement.id(), 0), requirement.id() + " child count");
            } else {
                assertEquals(0, childCounts.getOrDefault(requirement.id(), 0), requirement.id() + " child count");
            }
            maximumDepth = Math.max(maximumDepth, depth(requirement.id(), result.outgoing(), new HashSet<>()));
        }
        assertEquals(2, maximumDepth, "maximum decomposition path length");
        assertEquals(EXPECTED_SEMANTIC_SHA256, semanticFingerprint(result), "frozen semantic inventory SHA-256");
    }

    /** Test-only layout-independent semantic fingerprint used by Stage 7 trials. */
    public static String semanticFingerprint(Path corpus) {
        Interpreter.Result result = Interpreter.interpretInputs(java.util.List.of(corpus));
        assertEquals(java.util.List.of(), result.diagnostics(), corpus + " diagnostics");
        return semanticFingerprint(result);
    }

    private static int depth(String id, java.util.Map<String, Set<String>> outgoing, Set<String> visited) {
        if (!visited.add(id)) throw new AssertionError("cycle at " + id);
        Set<String> parents = outgoing.getOrDefault(id, Set.of());
        int result = parents.isEmpty() ? 0 : 1 + depth(parents.iterator().next(), outgoing, visited);
        visited.remove(id);
        return result;
    }

    private static String semanticFingerprint(Interpreter.Result result) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            result.requirements().stream()
                    .sorted(java.util.Comparator.comparing(Interpreter.Requirement::id))
                    .forEach(requirement -> {
                        add(digest, "requirement");
                        add(digest, requirement.id());
                        add(digest, requirement.title());
                        add(digest, requirement.allocation());
                        addBlocks(digest, requirement.statement());
                        addBlocks(digest, requirement.rationale());
                        add(digest, requirement.source());
                        requirement.decomposes().stream().sorted().forEach(parent -> add(digest, "parent:" + parent));
                        add(digest, "end");
                    });
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void addBlocks(MessageDigest digest, java.util.List<Interpreter.ContentBlock> blocks) {
        if (blocks == null) {
            add(digest, null);
            return;
        }
        for (Interpreter.ContentBlock block : blocks) {
            if (block instanceof Interpreter.ProseBlock prose) {
                add(digest, "prose:" + prose.text());
            } else if (block instanceof Interpreter.MathBlock math) {
                add(digest, "math:" + math.language());
                add(digest, math.payload());
            } else {
                throw new AssertionError("unknown content block " + block);
            }
        }
    }

    private static void add(MessageDigest digest, String value) {
        if (value == null) {
            digest.update((byte) 0);
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update((byte) 1);
        digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
        digest.update(bytes);
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!expected.equals(actual)) throw new AssertionError(
                "%s: expected <%s>, got <%s>".formatted(description, expected, actual));
    }
}
