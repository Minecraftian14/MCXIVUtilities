package in.mcxiv.math.gen;

import java.io.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeReplacer {

    private static final File input = new File("src\\mathGen\\java\\in\\mcxiv\\math\\gen\\MathBox.java").getAbsoluteFile();
    private static final File ouput = new File("src\\math\\java\\in\\mcxiv\\math\\MathBox.java").getAbsoluteFile();

    public static void main(String[] args) throws IOException {
        TypeReplacer replacer = new TypeReplacer();
        try (
                FileReader __reader__ = new FileReader(input);
                BufferedReader reader = new BufferedReader(__reader__);
                FileWriter __writer__ = new FileWriter(ouput);
                BufferedWriter writer = new BufferedWriter(__writer__);
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                replacer.evaluateLine(line);
                String newLine = replacer.getCode();
                if (newLine != null)
                    writer.write(newLine + System.lineSeparator());
            }
        }
    }

    interface Cache {
        void evaluateLine(String line);
    }

    String buffer = null;
    Cache cache = null;

    private void evaluateLine(String line) {
        if (cache != null) {
            cache.evaluateLine(line);
            return;
        }

        String deindented = line.stripLeading();
        if (deindented.startsWith("package"))
            evaluatePackage(line);
        else if (deindented.startsWith("@SupportedTypes"))
            evaluateAPossibleFunction(line);
        else buffer = line;
    }

    private void evaluatePackage(String line) {
        buffer = line.replace(".gen", "");
    }

    class APossibleFunctionCache implements Cache {

        Stream<String> stream;

        StringBuilder builder = new StringBuilder();

        public APossibleFunctionCache(Stream<String> stream) {
            this.stream = stream;
        }

        @Override
        public void evaluateLine(String line) {
            builder.append(line).append(System.lineSeparator());
            if (line.strip().equals("}")) {
                final String finalCode = builder
                        .append(System.lineSeparator())
                        .toString();
                buffer = stream
                        .map(s -> finalCode
                                .replace("Float", s)
                                .replace("float", s.toLowerCase())
                        ).collect(Collectors.joining());
                cache = null;
            }
        }
    }

    public static final Pattern rgx_supportedTypes
            = Pattern.compile("^[ ]*@SupportedTypes\\(\\{?([\\w ,.]+)\\}?\\)$");

    private void evaluateAPossibleFunction(String line) {
        Matcher matcher = rgx_supportedTypes.matcher(line);
        if (!matcher.matches())
            throw new IllegalStateException("No Match? What? Lol.");
        assert matcher.groupCount() > 1;
        cache = new APossibleFunctionCache(
                Arrays.stream(matcher.group(1).split(", *"))
                        .map(String::strip)
                        .map(s -> s.substring(0, s.indexOf('.')))
                        .map(s -> s.equals("Integer") ? "Int" : s)
        );
    }

    private String getCode() {
        String buffer = this.buffer;
        this.buffer = null;
        return buffer;
    }
}
