package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QuellDamageSupportTest {
    @Test
    void normalLevelsKeepExistingReductionAndCeiling() {
        assertEquals(8, EnchantDamageSupport.quellDamage(10, 10, 1, 3, 0.2));
        assertEquals(6, EnchantDamageSupport.quellDamage(10, 10, 2, 3, 0.2));
        assertEquals(5, EnchantDamageSupport.quellDamage(10, 10, 3, 3, 0.2));
        assertEquals(8, EnchantDamageSupport.quellDamage(10, 10, Integer.MAX_VALUE, 1, 0.2));
    }

    @Test
    void fullyAbsorbedOrInvalidHitsRemainUnchanged() {
        for (double damage : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(10, EnchantDamageSupport.quellDamage(10, damage, 3, 3, 0.2));
        }
        assertEquals(0, EnchantDamageSupport.quellDamage(0, 5, 3, 3, 0.2));
    }

    @Test
    void invalidConfigurationCannotReduceDamage() {
        for (double rate : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(10, EnchantDamageSupport.quellDamage(10, 5, 3, 3, rate));
        }
        assertEquals(10, EnchantDamageSupport.quellDamage(10, 5, 3, 0, 0.2));
    }

    @Test
    void extremeFiniteRatesCannotGrantImmunity() {
        assertEquals(5, EnchantDamageSupport.quellDamage(10, 5, 3, 3, Double.MAX_VALUE));
        assertEquals(Double.MAX_VALUE * 0.5, EnchantDamageSupport.quellDamage(
                Double.MAX_VALUE, 5, 3, 3, 0.2));
    }
}
