package mundanereq.cli;

/** Temporary native-image boundary for the future validator executable. */
public final class ValidateBoundary {
    private ValidateBoundary() {}

    public static void main(String[] arguments) {
        BoundarySmoke.run(arguments, "mundanereq-validate");
    }
}
