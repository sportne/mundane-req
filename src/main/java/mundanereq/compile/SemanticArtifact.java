package mundanereq.compile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import mundanereq.Interpreter;
import mundanereq.SourceFormat;
import mundanereq.Versions;
import mundanereq.source.SourcePosition;
import mundanereq.source.SourceSpan;

/** Requirement artifact 0.1; serializes retained interpretation, never reparses source. */
public final class SemanticArtifact {
    private SemanticArtifact() {}

    public static byte[] emit(List<Interpreter.Source> sources, Interpreter.Result result, SourceFormat format) {
        Map<String, Object> envelope = object(
                "artifactKind", "requirements", "format", Versions.REQUIREMENT_ARTIFACT,
                "sourceContract", format.contract,
                "compiler", object("name", "mundanereq-compile", "version", Versions.COMPILE_VERSION,
                        "contract", Versions.COMPILE_CONTRACT),
                "complete", result.valid());
        envelope.put("sources", sources.stream().sorted(Comparator.comparing(Interpreter.Source::file))
                .map(s -> object("path", s.file(), "sha256",
                        format == SourceFormat.YAML_03 && s.bytes().length > 8 * 1024 * 1024 ? null : sha256(s.bytes())))
                .toList());
        List<Object> records = new ArrayList<>();
        if (result.valid()) {
            Map<String, Interpreter.RequirementOrigin> origins = new TreeMap<>();
            result.origins().forEach(o -> origins.put(o.id(), o));
            for (var requirement : result.requirements().stream().sorted(Comparator.comparing(Interpreter.Requirement::id)).toList()) {
                var origin = origins.get(requirement.id());
                if (origin == null) throw new IllegalArgumentException("missing retained requirement origin: " + requirement.id());
                Map<String, Object> fields = new TreeMap<>();
                origin.fields().forEach((name, spans) -> fields.put(name, spans.stream().map(SemanticArtifact::span).toList()));
                Map<String, Object> references = new TreeMap<>();
                origin.references().forEach((target, value) -> references.put(target, span(value)));
                records.add(object("values", values(requirement), "locations",
                        object("record", span(origin.record()), "fields", fields, "references", references)));
            }
        }
        envelope.put("requirements", records);
        envelope.put("diagnostics", result.diagnostics().stream()
                .sorted(Comparator.comparing(Interpreter.Diagnostic::file).thenComparingInt(Interpreter.Diagnostic::line)
                        .thenComparingInt(Interpreter.Diagnostic::column).thenComparing(Interpreter.Diagnostic::code)
                        .thenComparing(Interpreter.Diagnostic::message))
                .map(d -> object("ruleId", d.code(), "severity", "error", "phase", operational(d) ? "input" : "source",
                        "message", d.message(), "location", object("path", d.file(),
                                "start", object("line", d.line(), "column", diagnosticColumn(d, sources)), "end", null))).toList());
        StringBuilder output = new StringBuilder();
        json(output, envelope);
        return output.append('\n').toString().getBytes(StandardCharsets.UTF_8);
    }

    // Legacy invalid-UTF-8 diagnostics use raw-byte columns. Convert the known-valid
    // prefix only, keeping existing CLI coordinates and avoiding a second parse.
    private static int diagnosticColumn(Interpreter.Diagnostic diagnostic, List<Interpreter.Source> sources) {
        if (!diagnostic.code().equals("invalid-utf8")) return diagnostic.column();
        var source = sources.stream().filter(s -> s.file().equals(diagnostic.file())).findFirst();
        if (source.isEmpty()) throw new IllegalArgumentException("missing bytes for UTF-8 diagnostic");
        byte[] bytes = source.get().bytes();
        int start = 0;
        for (int line = 1; line < diagnostic.line(); line++) {
            while (start < bytes.length && bytes[start++] != '\n') { /* advance physical line */ }
        }
        String prefix = new String(bytes, start, diagnostic.column() - 1, StandardCharsets.UTF_8);
        return prefix.codePointCount(0, prefix.length()) + 1;
    }

    public static boolean operational(Interpreter.Diagnostic diagnostic) {
        return diagnostic.code().equals("input-unavailable") || diagnostic.code().equals("no-source-files");
    }

    private static Map<String, Object> values(Interpreter.Requirement r) {
        return object("id", r.id(), "title", r.title(), "allocation", r.allocation(),
                "statement", blocks(r.statement()), "rationale", blocks(r.rationale()),
                "source", r.source(), "decomposes", r.decomposes().stream().sorted().toList());
    }

    private static Object blocks(List<Interpreter.ContentBlock> blocks) {
        if (blocks == null) return null;
        return blocks.stream().map(b -> switch (b) {
            case Interpreter.ProseBlock p -> object("kind", "prose", "text", p.text());
            case Interpreter.MathBlock m -> object("kind", "math", "language", m.language(), "payload", m.payload());
        }).toList();
    }

    private static Object span(SourceSpan span) {
        return object("path", span.start().source(), "start", position(span.start()), "end", position(span.end()));
    }

    private static Object position(SourcePosition position) {
        return object("line", position.line(), "column", position.column());
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("required SHA-256 unavailable", exception);
        }
    }

    private static Map<String, Object> object(Object... pairs) {
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
