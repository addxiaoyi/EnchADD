package net.enchadd.listeners.support;

import org.bukkit.util.Vector;

public final class ShadowstrikeHitSupport {
    private static final double MAX_BONUS_DAMAGE = 4.0;

    private ShadowstrikeHitSupport() {
    }

    public static double damage(double base, int level, double perLevel,
                                Vector facing, Vector toAttacker) {
        if (!CombatTriggerSupport.isBehind(facing, toAttacker)) {
            return base;
        }
        double bonus = EnchantDamageSupport.bonusDamage(level, perLevel, MAX_BONUS_DAMAGE);
        return EnchantDamageSupport.addBonus(base, bonus);
    }
}
