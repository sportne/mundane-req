package engineering.artifacts;

import static engineering.artifacts.Checks.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import mundanereq.Versions;

/** Validation and selected semantic projection for the two published artifact kinds. */
public final class Artifacts {
    public static final List<String> VALUE_FIELDS=List.of("id","title","allocation","statement","rationale","source","decomposes");
    private Artifacts() {}
    private static Set<String> envelope(Map<String,Object> a,String kind,String format,String file) {
        if(!kind.equals(a.get("artifactKind"))) throw new Problem("wrong-kind","expected "+kind,file);
        if(!format.equals(a.get("format"))) throw new Problem("unsupported-format","unsupported artifact format",file);
        if(!Boolean.TRUE.equals(a.get("complete"))) throw new Problem("incomplete-import","artifact is not complete",file);
        if(!list(a.get("diagnostics")).isEmpty()) throw new IllegalArgumentException("complete artifact has diagnostics");
        Map<String,Object> compiler=map(a.get("compiler"));text(compiler.get("name"));text(compiler.get("version"));text(compiler.get("contract"));
        Set<String> sources=new HashSet<>();
        for(Object source:list(a.get("sources"))) {
            var s=map(source);if(!sources.add(path(s.get("path")))) throw new IllegalArgumentException("duplicate source path");digest(s.get("sha256"));
        }
        if(sources.isEmpty()) throw new IllegalArgumentException("empty source inventory");return sources;
    }
    public static Map<String,Map<String,Object>> requirements(Map<String,Object> a,String file) {
        Set<String> paths=envelope(a,"requirements",Versions.REQUIREMENT_ARTIFACT,file);
        if(!Set.of(Versions.SOURCE_CUSTOM,Versions.SOURCE_YAML).contains(text(a.get("sourceContract")))) throw new Problem("unsupported-format","unsupported requirement source",file);
        Map<String,Map<String,Object>> records=new TreeMap<>();
        for(Object record:list(a.get("requirements"))) {
            var r=map(record);var v=map(r.get("values"));required(v,VALUE_FIELDS.toArray(String[]::new));String id=id(v.get("id"));
            if(records.put(id,r)!=null) throw new IllegalArgumentException("duplicate requirement ID");
            text(v.get("title"));for(String field:List.of("allocation","source")) if(v.get(field)!=null) text(v.get(field));
            blocks(v.get("statement"),true);if(v.get("rationale")!=null) blocks(v.get("rationale"),false);
            Set<String> targets=new HashSet<>();for(Object target:list(v.get("decomposes"))) if(!targets.add(id(target))) throw new IllegalArgumentException("duplicate target");
            var loc=map(r.get("locations"));span(loc.get("record"),paths);var fields=map(loc.get("fields"));required(fields,"id","title","statement");
            for(var entry:fields.entrySet()) { var spans=list(entry.getValue());if(spans.isEmpty()) throw new IllegalArgumentException("empty field spans");for(Object s:spans) span(s,paths); }
            for(String field:List.of("allocation","rationale","source")) if(v.get(field)!=null) required(fields,field);
            if(!targets.isEmpty()) required(fields,"decomposes");var references=map(loc.get("references"));
            if(!references.keySet().equals(targets)) throw new IllegalArgumentException("reference locations do not match targets");
            for(Object s:references.values()) span(s,paths);
        }
        if(records.isEmpty()) throw new IllegalArgumentException("empty requirements");
        for(var r:records.values()) for(Object target:list(map(r.get("values")).get("decomposes"))) if(!records.containsKey(target)) throw new IllegalArgumentException("unresolved decomposition");
        return records;
    }
    private static void blocks(Object value,boolean math) {
        List<?> blocks=list(value);if(blocks.isEmpty()) throw new IllegalArgumentException("empty body");
        for(Object block:blocks) {
            var b=map(block);
            if("prose".equals(b.get("kind"))) text(b.get("text"));
            else if(math && "math".equals(b.get("kind")) && "latex".equals(b.get("language"))) {
                String payload=text(b.get("payload"));if(payload.chars().allMatch(c->c=='\n')) throw new IllegalArgumentException("empty math");
            } else throw new IllegalArgumentException("unsupported content block");
        }
    }
    public static void plan(Map<String,Object> a,String file) {
        Set<String> paths=envelope(a,"verification-plan",Versions.PLAN_ARTIFACT,file);
        keys(a,"artifactKind","format","sourceContract","compiler","complete","sources","plans","activities","coverage","diagnostics");
        if(!Versions.PLAN_SOURCE.equals(a.get("sourceContract"))) throw new Problem("unsupported-format","unsupported plan source",file);
        Set<String> plans=new HashSet<>(),activities=new HashSet<>(),rows=new HashSet<>();
        for(Object value:list(a.get("plans"))) {
            var p=map(value);keys(p,"id","context","baselineScope","currentScope","location");
            if(!plans.add(id(p.get("id")))) throw new IllegalArgumentException("duplicate plan");text(p.get("context"));
            for(String scope:List.of("baselineScope","currentScope")) if(p.get(scope)!=null) id(p.get(scope));location(p.get("location"),paths);
        }
        for(Object value:list(a.get("activities"))) {
            var activity=map(value);keys(activity,"id","method","objective","expectedEvidence","location");
            if(!activities.add(id(activity.get("id")))) throw new IllegalArgumentException("duplicate activity");
            if(!Set.of("test","analysis","inspection","demonstration","review").contains(text(activity.get("method")))) throw new IllegalArgumentException("unknown activity method");
            text(activity.get("objective"));text(activity.get("expectedEvidence"));location(activity.get("location"),paths);
        }
        if(plans.isEmpty()||activities.isEmpty()) throw new IllegalArgumentException("empty plans or activities");
        for(Object value:list(a.get("coverage"))) {
            var row=map(value);keys(row,"planId","activityId","requirementId","location");
            String p=id(row.get("planId")),activity=id(row.get("activityId")),requirement=id(row.get("requirementId"));
            var loc=location(row.get("location"),paths);
            if(!plans.contains(p)) throw new IllegalArgumentException("unknown plan reference");
            if(!activities.contains(activity)) throw new Problem("missing-activity","unknown activity "+activity,qualified(loc,"plan"));
            if(!rows.add(p+":"+activity+":"+requirement)) throw new IllegalArgumentException("duplicate coverage row");
        }
    }
    public static Map<String,Object> values(Map<String,Object> record) {
        var raw=map(record.get("values"));Map<String,Object> result=new TreeMap<>();
        for(String field:VALUE_FIELDS) result.put(field,raw.get(field));
        result.put("decomposes",new java.util.ArrayList<>(new TreeSet<>(list(raw.get("decomposes")).stream().map(Checks::text).toList())));
        // Ignore informational additions inside supported blocks as the requirement contract permits.
        for(String field:List.of("statement","rationale")) if(raw.get(field)!=null) result.put(field,list(raw.get(field)).stream().map(x->{var b=map(x);return "prose".equals(b.get("kind"))?Json.object("kind","prose","text",b.get("text")):Json.object("kind","math","language",b.get("language"),"payload",b.get("payload"));}).toList());
        return result;
    }
    public static Map<String,Object> qualified(Map<String,Object> loc,String scope) { return Json.object("path",scope+":"+loc.get("path"),"line",loc.get("line"),"column",loc.get("column")); }
}
