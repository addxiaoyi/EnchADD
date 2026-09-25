package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BreakguardDamageSupportTest {
    @Test
    void defaultBonusRemainsUnchanged() {
        assertEquals(0.75, BreakguardDamageSupport.bonusDamage(1, 3, 0.75, 2.25));
        assertEquals(1.5, BreakguardDamageSupport.bonusDamage(2, 3, 0.75, 2.25));
        assertEquals(2.25, BreakguardDamageSupport.bonusDamage(3, 3, 0.75, 2.25));
    }

    @Test
    void levelAndDamageCapsApplyIndependently() {
        assertEquals(1.5, BreakguardDamageSupport.bonusDamage(Integer.MAX_VALUE, 2, 0.75, 4));
        assertEquals(1.0, BreakguardDamageSupport.bonusDamage(3, 3, 0.75, 1));
        assertEquals(4.0, BreakguardDamageSupport.bonusDamage(3, 3, Double.MAX_VALUE, 10));
        assertEquals(0.0, BreakguardDamageSupport.bonusDamage(3, 0, 0.75, 4));
    }

    @Test
    void invalidBonusesLeaveIncomingDamageUnchanged() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            double badRate = BreakguardDamageSupport.bonusDamage(3, 3, invalid, 4);
            double badCap = BreakguardDamageSupport.bonusDamage(3, 3, 0.75, invalid);
            assertEquals(5.0, EnchantDamageSupport.addBonus(5, badRate));
            assertEquals(5.0, EnchantDamageSupport.addBonus(5, badCap));
        }
    }
}
