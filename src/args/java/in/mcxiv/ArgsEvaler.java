package in.mcxiv;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgsEvaler {

    public static final String SINGLE_DASH_IN_NOTHING = "";
    public static final String SINGLE_DASH_IN_SHORT_NAMES = "";
    public static final String SINGLE_DASH_IN_FULL_NAMES = "";
    public static final String SINGLE_DASH_IN_ALL_NAMES = "";

    private static final Pattern rgx_pair = Pattern.compile("^([^ ]+)=([^ ]+)$");

    private final String[] indexedArgsNames;
    private final Class<?>[] indexedArgsClassTypes;
    private final ObjectResolver[] indexedArgsClassTypesResolvers;

    private final String[] namedArgsNames;
    private final Class<?>[] namedArgsClassTypes;
    private final ObjectResolver[] namedArgsClassTypesResolvers;

    public ArgsEvaler(String[] indexedArgsNames, Class<?>[] indexedArgsClassTypes, String[] namedArgsNames, Class<?>[] namedArgsClassTypes) {
        if (indexedArgsNames.length != indexedArgsClassTypes.length)
            throw new IllegalArgumentException("The number of indexed argument names provided must match the number of class types given.");
        if (namedArgsNames.length != namedArgsClassTypes.length)
            throw new IllegalArgumentException("The number of named argument names provided must match the number of class types given.");

        this.indexedArgsNames = indexedArgsNames;
        this.indexedArgsClassTypes = indexedArgsClassTypes;
        this.namedArgsNames = namedArgsNames;
        this.namedArgsClassTypes = namedArgsClassTypes;

        this.indexedArgsClassTypesResolvers = new ObjectResolver[this.indexedArgsClassTypes.length];
        for (int i = 0, s = this.indexedArgsClassTypes.length; i < s; i++) {
            this.indexedArgsClassTypesResolvers[i]
                    = ObjectResolver.DEFAULT_RESOLVERS
                    .get(this.indexedArgsClassTypes[i]);
        }

        this.namedArgsClassTypesResolvers = new ObjectResolver[this.namedArgsClassTypes.length];
        for (int i = 0, s = this.namedArgsClassTypes.length; i < s; i++) {
            this.namedArgsClassTypesResolvers[i]
                    = ObjectResolver.DEFAULT_RESOLVERS
                    .get(this.namedArgsClassTypes[i]);
        }
    }

    private void addResolver(Class clazz, ObjectResolver objectResolver) {
        for (int i = 0; i < indexedArgsNames.length; i++)
            if (Objects.equals(indexedArgsClassTypes[i], clazz))
                indexedArgsClassTypesResolvers[i] = objectResolver;
        for (int i = 0; i < namedArgsNames.length; i++)
            if (Objects.equals(namedArgsClassTypes[i], clazz))
                namedArgsClassTypesResolvers[i] = objectResolver;
    }

    public HashMap<String, Object> parse(String... args) {
        return parse(new LinkedList<>() {{
            Collections.addAll(this, args);
        }});
    }

    public HashMap<String, Object> parse(List<String> args) {
        HashMap<String, Object> map = new HashMap<>();

        for (int namedArgsIdx = 0, namedArgsS = namedArgsNames.length; namedArgsIdx < namedArgsS; namedArgsIdx++) {
            for (int argsIdx = 0, argsS = args.size(); argsIdx < argsS; argsIdx++) {

                String arg = args.get(argsIdx);
                String name = namedArgsNames[namedArgsIdx];

                if (arg.indexOf('=') != -1 && arg.startsWith(name)) {

                    Matcher matcher = rgx_pair.matcher(arg);
                    if (!matcher.matches() || !matcher.group(1).equals(name)) continue;

                    args.remove(argsIdx--);
                    argsS--;

                    map.put(
                            name, namedArgsClassTypesResolvers[namedArgsIdx]
                                    .objectify(namedArgsClassTypes[namedArgsIdx], matcher.group(2))
                    );
                }
            }
        }

        for (int i = 0, s = Math.min(indexedArgsNames.length, args.size()); i < s; i++) {
            map.put(
                    indexedArgsNames[i],
                    indexedArgsClassTypesResolvers[i]
                            .objectify(indexedArgsClassTypes[i], args.get(i))
            );
        }

        return map;
    }

    public ResultMap parseToResultMap(String... args) {
        return new ResultMap(parse(args));
    }

    public static class ArgsEvalerBuilder {

        private final List<String> indexedArgsNames = new ArrayList<>();
        private final List<Class<?>> indexedArgsClassTypes = new ArrayList<>();

        private final List<String> namedArgsNames = new ArrayList<>();
        private final List<Class<?>> namedArgsClassTypes = new ArrayList<>();

        private final HashMap<Class, ObjectResolver> objectResolvers = new HashMap<>();

        public ArgsEvalerBuilder addIndexed(String name) {
            return addIndexed(name, String.class);
        }

        public ArgsEvalerBuilder addIndexed(String name, Class<?> clazz) {
            indexedArgsNames.add(name);
            indexedArgsClassTypes.add(clazz);
            return this;
        }

        public ArgsEvalerBuilder addNamed(String name) {
            return addNamed(name, String.class);
        }

        public ArgsEvalerBuilder addNamed(String name, Class<?> clazz) {
            namedArgsNames.add(name);
            namedArgsClassTypes.add(clazz);
            return this;
        }

        public ArgsEvalerBuilder addResolver(Class clazz, ObjectResolver objectResolver) {
            objectResolvers.put(clazz, objectResolver);
            return this;
        }

        public ArgsEvaler build() {
            ArgsEvaler argsEvaler = new ArgsEvaler(
                    indexedArgsNames.toArray(String[]::new),
                    indexedArgsClassTypes.toArray(Class[]::new),
                    namedArgsNames.toArray(String[]::new),
                    namedArgsClassTypes.toArray(Class[]::new)
            );
            objectResolvers.forEach(argsEvaler::addResolver);
            return argsEvaler;
        }
    }

    public class ResultMap extends AbstractMap<String, Object> {

        private final HashMap<String, Object> map;

        public ResultMap(HashMap<String, Object> map) {
            this.map = map;
        }

        @SuppressWarnings("unchecked")
        public <ReType> ReType get(String name) {
            return (ReType) map.get(name);
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return map.entrySet();
        }
    }
}
