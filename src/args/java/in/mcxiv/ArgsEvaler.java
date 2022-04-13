package in.mcxiv;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgsEvaler {

    private static final Pattern rgx_pair = Pattern.compile("^([^ ]+)=([^ ]+)$");

    private final String[] indexedArgsNames;
    private final Class<?>[] indexedArgsClassTypes;
    private final ObjectResolver[] indexedArgsClassTypesResolvers;

    private final String[] namedArgsNames;
    private final Class<?>[] namedArgsClassTypes;
    private final ObjectResolver[] namedArgsClassTypesResolvers;

    private final String[] taggedArgsNames;
    private final Class<?>[] taggedArgsClassTypes;
    private final ObjectResolver[] taggedArgsClassTypesResolvers;

    public ArgsEvaler(String[] indexedArgsNames, Class<?>[] indexedArgsClassTypes,
                      String[] namedArgsNames, Class<?>[] namedArgsClassTypes,
                      String[] taggedArgsNames, Class<?>[] taggedArgsClassTypes) {

        if (indexedArgsNames.length != indexedArgsClassTypes.length)
            throw new IllegalArgumentException("The number of indexed argument names provided must match the number of class types given.");
        if (namedArgsNames.length != namedArgsClassTypes.length)
            throw new IllegalArgumentException("The number of named argument names provided must match the number of class types given.");
        if (taggedArgsNames.length != taggedArgsClassTypes.length)
            throw new IllegalArgumentException("The number of tagged argument names provided must match the number of class types given.");

        this.indexedArgsNames = indexedArgsNames;
        this.indexedArgsClassTypes = indexedArgsClassTypes;
        this.namedArgsNames = namedArgsNames;
        this.namedArgsClassTypes = namedArgsClassTypes;
        this.taggedArgsNames = taggedArgsNames;
        this.taggedArgsClassTypes = taggedArgsClassTypes;

        this.indexedArgsClassTypesResolvers = makeArgsClassTypesResolvers(this.indexedArgsClassTypes);
        this.namedArgsClassTypesResolvers = makeArgsClassTypesResolvers(this.namedArgsClassTypes);
        this.taggedArgsClassTypesResolvers = makeArgsClassTypesResolvers(this.taggedArgsClassTypes);
    }

    private static ObjectResolver[] makeArgsClassTypesResolvers(Class[] argsClassTypes) {
        ObjectResolver[] argsClassTypesResolvers = new ObjectResolver[argsClassTypes.length];
        for (int i = 0, s = argsClassTypes.length; i < s; i++) {
            argsClassTypesResolvers[i]
                    = ObjectResolver.DEFAULT_RESOLVERS
                    .get(argsClassTypes[i]);
        }
        return argsClassTypesResolvers;
    }

    public void addResolver(Class clazz, ObjectResolver objectResolver) {
        adjustResolverFor(indexedArgsNames, taggedArgsClassTypes, indexedArgsClassTypesResolvers, clazz, objectResolver);
        adjustResolverFor(taggedArgsNames, taggedArgsClassTypes, taggedArgsClassTypesResolvers, clazz, objectResolver);
        adjustResolverFor(namedArgsNames, namedArgsClassTypes, namedArgsClassTypesResolvers, clazz, objectResolver);
    }

    private static void adjustResolverFor(String[] argsNames, Class<?>[] argsClassTypes, ObjectResolver[] argsClassTypesResolvers, Class clazz, ObjectResolver objectResolver) {
        for (int i = 0; i < argsNames.length; i++)
            if (Objects.equals(argsClassTypes[i], clazz))
                argsClassTypesResolvers[i] = objectResolver;
    }

    public HashMap<String, Object> parse(String... args) {
        return parse(new LinkedList<>() {{
            Collections.addAll(this, args);
        }});
    }

    public HashMap<String, Object> parse(List<String> args) {
        HashMap<String, Object> map = new HashMap<>();

        for (int taggedArgsIdx = 0, taggedArgsS = taggedArgsNames.length; taggedArgsIdx < taggedArgsS; taggedArgsIdx++) {
            String name = taggedArgsNames[taggedArgsIdx];

            for (int argsIdx = 0, argsS = args.size() - 1; argsIdx < argsS; argsIdx++) {

                String arg = args.get(argsIdx);

                if (Objects.equals(name, arg)) {
                    map.put(
                            name, taggedArgsClassTypesResolvers[taggedArgsIdx]
                                    .objectify(taggedArgsClassTypes[taggedArgsIdx], args.get(argsIdx + 1))
                    );

                    args.remove(argsIdx);
                    args.remove(argsIdx);

                    break;
                }
            }
        }

        for (int namedArgsIdx = 0, namedArgsS = namedArgsNames.length; namedArgsIdx < namedArgsS; namedArgsIdx++) {
            String name = namedArgsNames[namedArgsIdx];

            for (int argsIdx = 0, argsS = args.size(); argsIdx < argsS; argsIdx++) {

                String arg = args.get(argsIdx);

                if (arg.indexOf('=') != -1 && arg.startsWith(name)) {

                    Matcher matcher = rgx_pair.matcher(arg);
                    if (!matcher.matches() || !matcher.group(1).equals(name)) continue;

                    args.remove(argsIdx);
//                    args.remove(argsIdx--);
//                    argsS--;

                    map.put(
                            name, namedArgsClassTypesResolvers[namedArgsIdx]
                                    .objectify(namedArgsClassTypes[namedArgsIdx], matcher.group(2))
                    );

                    break;
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

        private final List<String> taggedArgsNames = new ArrayList<>();
        private final List<Class<?>> taggedArgsClassTypes = new ArrayList<>();

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

        public ArgsEvalerBuilder addTagged(String name) {
            return addTagged(name, String.class);
        }

        public ArgsEvalerBuilder addTagged(String name, Class<?> clazz) {
            taggedArgsNames.add(name);
            taggedArgsClassTypes.add(clazz);
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
                    namedArgsClassTypes.toArray(Class[]::new),
                    taggedArgsNames.toArray(String[]::new),
                    taggedArgsClassTypes.toArray(Class[]::new));
            objectResolvers.forEach(argsEvaler::addResolver);
            return argsEvaler;
        }
    }

    public static class ResultMap extends AbstractMap<String, Object> {

        private final HashMap<String, Object> map;

        public ResultMap(HashMap<String, Object> map) {
            this.map = map;
        }

        @SuppressWarnings("unchecked")
        public <ReType> ReType get(String name) {
            Object o = map.get(name);
            if (o == null) return null;
            return (ReType) o;
        }

        public Optional<Object> getOpt(String name) {
            return Optional.ofNullable(map.get(name));
        }

        @SuppressWarnings("unchecked")
        public <ReType> ReType get(String name, ReType def) {
            Object o = map.get(name);
            if (o == null) return def;
            try {
                return (ReType) o;
            } catch (Exception e) {
                return def;
            }
        }

        public Optional<Object> getOpt(String name, Object def) {
            Object o = map.get(name);
            return Optional.of(o != null ? o : def);
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return map.entrySet();
        }
    }
}
