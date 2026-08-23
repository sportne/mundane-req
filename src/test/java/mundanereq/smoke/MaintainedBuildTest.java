package mundanereq.smoke;

/** JVM and Native Image smoke test for the maintained project boundary. */
public final class MaintainedBuildTest {
    private MaintainedBuildTest() {}

    public static void main(String[] arguments) {
        if (arguments.length != 0) {
            throw new AssertionError("the maintained build smoke test accepts no arguments");
        }
        if (!System.getProperty("java.specification.version").equals("21")) {
            throw new AssertionError("the maintained build requires Java 21");
        }
        System.out.println("PASS maintained Java 21 project skeleton");
    }
}
