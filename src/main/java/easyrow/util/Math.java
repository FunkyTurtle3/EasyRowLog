package easyrow.util;

public class Math {

    public static int getRandomRangedInt(int min, int max) {
        return (int) ((java.lang.Math.random() * (max - min)) + min);
    }
}
