package mundanereq.trace;

import java.nio.charset.StandardCharsets;
import java.util.List;
import mundanereq.Interpreter;

/** Focused graph checks independent of command rendering and traversal order. */
public final class TraceAnalyzerTest {
    private TraceAnalyzerTest() {}

    public static void run() {
        TraceAnalyzer analyzer = new TraceAnalyzer(result(graphSource()));

        assertEquals(List.of("A-NEAR-D", "Z-NEAR-D"), analyzer.parents("D"), "direct parents");
        assertEquals(
                List.of("A-NEAR-TOP", "Z-NEAR-TOP"), analyzer.children("TOP"), "direct children");

        TraceAnalyzer.TransitiveResult higher = analyzer.higher("D");
        assertEquals(
                List.of(
                        new TraceAnalyzer.PathResult("A-NEAR-D", List.of("D", "A-NEAR-D")),
                        new TraceAnalyzer.PathResult("Z-NEAR-D", List.of("D", "Z-NEAR-D")),
                        new TraceAnalyzer.PathResult(
                                "A-NEAR-TOP", List.of("D", "Z-NEAR-D", "A-NEAR-TOP")),
                        new TraceAnalyzer.PathResult(
                                "Z-NEAR-TOP", List.of("D", "A-NEAR-D", "Z-NEAR-TOP")),
                        new TraceAnalyzer.PathResult(
                                "TOP", List.of("D", "A-NEAR-D", "Z-NEAR-TOP", "TOP"))),
                higher.paths(),
                "higher paths and whole-path tie break");
        assertEquals(List.of(), higher.cycles(), "acyclic higher scope");

        TraceAnalyzer.TransitiveResult impact = analyzer.impact("TOP");
        assertEquals(
                new TraceAnalyzer.PathResult(
                        "D", List.of("D", "A-NEAR-D", "Z-NEAR-TOP", "TOP")),
                impact.paths().getLast(),
                "impact whole-path tie break");

        TraceAnalyzer.TransitiveResult cycle = analyzer.higher("CYCLE-A");
        assertEquals(
                List.of(new TraceAnalyzer.PathResult("CYCLE-B", List.of("CYCLE-A", "CYCLE-B"))),
                cycle.paths(),
                "cycle excludes query from transitive results");
        assertEquals(List.of(List.of("CYCLE-A", "CYCLE-B")), cycle.cycles(), "cycle component");
        assertEquals(List.of("SELF"), analyzer.parents("SELF"), "direct self relationship");
        assertEquals(List.of(List.of("SELF")), analyzer.higher("SELF").cycles(), "self-cycle component");
        assertEquals(List.of(), analyzer.children("DISCONNECTED"), "disconnected direct result");
    }

    public static String graphSource() {
        return requirement("TOP", List.of())
                + requirement("A-NEAR-TOP", List.of("TOP"))
                + requirement("Z-NEAR-TOP", List.of("TOP"))
                + requirement("Z-NEAR-D", List.of("A-NEAR-TOP"))
                + requirement("A-NEAR-D", List.of("Z-NEAR-TOP"))
                + requirement("D", List.of("Z-NEAR-D", "A-NEAR-D"))
                + requirement("CYCLE-A", List.of("CYCLE-B"))
                + requirement("CYCLE-B", List.of("CYCLE-A"))
                + requirement("SELF", List.of("SELF"))
                + requirement("DISCONNECTED", List.of());
    }

    private static String requirement(String id, List<String> parents) {
        StringBuilder source = new StringBuilder()
                .append("requirement ").append(id).append('\n')
                .append("title: ").append(id).append('\n')
                .append("statement:\n  Required.\n");
        parents.forEach(parent -> source.append("decomposes: ").append(parent).append('\n'));
        return source.append("end requirement\n\n").toString();
    }

    private static Interpreter.Result result(String source) {
        Interpreter.Result result = Interpreter.interpretSources(List.of(
                new Interpreter.Source("trace-graph.mreq", source.getBytes(StandardCharsets.UTF_8))));
        if (!result.valid()) throw new AssertionError("trace fixture diagnostics: " + result.diagnostics());
        return result;
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("%s: expected <%s> but was <%s>".formatted(description, expected, actual));
        }
    }
}
