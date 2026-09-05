package mundanereq.output;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Deterministic requirement-tool JSON output, shared without another parser or runtime library. */
public final class JsonOutput {
    private JsonOutput() {}
    public static byte[] encode(Object value) {
        StringBuilder output = new StringBuilder();
        json(output, value);
        return output.append('\n').toString().getBytes(StandardCharsets.UTF_8);
    }

    public static Map<String, Object> object(Object... pairs) {
        Map<String, Object> result = new TreeMap<>();
        for (int i = 0; i < pairs.length; i += 2) result.put((String) pairs[i], pairs[i + 1]);
        return result;
    }

    private static void json(StringBuilder output, Object value) {
        if (value == null) output.append("null");
        else if (value instanceof String string) quote(output, string);
        else if (value instanceof Boolean || value instanceof Integer) output.append(value);
        else if (value instanceof Map<?, ?> map) {
            output.append('{');
            boolean first = true;
            for (var entry : map.entrySet()) {
                if (!first) output.append(',');
                first = false;
                quote(output, (String) entry.getKey()); output.append(':'); json(output, entry.getValue());
            }
            output.append('}');
        } else if (value instanceof List<?> list) {
            output.append('[');
            for (int i = 0; i < list.size(); i++) {
                if (i != 0) output.append(',');
                json(output, list.get(i));
            }
            output.append(']');
        } else throw new IllegalArgumentException("unsupported serialization value");
    }

    private static void quote(StringBuilder output, String value) {
        output.append('"');
        value.codePoints().forEach(c -> {
            if (c >= 0xd800 && c <= 0xdfff) throw new IllegalArgumentException("unpaired surrogate in output");
            if (c == '"' || c == '\\') output.append('\\').appendCodePoint(c);
            else if (c < 32) output.append(String.format(java.util.Locale.ROOT, "\\u%04x", c));
            else output.appendCodePoint(c);
        });
        output.append('"');
    }
}
