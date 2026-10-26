package net.enchadd.mod.common;

public final class NourishRules {

    private static final int MAX_LEVEL = 3;
    private static final int SECONDS_PER_LEVEL = 2;
    private static final int MAX_DURATION_TICKS = 120;

    private NourishRules() {
    }

    public static int durationTicks(int level) {
        if (level <= 0) {
            return 0;
        }
        long ticks = (long) Math.min(level, MAX_LEVEL) * SECONDS_PER_LEVEL * 20L;
        return (int) Math.min(MAX_DURATION_TICKS, ticks);
    }

    public static int amplifier() {
        return 0;
    }
}
