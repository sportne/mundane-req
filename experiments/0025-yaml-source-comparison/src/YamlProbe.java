import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.snakeyaml.engine.v2.api.*;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.snakeyaml.engine.v2.schema.CoreSchema;

/** Standard SnakeYAML loading and two emission paths; profile validation is separate. */
public final class YamlProbe {
    private YamlProbe() {}
    public static void main(String[] args) throws Exception {
        if(args.length!=2) throw new IllegalArgumentException("load|dump|roundtrip|conservative FILE");
        var input=Files.readString(Path.of(args[1]));
        var settings=LoadSettings.builder().setSchema(new CoreSchema()).setAllowDuplicateKeys(false)
                .setParseComments(true).setLabel(Path.of(args[1]).getFileName().toString()).build();
        var dump=new Dump(DumpSettings.builder().setSchema(new CoreSchema()).setDumpComments(true)
                .setDefaultFlowStyle(FlowStyle.BLOCK).setIndent(2).setWidth(88).build());
        try {
            if(args[0].equals("roundtrip")) {
                var node=new Compose(settings).composeString(input).orElseThrow();
                var out=new StringBuilder();
                dump.dumpNode(node,new StreamDataWriter(){
                    public void write(String s){out.append(s);}
                    public void write(String s,int off,int len){out.append(s,off,off+len);}
                });
                System.out.print(out);
            } else {
                Object data=new Load(settings).loadFromString(input);
                if(args[0].equals("conservative")) System.out.print(input.replace("\r\n", "\n"));
                else if(args[0].equals("dump")) System.out.print(dump.dumpToString(data));
                else System.out.println(Json.write(Map.of("valid",true,"data",data)));
            }
        } catch(MarkedYamlEngineException e) {
            var mark=e.getProblemMark();
            System.out.println(Json.write(Map.of("valid",false,"code",e.getClass().getSimpleName(),
                    "line",mark.map(m->m.getLine()+1).orElse(1),"column",mark.map(m->m.getColumn()+1).orElse(1),
                    "message",e.getProblem())));
        }
        System.out.flush();
        if(System.out.checkError()) System.exit(2);
    }
}
