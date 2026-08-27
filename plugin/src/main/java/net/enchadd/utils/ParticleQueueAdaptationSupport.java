package net.enchadd.utils;

final class ParticleQueueAdaptationSupport {

    private ParticleQueueAdaptationSupport() {
    }

    static double windowDropRate(int offered, int dropped) {
        return offered > 0 ? dropped / (double) offered : 0.0;
    }

    static int adaptiveStep(int baseMaxPerTick) {
        return Math.max(5, baseMaxPerTick / 10);
    }

    static boolean shouldIncrease(double lastWindowDropRate, double queueFillRatio, double warnDropRateThreshold) {
        return lastWindowDropRate > warnDropRateThreshold || queueFillRatio >= 0.70;
    }

    static boolean shouldDecrease(double lastWindowDropRate, double queueFillRatio) {
        return lastWindowDropRate == 0.0 && queueFillRatio <= 0.20;
    }

    static int increaseBudget(int current, int max, int step) {
        return Math.min(max, current + step);
    }

    static int decreaseBudget(int current, int min, int step) {
        return Math.max(min, current - step);
    }

    static boolean shouldWarn(double lastWindowDropRate, double warnDropRateThreshold) {
        return lastWindowDropRate > warnDropRateThreshold;
    }
}
