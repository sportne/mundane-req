package mundanereq.source;

import java.util.Objects;

/** Half-open source span whose end may be immediately after the final character. */
public record SourceSpan(SourcePosition start, SourcePosition end) {
    public SourceSpan {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (!start.source().equals(end.source())) {
            throw new IllegalArgumentException("span endpoints must name the same source");
        }
        if (end.line() < start.line()
                || (end.line() == start.line() && end.column() < start.column())) {
            throw new IllegalArgumentException("span end must not precede its start");
        }
    }
}
