package de.longri.mapsforge;

/**
 * Created by Hoepfner on 21.01.2016.
 */
public class MemoryUsage {
    private static long maxMemory;

    private static int GcCount = 0;

    public static void chekMemory() {
        if (GcCount++ > 1000) {
            System.gc();
            GcCount = 0;
        }

        maxMemory = Math.max(maxMemory, getMemoryUsage());
    }

    public static void resetMemoryUsage() {
        System.gc();
        maxMemory = 0;
        System.gc();
    }

    public static long getMaxMemory() {
        return maxMemory;
    }

    private static long getMemoryUsage() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }


}
