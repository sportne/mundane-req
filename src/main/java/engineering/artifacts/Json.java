package engineering.artifacts;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Bounded strict JSON boundary, independent of every domain source parser. */
public final class Json {
    private final String source;
    private int at;
    private Json(String source) { this.source = source; }
    public static Object read(byte[] bytes) {
        try {
            String text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
            Json parser = new Json(text);
            Object value = parser.value(0); parser.space();
            if (parser.at != text.length()) throw new IllegalArgumentException("trailing JSON input");
            return value;
        } catch (java.nio.charset.CharacterCodingException error) { throw new IllegalArgumentException("invalid UTF-8", error); }
    }
    private void space() { while (at < source.length() && " \t\r\n".indexOf(source.charAt(at)) >= 0) at++; }
    private boolean take(char c) { space(); if (at < source.length() && source.charAt(at) == c) { at++; return true; } return false; }
    private void need(char c) { if (!take(c)) throw new IllegalArgumentException("expected '"+c+"' at offset "+at); }
    private Object value(int depth) {
        if (depth > 64) throw new IllegalArgumentException("JSON depth exceeds 64");
        space(); if (at == source.length()) throw new IllegalArgumentException("missing JSON value");
        char c = source.charAt(at);
        if (c == '"') return string();
        if (take('{')) {
            Map<String,Object> map = new TreeMap<>();
            if (take('}')) return map;
            do {
                space(); String key = string(); need(':');
                if (map.containsKey(key)) throw new IllegalArgumentException("duplicate JSON key: "+key);
                map.put(key, value(depth+1));
            } while (take(','));
            need('}'); return map;
        }
        if (take('[')) {
            List<Object> list = new ArrayList<>(); if (take(']')) return list;
            do { list.add(value(depth+1)); } while (take(','));
            need(']'); return list;
        }
        for (String literal : List.of("true","false","null")) if (source.startsWith(literal,at)) {
            at += literal.length(); return literal.equals("null") ? null : literal.equals("true");
        }
        int start = at;
        if (at < source.length() && source.charAt(at)=='-') at++;
        if (at >= source.length()) throw new IllegalArgumentException("invalid number");
        if (source.charAt(at)=='0') at++;
        else {
            if (source.charAt(at)<'1' || source.charAt(at)>'9') throw new IllegalArgumentException("invalid number");
            while (at<source.length() && source.charAt(at)>='0' && source.charAt(at)<='9') at++;
        }
        if (at<source.length() && source.charAt(at)=='.') { at++; digits(); }
        if (at<source.length() && (source.charAt(at)=='e' || source.charAt(at)=='E')) {
            at++; if (at<source.length() && (source.charAt(at)=='+' || source.charAt(at)=='-')) at++; digits();
        }
        String number=source.substring(start,at);
        if (number.length()>1000) throw new IllegalArgumentException("JSON number too long");
        return new BigDecimal(number);
    }
    private void digits() {
        int start=at; while (at<source.length() && source.charAt(at)>='0' && source.charAt(at)<='9') at++;
        if (at==start) throw new IllegalArgumentException("missing number digits");
    }
    private String string() {
        need('"'); StringBuilder result=new StringBuilder(); boolean closed=false;
        while (at<source.length()) {
            char c=source.charAt(at++);
            if (c=='"') { closed=true; break; }
            if (c<32) throw new IllegalArgumentException("control character in JSON string");
            if (c=='\\') {
                if (at==source.length()) throw new IllegalArgumentException("incomplete JSON escape");
                char escape=source.charAt(at++);
                switch (escape) {
                    case '"','\\','/' -> result.append(escape);
                    case 'b' -> result.append('\b'); case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n'); case 'r' -> result.append('\r'); case 't' -> result.append('\t');
                    case 'u' -> {
                        if (at+4>source.length() || !source.substring(at,at+4).matches("[0-9a-fA-F]{4}")) throw new IllegalArgumentException("bad Unicode escape");
                        result.append((char)Integer.parseInt(source.substring(at,at+4),16)); at+=4;
                    }
                    default -> throw new IllegalArgumentException("unknown JSON escape");
                }
            } else result.append(c);
        }
        if (!closed) throw new IllegalArgumentException("unterminated JSON string");
        String value=result.toString();
        if (value.codePoints().anyMatch(c -> c>=0xd800 && c<=0xdfff)) throw new IllegalArgumentException("unpaired surrogate");
        return value;
    }
    public static Map<String,Object> object(Object... pairs) {
        Map<String,Object> result=new TreeMap<>();
        for(int i=0;i<pairs.length;i+=2) result.put((String)pairs[i],pairs[i+1]);
        return result;
    }
    public static byte[] bytes(Object value) { return (write(value)+"\n").getBytes(StandardCharsets.UTF_8); }
    public static String write(Object value) {
        if (value==null) return "null";
        if (value instanceof Boolean) return value.toString();
        if (value instanceof Number) {
            String text=value.toString(); if (!text.matches("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")) throw new IllegalArgumentException("non-JSON number"); return text;
        }
        if (value instanceof String string) {
            StringBuilder b=new StringBuilder("\""); string.codePoints().forEach(c -> {
                if(c>=0xd800 && c<=0xdfff) throw new IllegalArgumentException("unpaired surrogate");
                if(c=='"'||c=='\\') b.append('\\').appendCodePoint(c);
                else if(c<32) b.append(String.format(java.util.Locale.ROOT,"\\u%04x",c));
                else b.appendCodePoint(c);
            }); return b.append('"').toString();
        }
        if(value instanceof List<?> list) return "["+String.join(",",list.stream().map(Json::write).toList())+"]";
        if(value instanceof Map<?,?> map) {
            Map<String,Object> sorted=new TreeMap<>(); map.forEach((k,v)->sorted.put((String)k,v));
            return "{"+String.join(",",sorted.entrySet().stream().map(e->write(e.getKey())+":"+write(e.getValue())).toList())+"}";
        }
        throw new IllegalArgumentException("unsupported JSON value");
    }
}
