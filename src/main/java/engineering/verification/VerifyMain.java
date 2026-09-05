package engineering.verification;

import java.io.PrintStream;
import engineering.artifacts.Command;
import engineering.artifacts.Json;
import engineering.artifacts.Linker;
import mundanereq.Versions;

public final class VerifyMain {
    private VerifyMain() {}
    public static void main(String[] args){System.exit(run(args,System.out,System.err));}
    public static int run(String[] args,PrintStream out,PrintStream err) {
        int status=0;
        try {
            if(args.length==1&&args[0].equals("--help"))out.println("Usage: mundane-verify --root DIRECTORY --plan COMPILED_PLAN [--context CONTEXT] IMPORTS");
            else if(args.length==1&&args[0].equals("--version"))out.println("mundane-verify "+Versions.VERIFY_VERSION+"; "+Versions.VERIFICATION_ARTIFACT+"; "+Versions.VERIFY_CONTRACT);
            else {
                var options=Command.options(args,true);var linked=Linker.link(options.root(),options.input(),options.plan(),options.context());
                var result=Verifier.analyze(linked);out.writeBytes(Json.bytes(result.output()));status=result.status();
            }
        } catch(IllegalArgumentException error) {err.println("invocation-failed: "+error.getMessage());status=2;}
        return Command.finish(out,err,status);
    }
}
