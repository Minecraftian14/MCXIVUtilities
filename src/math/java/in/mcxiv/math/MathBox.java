package in.mcxiv.math;

import java.util.Random;

public class MathBox {

    private static final class RandomNumberGeneratorHolder {
        static final Random randomNumberGenerator = new Random();
    }

    public static float mapTo01(float val, float min, float max) {
        return (val - min) / (max - min);
    }

    public static double mapTo01(double val, double min, double max) {
        return (val - min) / (max - min);
    }



    public static short random(short min, short max) {
        return (short) RandomNumberGeneratorHolder.randomNumberGenerator
                .nextInt(min, max);
    }

    public static byte random(byte min, byte max) {
        return (byte) RandomNumberGeneratorHolder.randomNumberGenerator
                .nextInt(min, max);
    }

    public static int random(int min, int max) {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextInt(min, max);
    }

    public static float random(float min, float max) {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextFloat(min, max);
    }

    public static long random(long min, long max) {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextLong(min, max);
    }

    public static double random(double min, double max) {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextDouble(min, max);
    }



    public static int random(int max) {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextInt(max);
    }

    public static float random(float max) {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextFloat(max);
    }

    public static long random(long max) {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextLong(max);
    }

    public static double random(double max) {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextDouble(max);
    }



    public static int randomInt() {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextInt();
    }

    public static float randomFloat() {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextFloat();
    }

    public static long randomLong() {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextLong();
    }

    public static double randomDouble() {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextDouble();
    }


}
