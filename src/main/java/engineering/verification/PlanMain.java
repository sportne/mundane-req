package engineering.verification;

import java.io.PrintStream;
import java.nio.file.Path;
import engineering.artifacts.Command;
import engineering.artifacts.Json;
import mundanereq.Versions;

public final class PlanMain {
    private PlanMain() {}
    public static void main(String[] args){System.exit(run(args,System.out,System.err));}
    public static int run(String[] args,PrintStream out,PrintStream err) {
        int status=0;
        try {
            if(args.length==1&&args[0].equals("--help"))out.println("Usage: mundane-plan --root DIRECTORY PLAN_DIRECTORY");
            else if(args.length==1&&args[0].equals("--version"))out.println("mundane-plan "+Versions.PLAN_VERSION+"; "+Versions.PLAN_SOURCE+"; "+Versions.PLAN_ARTIFACT+"; "+Versions.PLAN_CONTRACT);
            else {var options=Command.options(args,false);var result=PlanCompiler.compile(options.root(),Path.of(options.input()).toAbsolutePath().normalize());out.writeBytes(Json.bytes(result.output()));status=result.status();}
        } catch(IllegalArgumentException error) {err.println("invocation-failed: "+error.getMessage());status=2;}
        return Command.finish(out,err,status);
    }
}
