package engineering.artifacts;

import java.io.PrintStream;
import mundanereq.Versions;

public final class LinkMain {
    private LinkMain() {}
    public static void main(String[] args) {System.exit(run(args,System.out,System.err));}
    public static int run(String[] args,PrintStream out,PrintStream err) {
        int status=0;
        try {
            if(args.length==1&&args[0].equals("--help")) out.println("Usage: mundane-link --root DIRECTORY --plan COMPILED_PLAN [--context CONTEXT] IMPORTS");
            else if(args.length==1&&args[0].equals("--version")) out.println("mundane-link "+Versions.LINK_VERSION+"; "+Versions.IMPORT_FORMAT+"; "+Versions.LINK_ARTIFACT);
            else {
                var options=Command.options(args,true);
                var result=Linker.link(options.root(),options.input(),options.plan(),options.context());out.writeBytes(Json.bytes(result.output()));status=result.status();
            }
        } catch(IllegalArgumentException error) {err.println("invocation-failed: "+error.getMessage());status=2;}
        return Command.finish(out,err,status);
    }
}
