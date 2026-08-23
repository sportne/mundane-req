package mundanereq.cli;

/** Temporary native-image boundary for the future formatter executable. */
public final class FormatBoundary {
    private FormatBoundary() {}

    public static void main(String[] arguments) {
        BoundarySmoke.run(arguments, "mundanereq-format");
    }
}
