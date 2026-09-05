package engineering.artifacts;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Structural checks at the compiled boundary; no dependency on source-language code. */
public final class Checks {
    private Checks() {}
    public static Map<String,Object> map(Object value) {
        if (!(value instanceof Map<?,?> raw)) throw new IllegalArgumentException("expected object");
        Map<String,Object> result=new java.util.TreeMap<>();
        raw.forEach((key,item)->{ if(!(key instanceof String text)) throw new IllegalArgumentException("non-string key"); result.put(text,item); });
        return result;
    }
    public static List<?> list(Object value) { if(value instanceof List<?> list) return list; throw new IllegalArgumentException("expected array"); }
    public static String text(Object value) { if(value instanceof String text && !text.isEmpty()) return text; throw new IllegalArgumentException("expected nonempty string"); }
    public static String id(Object value) { String text=text(value); if(!text.matches("[A-Za-z0-9][A-Za-z0-9._-]*")) throw new IllegalArgumentException("invalid ID/scope"); return text; }
    public static String digest(Object value) { String text=text(value); if(!text.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("invalid SHA-256"); return text; }
    public static String path(Object value) {
        String text=text(value);
        if(text.startsWith("/")||text.contains("\\")||text.contains(":")) throw new IllegalArgumentException("invalid relative path");
        for(String part:text.split("/",-1)) if(part.isEmpty()||part.equals(".")||part.equals("..")) throw new IllegalArgumentException("invalid relative path");
        if(text.codePoints().anyMatch(c->c<32)) throw new IllegalArgumentException("control in path");
        return text;
    }
    public static int integer(Object value) {
        if(!(value instanceof Number number)) throw new IllegalArgumentException("expected integer coordinate");
        try { int n=new java.math.BigDecimal(number.toString()).intValueExact(); if(n<1) throw new IllegalArgumentException("coordinate must be positive"); return n; }
        catch(ArithmeticException ex) { throw new IllegalArgumentException("invalid coordinate",ex); }
    }
    public static void keys(Map<String,Object> map,String... names) {
        if(!map.keySet().equals(Set.of(names))) throw new IllegalArgumentException("unexpected or missing fields; expected "+String.join(",",names));
    }
    public static void required(Map<String,Object> map,String... names) {
        for(String name:names) if(!map.containsKey(name)) throw new IllegalArgumentException("missing field "+name);
    }
    public static Map<String,Object> location(Object value,Set<String> paths) {
        Map<String,Object> loc=map(value); keys(loc,"path","line","column");
        String file=path(loc.get("path")); if(!paths.contains(file)) throw new IllegalArgumentException("location not in source inventory");
        integer(loc.get("line")); integer(loc.get("column")); return loc;
    }
    public static void span(Object value,Set<String> paths) {
        Map<String,Object> span=map(value); required(span,"path","start","end");
        if(!paths.contains(path(span.get("path")))) throw new IllegalArgumentException("span not in source inventory");
        var a=map(span.get("start"));var b=map(span.get("end"));
        int al=integer(a.get("line")),ac=integer(a.get("column")),bl=integer(b.get("line")),bc=integer(b.get("column"));
        if(bl<al || bl==al && bc<ac) throw new IllegalArgumentException("reversed span");
    }
}
