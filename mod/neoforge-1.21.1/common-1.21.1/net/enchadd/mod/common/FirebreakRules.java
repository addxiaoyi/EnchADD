package net.enchadd.mod.common;

public final class FirebreakRules {
    private static final int MAX_LEVEL = 3;
    private static final double COMBUSTION_REDUCTION_PER_LEVEL = 0.20;
    private static final double MAX_COMBUSTION_REDUCTION = 0.60;
    private static final double DAMAGE_REDUCTION_PER_LEVEL = 0.08;
    private static final double MAX_DAMAGE_REDUCTION = 0.24;

    private FirebreakRules() {}

    public static int adjustBurnTicks(int ticks, int level) {
        if (ticks <= 0) return ticks;
        double adjusted = ticks * (1.0 - combustionReduction(level));
        if (!Double.isFinite(adjusted)) return ticks;
        return Math.max(0, (int) Math.floor(adjusted));
    }

    public static float adjustDamage(float damage, int level) {
        if (!Float.isFinite(damage) || damage <= 0.0f) return damage;
        double adjusted = damage * (1.0 - damageReduction(level));
        return Double.isFinite(adjusted) ? (float) Math.max(0.0, adjusted) : damage;
    }

    private static double combustionReduction(int level) {
        return Math.min(MAX_COMBUSTION_REDUCTION,
                COMBUSTION_REDUCTION_PER_LEVEL * clampLevel(level));
    }

    private static double damageReduction(int level) {
        return Math.min(MAX_DAMAGE_REDUCTION,
                DAMAGE_REDUCTION_PER_LEVEL * clampLevel(level));
    }

    private static int clampLevel(int level) {
        return Math.max(0, Math.min(MAX_LEVEL, level));
    }
}
