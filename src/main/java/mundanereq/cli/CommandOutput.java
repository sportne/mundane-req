package mundanereq.cli;

import java.io.PrintStream;

/** Completion checks also cover early diagnostic-only return paths. */
final class CommandOutput {
    private CommandOutput() {}

    static int finish(PrintStream out, PrintStream err, int status) {
        out.flush();
        err.flush();
        boolean outFailed = out.checkError();
        boolean errFailed = err.checkError();
        if (outFailed && !errFailed) {
            err.println("standard-output:1:1: output-failed: unable to write command output");
            err.flush();
        }
        return outFailed || errFailed ? 2 : status;
    }
}
