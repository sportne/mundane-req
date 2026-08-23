package mundanereq.cli;

/** Temporary native-image boundary for the future trace executable. */
public final class TraceBoundary {
    private TraceBoundary() {}

    public static void main(String[] arguments) {
        BoundarySmoke.run(arguments, "mundanereq-trace");
    }
}
