package in.mcxiv.math.gen;

import java.util.Random;

public class MathBox {

    @SupportedTypes({Float.class, Double.class})
    public static float map(float val, float minVal, float maxVal, float minReq, float maxReq) {
        return (val - minVal) / (maxVal - minVal) * (maxReq - minReq) + minReq;
    }

    @SupportedTypes({Float.class, Double.class})
    public static float map01(float val, float min, float max) {
        return (val - min) / (max - min);
    }

    private static final class RandomNumberGeneratorHolder {
        static final Random randomNumberGenerator = new Random();
    }

    public static short random(short min, short max) {
        return (short) RandomNumberGeneratorHolder.randomNumberGenerator
                .nextInt(min, max);
    }

    public static byte random(byte min, byte max) {
        return (byte) RandomNumberGeneratorHolder.randomNumberGenerator
                .nextInt(min, max);
    }

    @SupportedTypes({Integer.class, Float.class, Long.class, Double.class})
    public static float random(float min, float max) {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextFloat(min, max);
    }

    @SupportedTypes({Integer.class, Float.class, Long.class, Double.class})
    public static float random(float max) {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextFloat(max);
    }

    @SupportedTypes({Integer.class, Float.class, Long.class, Double.class})
    public static float randomFloat() {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextFloat();
    }
}
