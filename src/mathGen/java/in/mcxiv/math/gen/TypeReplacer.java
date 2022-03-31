package in.mcxiv.math.gen;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeReplacer {

    interface Cache {
        void evaluateLine(String line);
    }

    String buffer = null;
    Cache cache = null;

    public void evaluateLine(String line) {
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

    public String getCode() {
        String buffer = this.buffer;
        this.buffer = null;
        return buffer;
    }
}
