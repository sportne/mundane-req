package engineering.artifacts;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Independent command plumbing shared only by artifact-consuming tools. */
public final class Command {
    private Command() {}
    public record Options(Path root,String plan,String input,String context) {}
    public static Options options(String[] args,boolean needsPlan) {
        Map<String,String> options=new HashMap<>();String input=null;boolean ended=false;
        for(int i=0;i<args.length;i++) {
            String arg=args[i];if(!ended&&arg.equals("--")) { ended=true;continue; }
            if(!ended&&arg.startsWith("--")) {
                if(!List.of("--root","--plan","--context").contains(arg)||!needsPlan&&!arg.equals("--root")||i+1==args.length||options.containsKey(arg)) throw new IllegalArgumentException("unknown, duplicate or incomplete option: "+arg);
                options.put(arg,args[++i]);
            } else {if(input!=null) throw new IllegalArgumentException("expected one input");input=arg;}
        }
        if(!options.containsKey("--root")||input==null||needsPlan&&!options.containsKey("--plan")) throw new IllegalArgumentException("supply --root"+(needsPlan?", --plan":"")+" and one input");
        Path root=Path.of(options.get("--root")).toAbsolutePath().normalize();if(!Files.isDirectory(root)) throw new IllegalArgumentException("root is not a directory");
        Snapshots paths=new Snapshots(root);
        String normalized=needsPlan?paths.argument(Path.of(input)):input;
        return new Options(root,needsPlan?paths.argument(Path.of(options.get("--plan"))):null,normalized,options.get("--context"));
    }
    public static int finish(PrintStream out,PrintStream err,int status) {
        out.flush();err.flush();boolean fail=out.checkError(),errFail=err.checkError();
        if(fail&&!errFail) {err.println("output-failed: unable to deliver command output");err.flush();}
        return fail||errFail?2:status;
    }
}
