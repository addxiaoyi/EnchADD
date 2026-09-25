package net.enchadd.utils;

final class PerformanceMathSupport {

    private static final int MAX_EFFECT_DURATION_SECONDS = 120;

    private PerformanceMathSupport() {
    }

    static double safeDivide(double numerator, double denominator, double defaultValue) {
        if (denominator == 0.0 || Double.isNaN(denominator) || Double.isInfinite(denominator)) {
            return defaultValue;
        }
        double result = numerator / denominator;
        return Double.isNaN(result) || Double.isInfinite(result) ? defaultValue : result;
    }

    static double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) {
            return min;
        }
        if (!Double.isFinite(min) || !Double.isFinite(max)) {
            return value;
        }
        if (min > max) {
            double lower = max;
            max = min;
            min = lower;
        }
        return Math.max(min, Math.min(max, value));
    }

    static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static int calculateDurationTicks(int seconds, int level) {
        long rawSeconds = (long) seconds * level;
        long safeSeconds = Math.min(MAX_EFFECT_DURATION_SECONDS, Math.max(1L, rawSeconds));
        return (int) (safeSeconds * 20L);
    }

    static int calculateDurationTicksPerLevel(int secondsPerLevel, int level) {
        long rawSeconds = (long) secondsPerLevel * level;
        long safeSeconds = Math.min(MAX_EFFECT_DURATION_SECONDS, Math.max(1L, rawSeconds));
        return (int) (safeSeconds * 20L);
    }
}
