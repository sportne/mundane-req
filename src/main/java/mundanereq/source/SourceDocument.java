package mundanereq.source;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Lossless physical-line representation for a decodable, terminated source file. */
public final class SourceDocument {
    private final String name;
    private final byte[] originalBytes;
    private final List<ConcreteLine> lines;

    private SourceDocument(String name, byte[] originalBytes, List<ConcreteLine> lines) {
        this.name = name;
        this.originalBytes = originalBytes.clone();
        this.lines = List.copyOf(lines);
    }

    public static SourceDocument read(String name, byte[] bytes) throws CharacterCodingException {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(bytes, "bytes");
        if (name.isEmpty()) throw new IllegalArgumentException("source name must not be empty");

        CharBuffer decoded = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes));
        String text = decoded.toString();
        List<ConcreteLine> lines = splitLines(name, text);
        return new SourceDocument(name, bytes, lines);
    }

    private static List<ConcreteLine> splitLines(String name, String text) {
        List<ConcreteLine> result = new ArrayList<>();
        int offset = 0;
        int lineNumber = 1;
        while (offset < text.length()) {
            int lf = text.indexOf('\n', offset);
            if (lf < 0) throw new IllegalArgumentException("source must end with LF or CRLF");
            boolean crlf = lf > offset && text.charAt(lf - 1) == '\r';
            int contentEnd = crlf ? lf - 1 : lf;
            String content = text.substring(offset, contentEnd);
            if (content.indexOf('\r') >= 0) {
                throw new IllegalArgumentException("a carriage return must be followed by a line feed");
            }
            int endColumn = content.codePointCount(0, content.length()) + 1;
            SourceSpan span = new SourceSpan(
                    new SourcePosition(name, lineNumber, 1),
                    new SourcePosition(name, lineNumber, endColumn));
            PhysicalLine line = new PhysicalLine(span, content, crlf ? LineEnding.CRLF : LineEnding.LF);
            result.add(ConcreteLine.classify(line));
            offset = lf + 1;
            lineNumber++;
        }
        if (result.isEmpty()) throw new IllegalArgumentException("source must contain a terminated physical line");
        return result;
    }

    public String name() {
        return name;
    }

    public byte[] originalBytes() {
        return originalBytes.clone();
    }

    public List<ConcreteLine> lines() {
        return lines;
    }

    public List<ConcreteLine> comments() {
        return lines.stream().filter(line -> line.kind() == ConcreteLine.Kind.COMMENT).toList();
    }

    public byte[] renderedBytes() {
        StringBuilder rendered = new StringBuilder();
        lines.forEach(line -> rendered.append(line.physicalLine().rendered()));
        return rendered.toString().getBytes(StandardCharsets.UTF_8);
    }
}
