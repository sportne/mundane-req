import java.util.Map;
import java.util.stream.Collectors;

/** Experiment-only interchange between the maintained oracle and test harness. */
final class Json {
    private Json() {}
    static String write(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) return quote(s);
        if (value instanceof Boolean || value instanceof Number) return value.toString();
        if (value instanceof Map<?, ?> map) return map.entrySet().stream()
                .sorted(java.util.Comparator.comparing(e -> e.getKey().toString()))
                .map(e -> quote(e.getKey().toString()) + ":" + write(e.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
        if (value instanceof Iterable<?> items) {
            var output = new java.util.ArrayList<String>();
            items.forEach(item -> output.add(write(item)));
            return String.join(",", output.stream().toList()).transform(s -> "[" + s + "]");
        }
        throw new IllegalArgumentException("unsupported JSON value " + value.getClass());
    }
    private static String quote(String text) {
        var out = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> { if (c < 32) out.append("\\u%04x".formatted((int)c)); else out.append(c); }
            }
        }
        return out.append('"').toString();
    }
}
