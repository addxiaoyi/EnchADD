package net.enchadd.listeners.support;

import org.bukkit.util.Vector;

final class CombatTriggerSupport {
    private static final double BACKSTAB_DOT_THRESHOLD = -0.7;
    private static final double MIN_DIRECTION_LENGTH_SQUARED = 1.0e-12;

    private CombatTriggerSupport() {
    }

    static boolean isBehind(Vector facing, Vector toAttacker) {
        double facingLength = facing.lengthSquared();
        double offsetLength = toAttacker.lengthSquared();
        if (!Double.isFinite(facingLength) || !Double.isFinite(offsetLength)
                || facingLength <= MIN_DIRECTION_LENGTH_SQUARED
                || offsetLength <= MIN_DIRECTION_LENGTH_SQUARED) {
            return false;
        }
        double dot = facing.clone().normalize().dot(toAttacker.clone().normalize());
        return Double.isFinite(dot) && dot < BACKSTAB_DOT_THRESHOLD;
    }

    static int requiredHits(int base, int reduction, int minimum, int level) {
        long scaled = (long) base - (Math.max(1L, level) - 1L) * Math.max(0L, reduction);
        return (int) Math.min(Integer.MAX_VALUE, Math.max(Math.max(1L, minimum), scaled));
    }

    static int nextCombo(int current) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, current) + 1L);
    }
}
