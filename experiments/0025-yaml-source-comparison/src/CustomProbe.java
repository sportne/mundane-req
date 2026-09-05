import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mundanereq.Interpreter;
import mundanereq.format.SourceFormatter;
import mundanereq.source.SourceDocument;

/** Calls maintained code; does not reimplement .mreq parsing. */
public final class CustomProbe {
    private CustomProbe() {}
    public static void main(String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("inspect INPUT... | format INPUT");
        if (args[0].equals("format")) {
            // Formatting fixtures are prevalidated as a complete source set by the harness.
            var bytes = SourceFormatter.format(SourceDocument.read(args[1], Files.readAllBytes(Path.of(args[1]))));
            System.out.write(bytes);
        } else {
            var result = Interpreter.interpretInputs(Arrays.stream(args).skip(1).map(Path::of).toList());
            var requirements = new ArrayList<Map<String,Object>>();
            for (var r : result.requirements().stream().sorted(java.util.Comparator.comparing(Interpreter.Requirement::id)).toList()) {
                Map<String,Object> item = new LinkedHashMap<>();
                item.put("id",r.id()); item.put("title",r.title()); item.put("allocation",r.allocation());
                item.put("statement",blocks(r.statement())); item.put("rationale",blocks(r.rationale()));
                item.put("source",r.source()); item.put("decomposes",r.decomposes().stream().sorted().toList());
                requirements.add(item);
            }
            var diagnostics = result.diagnostics().stream().map(d -> Map.of("file",Path.of(d.file()).getFileName().toString(),
                    "line",d.line(),"column",d.column(),"code",d.code(),"message",d.message())).toList();
            System.out.println(Json.write(Map.of("valid",result.valid(),"requirements",requirements,"diagnostics",diagnostics)));
        }
        System.out.flush();
        if (System.out.checkError()) System.exit(2);
    }
    private static Object blocks(List<Interpreter.ContentBlock> blocks) {
        if (blocks==null) return null;
        var result=new ArrayList<List<String>>();
        for(var b:blocks) {
            if(b instanceof Interpreter.ProseBlock p) result.add(List.of("prose",p.text()));
            else if(b instanceof Interpreter.MathBlock m) result.add(List.of("math",m.language(),m.payload()));
        }
        return result;
    }
}
