import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.jlt.util.Language;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class myMethod {

    private static Map<String, Map<String, Integer>> output = new HashMap<>();
    private static BabelNet bn = BabelNet.getInstance();

    public static void main(String[] args) {
        try {
            int steps = Integer.parseInt(args[0]);
            Files.lines(Paths.get("src/main/resources/input.txt")).forEach(line -> populateMap(line, 0, steps));
            List<String> json = createJson();
            Path file = Paths.get("src/main/resources/output.json");
            Files.write(file, json, Charset.forName("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void populateMap(String word, int i, int max) {
        String pos = null;
        if (i == 0 && word.contains(",")) {
            String[] lineParts = word.split(",");
            word = lineParts[0];
            pos = lineParts[1];
        }
        Map<String, Integer> concepts = new HashMap<>();
        if (!output.containsKey(word)) {
            List<BabelSynset> synsets = bn.getSynsets(word, Language.EN);
            for (BabelSynset synset : synsets) {
                if (synset.getMainSense(Language.EN).isPresent()) {
                    if (pos != null && !pos.equalsIgnoreCase(Character.toString(synset.getPOS().getTag()))) {
                        continue;
                    }
                    concepts.put(synset.getMainSense(Language.EN).get().getFullLemma(), i);
                    if (i < max) {
                        i++;
                        populateMap(synset.getMainSense(Language.EN).get().getFullLemma(), i, max);
                    }
                }
            }
            if (i == 0) {
                output.put(word, concepts);
            }
        }
    }

    private static List<String> createJson() {
        List<String> lines = new ArrayList<>();
        lines.add("{");
        List<Map.Entry<String, Map<String, Integer>>> outputEntries = output.entrySet().stream().collect(Collectors.toList());
        for (int i = 0; i < outputEntries.size(); i++) {
            lines.add("\"" + outputEntries.get(i).getKey() + "\": {");
            List<Map.Entry<String, Integer>> entries = outputEntries.get(i).getValue().entrySet().stream().collect(Collectors.toList());
            for (int j = 0; j < entries.size(); j++) {
                if (j == (entries.size() - 1)) {
                    lines.add("\"" + entries.get(j).getKey() + "\":" + entries.get(j).getValue());
                } else {
                    lines.add("\"" + entries.get(j).getKey() + "\":" + entries.get(j).getValue() + ",");
                }
            }
            if (i == (outputEntries.size() - 1)) {
                lines.add("}");
            } else {
                lines.add("},");
            }
        }
        lines.add("}");
        return lines;
    }
}
