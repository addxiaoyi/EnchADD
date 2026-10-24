package net.enchadd.mod.common;

public final class FreshcatchRules {

    private static final int MAX_LEVEL = 2;
    private static final int FOOD_PER_LEVEL = 1;
    private static final float SATURATION_PER_LEVEL = 0.5f;

    private FreshcatchRules() {
    }

    public static int foodLevel(int current, int level) {
        if (current < 0 || level <= 0) return current;
        return Math.min(20, current + Math.min(level, MAX_LEVEL) * FOOD_PER_LEVEL);
    }

    public static float saturation(float current, int foodLevel, int level) {
        if (!Float.isFinite(current) || foodLevel < 0 || level <= 0) return current;
        float restored = current + Math.min(level, MAX_LEVEL) * SATURATION_PER_LEVEL;
        return Math.max(current, Math.min(foodLevel, restored));
    }
}
