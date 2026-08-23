package mundanereq.cli;

import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import mundanereq.Interpreter;

/** Focused command-line validator for the provisional 0.2 source contract. */
public final class ValidatorMain {
    static final String TOOL_VERSION = "trial-0.1";
    static final String SOURCE_CONTRACT = "mundanereq-source-0.2";

    private ValidatorMain() {}

    public static void main(String[] arguments) {
        System.exit(run(arguments, System.out, System.err));
    }

    static int run(String[] arguments, PrintStream out, PrintStream err) {
        if (arguments.length == 1 && arguments[0].equals("--help")) {
            out.print(usage());
            return 0;
        }
        if (arguments.length == 1 && arguments[0].equals("--version")) {
            out.printf("mundanereq-validate %s; source contract %s%n", TOOL_VERSION, SOURCE_CONTRACT);
            return 0;
        }

        List<Path> inputs = new ArrayList<>();
        boolean optionsEnded = false;
        try {
            for (String argument : arguments) {
                if (!optionsEnded && argument.equals("--")) {
                    optionsEnded = true;
                } else if (!optionsEnded && argument.startsWith("-")) {
                    err.println("unknown option: " + argument);
                    err.print(usage());
                    return 2;
                } else {
                    inputs.add(Path.of(argument));
                }
            }
        } catch (InvalidPathException exception) {
            err.println("invalid input path: " + exception.getMessage());
            return 2;
        }

        if (inputs.isEmpty()) {
            err.print(usage());
            return 2;
        }

        Interpreter.Result result = Interpreter.interpretInputs(inputs);
        if (!result.diagnostics().isEmpty()) {
            result.diagnostics().forEach(diagnostic -> err.println(render(diagnostic)));
            return result.diagnostics().stream().anyMatch(ValidatorMain::isOperational) ? 2 : 1;
        }

        int relationships = result.outgoing().values().stream().mapToInt(java.util.Set::size).sum();
        out.printf(
                "Validated %d requirements and %d decomposition relationships from %d %s as %s.%n",
                result.requirements().size(),
                relationships,
                result.fileCount(),
                result.fileCount() == 1 ? "file" : "files",
                SOURCE_CONTRACT);
        return 0;
    }

    private static boolean isOperational(Interpreter.Diagnostic diagnostic) {
        return diagnostic.code().equals("input-unavailable") || diagnostic.code().equals("no-source-files");
    }

    private static String render(Interpreter.Diagnostic diagnostic) {
        return "%s:%d:%d: %s: %s".formatted(
                diagnostic.file(),
                diagnostic.line(),
                diagnostic.column(),
                diagnostic.code(),
                diagnostic.message());
    }

    private static String usage() {
        return "Usage: mundanereq-validate [--] FILE_OR_DIRECTORY...\n"
                + "       mundanereq-validate --help\n"
                + "       mundanereq-validate --version\n";
    }
}
