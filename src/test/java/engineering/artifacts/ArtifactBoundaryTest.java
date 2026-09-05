package engineering.artifacts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ArtifactBoundaryTest {
    private ArtifactBoundaryTest() {}
    public static void run() throws Exception {
        for(String invalid:List.of("{\"a\":1,\"a\":2}","[1,]","{\"a\":1,}","01","+1","1.","1e","true false","NaN","\"\\ud800\"","\"\\q\"","\"raw\nnewline\"","[".repeat(66)+"0"+"]".repeat(66))) {
            try {Json.read(invalid.getBytes(StandardCharsets.UTF_8));throw new AssertionError("accepted malformed JSON: "+invalid);}
            catch(IllegalArgumentException expected) { /* boundary rejected */ }
        }
        Object valid=Json.read("{\"escaped\":\"\\ud83d\\ude00\\n\",\"v\":[true,false,null,-1.25e+2]}".getBytes(StandardCharsets.UTF_8));
        if(!Json.write(Json.read(Json.bytes(valid))).equals(Json.write(valid))) throw new AssertionError("JSON roundtrip");
        Path fixture=Path.of("experiments/0028-verification-contract").toAbsolutePath();
        String[] args={"--root",fixture.toString(),"--plan",fixture.resolve("fixtures/plan.json").toString(),fixture.resolve("fixtures/imports.json").toString()};
        for(int limit:List.of(0,23,Integer.MAX_VALUE)) {
            PrintStream failure=new PrintStream(new OutputStream(){private int count;
                @Override public void write(int b)throws IOException {if(count++>=limit)throw new IOException("prefix failure");}
                @Override public void flush()throws IOException {throw new IOException("flush failure");}});
            if(LinkMain.run(args,failure,sink())!=2)throw new AssertionError("failed output succeeded");
        }
        PrintStream closed=sink();closed.close();if(LinkMain.run(args,sink(),closed)!=2)throw new AssertionError("closed stderr succeeded");
        Path temporary=Files.createTempDirectory("artifact-snapshot-");
        try {
            Path file=temporary.resolve("a.json");Files.writeString(file,"{}\n");
            Snapshots snapshots=new Snapshots(temporary);snapshots.read("a.json");Files.writeString(file,"[]\n");
            try {snapshots.recheck();throw new AssertionError("changed snapshot accepted");}
            catch(Problem problem) {if(!problem.code.equals("input-changed"))throw problem;}
            Files.delete(file);
        } finally {Files.delete(temporary);}
        Path linkedRoot=Files.createTempDirectory("link-change-");
        try {
            Files.createDirectory(linkedRoot.resolve("fixtures"));
            try(var paths=Files.list(fixture.resolve("fixtures"))) {
                for(Path path:paths.toList()) Files.copy(path,linkedRoot.resolve("fixtures").resolve(path.getFileName()));
            }
            var changed=Linker.link(linkedRoot,"fixtures/imports.json","fixtures/plan.json",null,()->{
                try {Files.writeString(linkedRoot.resolve("fixtures/current.json"),"{}\n");}
                catch(IOException error) {throw new IllegalStateException(error);}
            });
            if(changed.status()!=2 || Boolean.TRUE.equals(changed.output().get("complete"))
                    || !Checks.list(changed.output().get("edges")).isEmpty()) throw new AssertionError("changed import marked linked");
        } finally {
            try(var paths=Files.walk(linkedRoot)) {for(Path path:paths.sorted(java.util.Comparator.reverseOrder()).toList())Files.delete(path);}
        }
        System.out.println("PASS strict JSON boundary, Unicode/number roundtrip, linker output faults and detected snapshot changes");
    }
    private static PrintStream sink(){return new PrintStream(new ByteArrayOutputStream());}
}
