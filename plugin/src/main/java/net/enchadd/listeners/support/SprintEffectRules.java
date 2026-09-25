package net.enchadd.listeners.support;

final class SprintEffectRules {
    private static final int MAX_SECONDS = 120;
    private static final int MAX_AMPLIFIER = 2;

    private SprintEffectRules() {
    }

    static int seconds(int level, int maxLevel, int perLevel) {
        if (level <= 0 || maxLevel <= 0 || perLevel <= 0) return 0;
        return (int) Math.min(MAX_SECONDS, (long) Math.min(level, maxLevel) * perLevel);
    }

    static int slowAmplifier(int base, int level, int maxLevel) {
        if (level <= 0 || maxLevel <= 0) return 0;
        long scaled = (long) base + Math.min(level, maxLevel) - 1;
        return (int) Math.min(MAX_AMPLIFIER, Math.max(0L, scaled));
    }
}
