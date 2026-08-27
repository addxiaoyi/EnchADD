package net.enchadd.utils;

final class PerformanceMathSupport {

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
        return Math.max(min, Math.min(max, value));
    }

    static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static int calculateDurationTicks(int seconds, int level) {
        return Math.max(1, seconds * level) * 20;
    }

    static int calculateDurationTicksPerLevel(int secondsPerLevel, int level) {
        return Math.max(1, secondsPerLevel * level) * 20;
    }
}
