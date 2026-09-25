package net.enchadd.listeners.support;

final class RetaliationRules {
    private static final double MAX_RIPOSTE_CHANCE = 0.85;

    private RetaliationRules() {
    }

    static int weaknessTicks(int level, int maxLevel, int secondsPerLevel) {
        if (level <= 0 || maxLevel <= 0 || secondsPerLevel <= 0) return 0;
        return (int) Math.min(120L, (long) Math.min(level, maxLevel) * secondsPerLevel) * 20;
    }

    static double riposteChance(int level, int maxLevel, double chance) {
        if (level <= 0 || maxLevel <= 0 || !Double.isFinite(chance) || chance <= 0) return 0;
        return Math.min(MAX_RIPOSTE_CHANCE, chance * Math.min(level, maxLevel));
    }

    static boolean canArm(int level, int maxLevel, int ticks, double perLevel, double maxBonus) {
        return ticks > 0 && EnchantDamageSupport.bonusDamage(Math.min(level, maxLevel), perLevel, maxBonus) > 0;
    }
}
