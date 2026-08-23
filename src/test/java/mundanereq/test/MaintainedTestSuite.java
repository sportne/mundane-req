package mundanereq.test;

import mundanereq.OracleSemanticConversionTest;
import mundanereq.smoke.MaintainedBuildTest;
import mundanereq.source.SourceDocumentTest;

/** Dependency-free maintained JVM test suite. */
public final class MaintainedTestSuite {
    private MaintainedTestSuite() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 0) throw new AssertionError("the test suite accepts no arguments");
        MaintainedBuildTest.main(arguments);
        SourceDocumentTest.run();
        System.out.println("PASS physical and concrete source representation");
        OracleSemanticConversionTest.run();
        System.out.println("PASS concrete-to-semantic migration oracle");
        System.out.println("Passed 3 maintained test groups.");
    }
}
