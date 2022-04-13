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

    public class ResultMap extends AbstractMap<String, Object> {

        private final HashMap<String, Object> map;

        public ResultMap(HashMap<String, Object> map) {
            this.map = map;
        }

        @SuppressWarnings("unchecked")
        @Deprecated
        public <ReType> ReType get(String name) {
            return (ReType) map.get(name);
        }

        public <ReType> ReType get(String name, Class<ReType> def) {

        }

        @SuppressWarnings("unchecked")
        public <ReType> ReType get(String name, ReType def) {
            Object o = map.get(name);
            if (o == null) return def;

            int x = indexOf(name, indexedArgsNames);

            // Test this method and then implement optional
            //

            if (x != -1) {
                if (indexedArgsClassTypes[x].isInstance(def))
                    return ((Class<? extends ReType>) indexedArgsClassTypes[x]).cast(o);
                else throw new ClassCastException(o.getClass() + " can not be cast to the required type.");
            }

            x = indexOf(name, namedArgsNames);

            if (x != -1) {
                if (namedArgsClassTypes[x].isInstance(def))
                    return ((Class<? extends ReType>) namedArgsClassTypes[x]).cast(o);
                else throw new ClassCastException(o.getClass() + " can not be cast to the required type.");
            }

            x = indexOf(name, taggedArgsNames);

            if (x != -1) {
                if (taggedArgsClassTypes[x].isInstance(def))
                    return ((Class<? extends ReType>) taggedArgsClassTypes[x]).cast(o);
                else throw new ClassCastException(o.getClass() + " can not be cast to the required type.");
            }

            return def;
        }

        private static int indexOf(String value, String[] array) {
            for (int i = 0; i < array.length; i++)
                if (Objects.equals(value, array[i]))
                    return i;
            return -1;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return map.entrySet();
        }
    }
}
