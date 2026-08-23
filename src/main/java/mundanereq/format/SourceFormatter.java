package mundanereq.format;

import java.nio.charset.StandardCharsets;
import java.util.List;
import mundanereq.source.ConcreteLine;
import mundanereq.source.SourceDocument;

/** Conservative physical-source formatter selected by Experiment 0008. */
public final class SourceFormatter {
    private SourceFormatter() {}

    public static byte[] format(SourceDocument document) {
        List<ConcreteLine> lines = document.lines();
        StringBuilder output = new StringBuilder();
        for (int index = 0; index < lines.size(); index++) {
            String text = lines.get(index).physicalLine().text();
            output.append(text).append('\n');
            if (!text.equals("end requirement")) continue;

            int next = index + 1;
            while (next < lines.size() && lines.get(next).kind() == ConcreteLine.Kind.BLANK) next++;
            if (next > index + 1
                    && next < lines.size()
                    && lines.get(next).physicalLine().text().startsWith("requirement ")) {
                output.append('\n');
                index = next - 1;
            }
        }
        return output.toString().getBytes(StandardCharsets.UTF_8);
    }
}
