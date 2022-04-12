package in.mcxiv;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiFunction;

@FunctionalInterface
public interface ObjectResolver extends BiFunction<Class, String, Object> {

    HashMap<Class<?>, ObjectResolver> DEFAULT_RESOLVERS = new HashMap<>() {{
        put(boolean.class, (c, s) -> Boolean.parseBoolean(s));
        put(Boolean.class, (c, s) -> Boolean.parseBoolean(s));
        put(byte.class, (c, s) -> Byte.parseByte(s));
        put(Byte.class, (c, s) -> Byte.parseByte(s));
        put(char.class/* */, (c, s) -> s.charAt(0));
        put(Character.class, (c, s) -> s.charAt(0));
        put(short.class, (c, s) -> Short.parseShort(s));
        put(Short.class, (c, s) -> Short.parseShort(s));
        put(int.class/**/, (c, s) -> Integer.parseInt(s));
        put(Integer.class, (c, s) -> Integer.parseInt(s));
        put(float.class, (c, s) -> Float.parseFloat(s));
        put(Float.class, (c, s) -> Float.parseFloat(s));
        put(long.class, (c, s) -> Long.parseLong(s));
        put(Long.class, (c, s) -> Long.parseLong(s));
        put(double.class, (c, s) -> Double.parseDouble(s));
        put(Double.class, (c, s) -> Double.parseDouble(s));

        put(String.class, (c, s) -> s);
        put(StringBuilder.class, (c, s) -> new StringBuilder(s));
        put(StringBuffer.class, (c, s) -> new StringBuffer(s));

        put(BigInteger.class, (c, s) -> new BigInteger(s));
        put(BigDecimal.class, (c, s) -> new BigDecimal(s));
        put(AtomicInteger.class, (c, s) -> new AtomicInteger(Integer.parseInt(s)));
        put(AtomicLong.class, (c, s) -> new AtomicLong(Long.parseLong(s)));
        put(DoubleAdder.class, (c, s) -> {
            DoubleAdder adder = new DoubleAdder();
            adder.add(Double.parseDouble(s));
            return adder;
        });
        put(LongAdder.class, (c, s) -> {
            LongAdder adder = new LongAdder();
            adder.add(Long.parseLong(s));
            return adder;
        });

        put(File.class, (c, s) -> new File(s));
    }};

    @Override
    Object apply(Class objectClass, String s);

    default Object objectify(Class typeClass, String s) {
        return apply(typeClass, s);
    }

    default <ReType> ReType rectify(Class<ReType> typeClass, String s) {
        Object object = objectify(typeClass, s);
        return typeClass.cast(object);
    }
}
