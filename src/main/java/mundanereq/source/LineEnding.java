package mundanereq.source;

/** Physical line-ending spelling retained for conservative rewriting. */
public enum LineEnding {
    LF("\n"),
    CRLF("\r\n");

    private final String text;

    LineEnding(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}
