package mundanereq.source;

import java.util.Objects;

/** Minimal concrete classification; comments remain source text without attachment. */
public record ConcreteLine(Kind kind, PhysicalLine physicalLine) {
    public enum Kind {
        BLANK,
        COMMENT,
        CONTENT
    }

    public ConcreteLine {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(physicalLine, "physicalLine");
        Kind expected = physicalLine.text().isEmpty()
                ? Kind.BLANK
                : physicalLine.text().startsWith("#") ? Kind.COMMENT : Kind.CONTENT;
        if (kind != expected) throw new IllegalArgumentException("line kind does not match its text");
    }

    static ConcreteLine classify(PhysicalLine line) {
        Kind kind = line.text().isEmpty()
                ? Kind.BLANK
                : line.text().startsWith("#") ? Kind.COMMENT : Kind.CONTENT;
        return new ConcreteLine(kind, line);
    }
}
