package mundanereq;

import java.nio.file.Path;
import java.util.Arrays;

/** Test-only inventory emitter used to close the historical migration comparison. */
public final class InventoryMain {
    private InventoryMain() {}

    public static void main(String[] arguments) {
        if (arguments.length == 0) throw new IllegalArgumentException("at least one source input is required");
        Interpreter.Result result = Interpreter.interpretInputs(
                Arrays.stream(arguments).map(Path::of).toList());
        if (!result.valid()) throw new IllegalArgumentException(result.diagnostics().toString());
        System.out.print(Interpreter.normalizedInventory(result.requirements()));
    }
}
