package engineering.verification;

import static engineering.artifacts.Checks.*;
import engineering.artifacts.Artifacts;
import engineering.artifacts.Json;
import engineering.artifacts.Linker;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import mundanereq.Versions;

/** Planned coverage and review basis only; no test execution or satisfaction inference. */
public final class Verifier {
    private Verifier() {}
    public record Result(Map<String,Object> output,int status) {}
    public static Result analyze(Linker.Result resolved) {
        var linked=resolved.output();
        Map<String,Object> output=Json.object("format",Versions.VERIFICATION_ARTIFACT,"complete",false,"context",linked.get("context"),
                "analyzer",Json.object("name","mundane-verify","version",Versions.VERIFY_VERSION,"contract",Versions.VERIFY_CONTRACT),
                "linked",linked,"coverage",List.of(),"uncovered",List.of(),"diagnostics",linked.get("diagnostics"));
        if(resolved.status()!=0)return new Result(output,2);
        Map<String,Map<String,Map<String,Object>>> records=new TreeMap<>();
        for(Object value:list(linked.get("imports"))) {var imported=map(value);records.put(text(imported.get("scope")),Artifacts.requirements(map(imported.get("artifact")),text(imported.get("path"))));}
        List<Map<String,Object>> coverage=new ArrayList<>(),uncovered=new ArrayList<>();Set<String> covered=new HashSet<>();boolean stale=false;
        for(Object value:list(linked.get("edges"))) {
            var edge=map(value);String id=text(edge.get("requirementId"));
            var before=Artifacts.values(records.get(text(edge.get("baselineScope"))).get(id));var after=Artifacts.values(records.get(text(edge.get("currentScope"))).get(id));
            List<String> changed=Artifacts.VALUE_FIELDS.stream().filter(f->!java.util.Objects.equals(before.get(f),after.get(f))).sorted().toList();
            var row=new TreeMap<>(edge);row.put("state",changed.isEmpty()?"current":"review-stale");row.put("changedFields",changed);row.put("possibleImpact",!changed.isEmpty());coverage.add(row);
            stale|=!changed.isEmpty();covered.add(edge.get("planId")+":"+id);
        }
        var plan=map(map(linked.get("planArtifact")).get("artifact"));Set<?> selected=new HashSet<>(list(linked.get("plans")));
        for(Object value:list(plan.get("plans"))) {
            var p=map(value);String id=text(p.get("id"));if(!selected.contains(id))continue;
            String scope=p.get("currentScope")==null?records.keySet().iterator().next():text(p.get("currentScope"));
            for(var entry:records.get(scope).entrySet()) if(!covered.contains(id+":"+entry.getKey())) {
                var span=map(map(entry.getValue().get("locations")).get("record"));var point=map(span.get("start"));
                uncovered.add(Json.object("planId",id,"context",p.get("context"),"scope",scope,"requirementId",entry.getKey(),
                        "location",Json.object("path",scope+":"+span.get("path"),"line",point.get("line"),"column",point.get("column"))));
            }
        }
        uncovered.sort(Comparator.comparing((Map<String,Object> u)->text(u.get("planId"))).thenComparing(u->text(u.get("scope"))).thenComparing(u->text(u.get("requirementId"))));
        output.put("complete",true);output.put("coverage",coverage);output.put("uncovered",uncovered);
        return new Result(output,stale||!uncovered.isEmpty()?1:0);
    }
}
