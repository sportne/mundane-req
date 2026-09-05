package mundanereq.cli;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import mundanereq.Interpreter;
public class SafetyRegressionProof {
    public static void main(String[] args) throws Exception {
        var fail = new PrintStream(new OutputStream() { public void write(int b) throws IOException { throw new IOException("injected"); } });
        int status = ValidatorMain.run(new String[]{"--version"}, fail, new PrintStream(OutputStream.nullOutputStream()));
        Path file = Files.createTempFile("formatter-snapshot-proof-", ".mreq");
        try {
            String source = "requirement A\r\ntitle: Title\r\nstatement:\r\n  Text.\r\nend requirement\r\n";
            Files.writeString(file, source);
            var selected = Interpreter.selectInputs(List.of(file));
            Files.writeString(file, "external edit\n");
            var method = FormatterMain.class.getDeclaredMethod("writeFiles", List.class, Map.class, PrintStream.class, PrintStream.class);
            method.setAccessible(true);
            int write = (Integer) method.invoke(null, selected.sources(), Map.of(file, source.replace("\r\n","\n").getBytes(StandardCharsets.UTF_8)), new PrintStream(OutputStream.nullOutputStream()), new PrintStream(OutputStream.nullOutputStream()));
            System.out.printf("validator failed-output status=%d; formatter changed-source status=%d; external edit preserved=%s%n", status, write, Files.readString(file).equals("external edit\n"));
        } finally { Files.deleteIfExists(file); }
    }
}
