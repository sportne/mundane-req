package mundanereq.source;

import java.util.Objects;

/** One terminated physical line without its line-ending characters in {@code text}. */
public record PhysicalLine(SourceSpan span, String text, LineEnding lineEnding) {
    public PhysicalLine {
        Objects.requireNonNull(span, "span");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(lineEnding, "lineEnding");
        if (text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("physical line text must not contain line endings");
        }
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (Character.isHighSurrogate(character)) {
                if (index + 1 >= text.length() || !Character.isLowSurrogate(text.charAt(index + 1))) {
                    throw new IllegalArgumentException("physical line text must contain only Unicode scalar values");
                }
                index++;
            } else if (Character.isLowSurrogate(character)) {
                throw new IllegalArgumentException("physical line text must contain only Unicode scalar values");
            }
        }
        if (span.start().line() != span.end().line() || span.start().column() != 1) {
            throw new IllegalArgumentException("a physical-line span must cover one complete line");
        }
        int expectedEnd = text.codePointCount(0, text.length()) + 1;
        if (span.end().column() != expectedEnd) {
            throw new IllegalArgumentException("physical-line span must count Unicode scalar values");
        }
    }

    public String rendered() {
        return text + lineEnding.text();
    }
}
