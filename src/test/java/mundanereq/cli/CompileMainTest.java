package mundanereq.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import mundanereq.Interpreter;
import mundanereq.compile.SemanticArtifact;
import mundanereq.SourceFormat;

/** Observable compiler failures and exact parser-retained provenance. */
public final class CompileMainTest {
    private CompileMainTest() {}

    public static void run() throws Exception {
        String root = "specification/examples/requirements-artifact-0.1/valid";
        String[] args = {"--source=yaml-0.3", "--root", root, root};
        for (int limit : List.of(0, 37, Integer.MAX_VALUE)) {
            var failure = new PrintStream(new OutputStream() {
                private int count;
                @Override public void write(int value) throws IOException {
                    if (count++ >= limit) throw new IOException("injected partial output");
                }
                @Override public void flush() throws IOException { throw new IOException("injected flush failure"); }
            }, false, StandardCharsets.UTF_8);
            require(CompileMain.run(args, failure, sink()) == 2, "failed output must not succeed");
        }
        PrintStream closed = sink(); closed.close();
        require(CompileMain.run(args, closed, sink()) == 2, "closed stdout");
        closed = sink(); closed.close();
        require(CompileMain.run(args, sink(), closed) == 2, "closed stderr");
        for (String[] invocation : List.of(new String[]{}, new String[]{"--source=unknown"},
                new String[]{"--root", root, "--root", root, root},
                new String[]{"--root", root, "README.md"}, new String[]{"--unknown"})) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            require(CompileMain.run(invocation, new PrintStream(bytes), sink()) == 2, "invalid invocation status");
            require(bytes.size() == 0, "invalid invocation must not emit artifact");
        }
        Path missing = Path.of(root, "missing.mreq.yaml");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        require(CompileMain.run(new String[]{"--source=yaml-0.3","--root",root,missing.toString()},
                new PrintStream(bytes), sink()) == 2, "input failure status");
        require(bytes.toString(StandardCharsets.UTF_8).contains("\"phase\":\"input\""), "input failure artifact");
        // Custom spans count supplementary characters as one, and preserve repeated reference values.
        String text = "requirement A\ntitle: Parent 😀\nstatement:\n  Shall one.\nend requirement\n\n"
                + "requirement B\ntitle: Child\nstatement:\n  Shall two.\n\n  math latex\n    x = 1\n\n    y = 2\n  end math\nrationale:\n  Because.\ndecomposes: A\ndecomposes: B\nend requirement\n";
        var result = Interpreter.interpretSources(List.of(new Interpreter.Source("test.mreq", text.getBytes(StandardCharsets.UTF_8))));
        require(result.valid(), "custom span fixture valid");
        var first = result.origins().getFirst();
        require(first.fields().get("title").getFirst().start().column() == 8, "title start");
        require(first.fields().get("title").getFirst().end().column() == 16, "Unicode code-point end");
        var second = result.origins().get(1);
        require(second.fields().get("decomposes").size() == 2, "repeated field spans");
        require(second.references().get("A").start().line() == 19, "authored target line");
        require(second.record().end().line() == 21 && second.record().end().column() == 16, "record exclusive end");
        require(second.fields().get("statement").getFirst().end().line() == 16, "body range");
        // Serialization fails explicitly rather than emitting invalid Unicode or an invented origin.
        try {
            var originless = new Interpreter.Result(result.requirements(), result.byId(), result.outgoing(), List.of(), 1);
            SemanticArtifact.emit(List.of(), originless, SourceFormat.CUSTOM_02);
            throw new AssertionError("missing origin serialized");
        } catch (IllegalArgumentException expected) { /* explicit failure */ }
        // Bytes already selected are the snapshot even if the original file changes afterward.
        Path temporary = Files.createTempDirectory("compiled-snapshot-");
        try {
            Path file = temporary.resolve("source.mreq"); Files.writeString(file, text);
            var selected = Interpreter.selectInputs(List.of(file));
            Files.writeString(file, "external edit\n");
            var snapshot = Interpreter.interpretSources(selected.sources());
            String output = new String(SemanticArtifact.emit(selected.sources(), snapshot, SourceFormat.CUSTOM_02), StandardCharsets.UTF_8);
            require(output.contains("Shall one.") && !output.contains("external edit"), "single selected snapshot");
        } finally { Files.deleteIfExists(temporary.resolve("source.mreq")); Files.delete(temporary); }
        System.out.println("PASS compiler output faults, invocation/input errors, exact retained spans and selected snapshots");
    }

    private static PrintStream sink() { return new PrintStream(new ByteArrayOutputStream(), false, StandardCharsets.UTF_8); }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
