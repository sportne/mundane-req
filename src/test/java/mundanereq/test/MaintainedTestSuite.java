package mundanereq.test;

import mundanereq.InterpreterTest;
import mundanereq.InterpreterRegressionTest;
import mundanereq.SemanticConversionTest;
import mundanereq.smoke.MaintainedBuildTest;
import mundanereq.source.SourceDocumentTest;
import mundanereq.cli.ValidatorMainTest;

/** Dependency-free maintained JVM test suite. */
public final class MaintainedTestSuite {
    private MaintainedTestSuite() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 0) throw new AssertionError("the test suite accepts no arguments");
        MaintainedBuildTest.main(arguments);
        SourceDocumentTest.run();
        System.out.println("PASS physical and concrete source representation");
        SemanticConversionTest.run();
        System.out.println("PASS concrete-to-semantic conversion");
        InterpreterTest.run();
        System.out.println("PASS maintained semantic interpretation");
        InterpreterRegressionTest.main(arguments);
        ValidatorMainTest.run();
        System.out.println("PASS validator command interface");
        System.out.println("Passed 6 maintained test groups.");
    }
}
