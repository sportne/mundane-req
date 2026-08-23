package mundanereq.cli;

import java.util.List;
import mundanereq.Interpreter;

/** Temporary proof that each executable can link and invoke the shared interpreter. */
final class BoundarySmoke {
    private BoundarySmoke() {}

    static void run(String[] arguments, String executableName) {
        if (arguments.length != 1 || !arguments[0].equals("--boundary-smoke")) {
            throw new IllegalArgumentException("temporary boundary accepts only --boundary-smoke");
        }
        Interpreter.Result result = Interpreter.interpretSources(List.of());
        if (result.diagnostics().size() != 1
                || !result.diagnostics().getFirst().code().equals("no-source-files")) {
            throw new IllegalStateException("shared interpreter boundary failed");
        }
        System.out.println(executableName + " boundary");
    }
}
