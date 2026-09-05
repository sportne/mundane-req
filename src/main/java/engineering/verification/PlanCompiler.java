package engineering.verification;

import static engineering.artifacts.Checks.*;
import engineering.artifacts.Artifacts;
import engineering.artifacts.Json;
import engineering.artifacts.Problem;
import engineering.artifacts.Snapshots;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import mundanereq.Versions;

/** Three explicit TSV tables; independent of requirement source representation. */
public final class PlanCompiler {
    private PlanCompiler() {}
    public record Result(Map<String,Object> output,int status) {}
    public static Result compile(Path root,Path directory) {return compile(root,directory,()->{});}
    public static Result compile(Path root,Path directory,Runnable beforeRecheck) {
        Snapshots snapshots=new Snapshots(root);
        Map<String,Object> output=Json.object("artifactKind","verification-plan","format",Versions.PLAN_ARTIFACT,
                "sourceContract",Versions.PLAN_SOURCE,"compiler",Json.object("name","mundane-plan","version",Versions.PLAN_VERSION,"contract",Versions.PLAN_CONTRACT),
                "complete",false,"sources",List.of(),"plans",List.of(),"activities",List.of(),"coverage",List.of(),"diagnostics",List.of());
        List<Map<String,Object>> sources=new ArrayList<>(),plans=new ArrayList<>(),activities=new ArrayList<>(),coverage=new ArrayList<>();
        java.util.Set<String> planIds=new java.util.HashSet<>(),activityIds=new java.util.HashSet<>(),tuples=new java.util.HashSet<>();
        String current="plan.tsv";
        try {
            for(String file:List.of("activities.tsv","coverage.tsv","plan.tsv")) {
                current=snapshots.argument(directory.resolve(file));var snapshot=snapshots.read(current,8*1024*1024);
                sources.add(Json.object("path",current,"sha256",snapshot.sha256()));
                List<String[]> rows=table(snapshot);
                for(int i=1;i<rows.size();i++) {
                    String[] row=rows.get(i);var location=Json.object("path",current,"line",i+1,"column",1);
                    try {
                        switch(file) {
                            case "plan.tsv" -> {
                                if(!Versions.PLAN_SOURCE.equals(row[0])) throw new Problem("unsupported-format","unsupported plan source identifier",location);
                                if(!planIds.add(id(row[1])))throw new IllegalArgumentException("duplicate plan ID");
                                plans.add(Json.object("id",id(row[1]),"context",row[2],"baselineScope",row[3].isEmpty()?null:id(row[3]),"currentScope",row[4].isEmpty()?null:id(row[4]),"location",location));
                            }
                            case "activities.tsv" -> {
                                if(!activityIds.add(id(row[0])))throw new IllegalArgumentException("duplicate activity ID");
                                if(!java.util.Set.of("test","analysis","inspection","demonstration","review").contains(row[1]))throw new IllegalArgumentException("unknown activity method");
                                activities.add(Json.object("id",id(row[0]),"method",row[1],"objective",row[2],"expectedEvidence",row[3],"location",location));
                            }
                            case "coverage.tsv" -> {
                                if(!tuples.add(String.join(":",row)))throw new IllegalArgumentException("duplicate coverage assertion");
                                coverage.add(Json.object("planId",id(row[0]),"activityId",id(row[1]),"requirementId",id(row[2]),"location",location));
                            }
                            default -> throw new IllegalStateException(file);
                        }
                    } catch(IllegalArgumentException error) {throw new Problem("invalid-plan",error.getMessage(),location);}
                }
            }
            for(var row:coverage) if(!planIds.contains(row.get("planId"))||!activityIds.contains(row.get("activityId")))throw new Problem("invalid-plan","unknown plan or activity reference",map(row.get("location")));
            plans.sort(Comparator.comparing(p->text(p.get("id"))));activities.sort(Comparator.comparing(a->text(a.get("id"))));
            coverage.sort(Comparator.comparing((Map<String,Object> c)->text(c.get("planId"))).thenComparing(c->text(c.get("activityId"))).thenComparing(c->text(c.get("requirementId"))));
            Map<String,Object> candidate=new java.util.TreeMap<>(output);
            candidate.put("complete",true);candidate.put("sources",sources);candidate.put("plans",plans);candidate.put("activities",activities);candidate.put("coverage",coverage);
            Artifacts.plan(candidate,current);beforeRecheck.run();snapshots.recheck();return new Result(candidate,0);
        } catch(Problem problem) {output.put("sources",sources);output.put("diagnostics",List.of(problem.diagnostic()));return new Result(output,problem.operational()?2:1);}
        catch(IllegalArgumentException error) {output.put("sources",sources);output.put("diagnostics",List.of(new Problem("invalid-plan",error.getMessage(),current).diagnostic()));return new Result(output,1);}
    }
    private static List<String[]> table(Snapshots.Snapshot snapshot) {
        String text;
        try {text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(snapshot.bytes())).toString();}
        catch(java.nio.charset.CharacterCodingException error) {throw new Problem("invalid-plan","invalid UTF-8",snapshot.path());}
        if(!text.endsWith("\n")) throw new Problem("invalid-plan","table must end with LF or CRLF",snapshot.path());
        String file=Path.of(snapshot.path()).getFileName().toString();
        String header=switch(file){case "plan.tsv"->"format\tplan_id\tcontext\tbaseline_scope\tcurrent_scope";case "activities.tsv"->"activity_id\tmethod\tobjective\texpected_evidence";default->"plan_id\tactivity_id\trequirement_id";};
        String[] lines=text.substring(0,text.length()-1).split("\n",-1);
        if(lines.length>10001)throw new Problem("invalid-plan","table exceeds 10000 rows",snapshot.path());
        List<String[]> result=new ArrayList<>();int columns=header.split("\t").length;
        for(int i=0;i<lines.length;i++) {
            String line=lines[i].endsWith("\r")?lines[i].substring(0,lines[i].length()-1):lines[i];
            if(i==0&&!line.equals(header))throw new Problem("invalid-plan","wrong table header",snapshot.path());
            String[] cells=line.split("\t",-1);var location=Json.object("path",snapshot.path(),"line",i+1,"column",1);
            if(cells.length!=columns)throw new Problem("invalid-plan","wrong column count",location);
            for(int col=0;col<cells.length;col++) {
                String cell=cells[col];boolean optionalScope=i>0&&file.equals("plan.tsv")&&col>=3;
                if(cell.isEmpty()&&!optionalScope || !cell.equals(cell.strip()) || cell.codePoints().anyMatch(c->c<32||c>=127&&c<=159||c==0xfeff))throw new Problem("invalid-plan","empty, padded or control-containing cell",location);
            }
            result.add(cells);
        }
        return result;
    }
}
