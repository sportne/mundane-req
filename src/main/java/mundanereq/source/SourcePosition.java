package mundanereq.source;

import java.util.Objects;

/** One-based position in a named source file. */
public record SourcePosition(String source, int line, int column) {
    public SourcePosition {
        Objects.requireNonNull(source, "source");
        if (source.isEmpty()) throw new IllegalArgumentException("source must not be empty");
        if (line < 1) throw new IllegalArgumentException("line must be positive");
        if (column < 1) throw new IllegalArgumentException("column must be positive");
    }
}
