import java.nio.file.Path;
import java.util.*;
import mundanereq.Interpreter;
import mundanereq.SourceFormat;

/** Experiment only; consumers see JSON, never these implementation classes. */
public final class RequirementAdapter {
    static String quote(String value) {
        if (value == null) return "null";
        StringBuilder b = new StringBuilder("\"");
        value.codePoints().forEach(c -> {
            if (c == '"' || c == '\\') b.append('\\').appendCodePoint(c);
            else if (c < 32) b.append(String.format("\\u%04x", c));
            else b.appendCodePoint(c);
        });
        return b.append('"').toString();
    }
    static String blocks(List<Interpreter.ContentBlock> blocks) {
        if (blocks == null) return "null";
        return "[" + String.join(",", blocks.stream().map(b -> switch (b) {
            case Interpreter.ProseBlock p -> "{\"prose\":" + quote(p.text()) + "}";
            case Interpreter.MathBlock m -> "{\"language\":" + quote(m.language()) + ",\"math\":" + quote(m.payload()) + "}";
        }).toList()) + "]";
    }
    public static void main(String[] args) {
        var result = Interpreter.interpretInputs(List.of(Path.of(args[0])), SourceFormat.YAML_03);
        var records = new ArrayList<String>();
        if (result.valid()) for (var r : result.requirements().stream().sorted(Comparator.comparing(Interpreter.Requirement::id)).toList()) {
            records.add("{\"id\":"+quote(r.id())+",\"title\":"+quote(r.title())+",\"allocation\":"+quote(r.allocation())
                +",\"statement\":"+blocks(r.statement())+",\"rationale\":"+blocks(r.rationale())+",\"source\":"+quote(r.source())
                +",\"decomposes\":["+String.join(",",r.decomposes().stream().sorted().map(RequirementAdapter::quote).toList())+"]}");
        }
        System.out.println("{\"format\":\"experiment-0027-requirements-1\",\"complete\":"+result.valid()
            +",\"requirements\":["+String.join(",",records)+"],\"diagnostics\":["
            +String.join(",",result.diagnostics().stream().map(d -> quote(d.code())).toList())+"]}");
    }
}
