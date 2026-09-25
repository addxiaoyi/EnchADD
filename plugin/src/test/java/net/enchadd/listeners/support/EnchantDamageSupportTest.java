package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantDamageSupportTest {
    @Test
    void flatBonusKeepsNormalScalingAndRespectsBothCaps() {
        assertEquals(2.25, EnchantDamageSupport.bonusDamage(3, 0.75, 3.0));
        assertEquals(1.5, EnchantDamageSupport.bonusDamage(3, 0.75, 1.5));
        assertEquals(4.0, EnchantDamageSupport.bonusDamage(Integer.MAX_VALUE, Double.MAX_VALUE, 10));
    }

    @Test
    void invalidPerLevelOrMaximumCannotContaminateDamage() {
        for (double invalid : new double[]{Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, -1.0, 0.0}) {
            assertEquals(0.0, EnchantDamageSupport.bonusDamage(3, invalid, 4));
            assertEquals(0.0, EnchantDamageSupport.bonusDamage(3, 1, invalid));
        }
        assertEquals(0.0, EnchantDamageSupport.bonusDamage(-1, 1, 4));
    }

    @Test
    void zeroDamageHitsStayHarmless() {
        assertEquals(0.0, EnchantDamageSupport.addBonus(0, 4));
        assertEquals(8.0, EnchantDamageSupport.addBonus(5, 3));
        assertEquals(5.0, EnchantDamageSupport.addBonus(5, Double.NaN));
    }

    @Test
    void executionerFifthLevelRetainsItsIntendedBonus() {
        assertEquals(12.5, EnchantDamageSupport.executionerDamage(10, 5, 0.05, 4, 20, 0.25));
        assertEquals(12.0, EnchantDamageSupport.executionerDamage(10, 4, 0.05, 4, 20, 0.25));
    }

    @Test
    void executionerOnlyActivatesBelowTheHealthThreshold() {
        assertEquals(10.0, EnchantDamageSupport.executionerDamage(10, 5, 0.05, 5, 20, 0.25));
        assertEquals(10.0, EnchantDamageSupport.executionerDamage(10, 5, 0.05, 20, 20, 2));
    }

    @Test
    void executionerRejectsInvalidHealthAndConfiguration() {
        assertEquals(10.0, EnchantDamageSupport.executionerDamage(10, 5, 0.05, 4, 0, 0.25));
        assertEquals(10.0, EnchantDamageSupport.executionerDamage(10, 5, Double.NaN, 4, 20, 0.25));
        assertEquals(10.0, EnchantDamageSupport.executionerDamage(10, 5, 0.05, 4, 20, Double.NaN));
        assertEquals(10.0, EnchantDamageSupport.executionerDamage(10, 5, -0.5, 4, 20, 0.25));
        assertEquals(0.0, EnchantDamageSupport.executionerDamage(0, 5, 0.05, 4, 20, 0.25));
    }

    @Test
    void executionerCapsExtremeMultipliersAndAvoidsOverflow() {
        assertEquals(25.0, EnchantDamageSupport.executionerDamage(10, 5, Double.MAX_VALUE, 4, 20, 0.25));
        assertEquals(Double.MAX_VALUE, EnchantDamageSupport.executionerDamage(
                Double.MAX_VALUE, 5, 0.05, 4, 20, 0.25));
    }
}
