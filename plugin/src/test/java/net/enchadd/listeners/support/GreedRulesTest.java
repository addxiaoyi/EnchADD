package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GreedRulesTest {
    @Test
    void defaultRewardsAndCostsKeepTheirProgression() {
        for (int level = 1; level <= 3; level++) {
            assertEquals(100 + 30 * level, GreedRules.experience(100, level, 3, 0.3, 3));
            assertEquals(1 + 0.25 * level, GreedRules.vulnerability(level, 3, 0.25, 1));
            assertEquals(120 * level, GreedRules.durationTicks(level, 3, 6));
        }
    }

    @Test
    void unsafeEnchantLevelsCannotExceedConfiguredLevel() {
        assertEquals(190, GreedRules.experience(100, Integer.MAX_VALUE, 3, 0.3, 3));
        assertEquals(1.75, GreedRules.vulnerability(Integer.MAX_VALUE, 3, 0.25, 1));
        assertEquals(360, GreedRules.durationTicks(Integer.MAX_VALUE, 3, 6));
    }

    @Test
    void invalidRewardsNeverRemoveExperience() {
        for (double invalid : new double[]{-1, 0, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(100, GreedRules.experience(100, 3, 3, invalid, 3));
            assertEquals(100, GreedRules.experience(100, 3, 3, 0.3, invalid));
        }
        assertEquals(100, GreedRules.experience(100, 3, 3, 0.3, 0.5));
        assertEquals(0, GreedRules.experience(0, 3, 3, 0.3, 3));
    }

    @Test
    void extremeFiniteRewardsAndCostsAreBounded() {
        assertEquals(200, GreedRules.experience(100, 3, 3, Double.MAX_VALUE, 3));
        assertEquals(Integer.MAX_VALUE, GreedRules.experience(Integer.MAX_VALUE, 3, 3, 0.3, 3));
        assertEquals(2.0, GreedRules.vulnerability(3, 3, Double.MAX_VALUE, 10));
        assertEquals(1200, GreedRules.durationTicks(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    void invalidLevelsAndDurationsDisableEffects() {
        assertEquals(100, GreedRules.experience(100, 0, 3, 0.3, 3));
        assertEquals(100, GreedRules.experience(100, 3, 0, 0.3, 3));
        assertEquals(1.0, GreedRules.vulnerability(0, 3, 0.25, 1));
        assertEquals(1.0, GreedRules.vulnerability(3, 0, 0.25, 1));
        assertEquals(0, GreedRules.durationTicks(3, 3, 0));
        assertEquals(0, GreedRules.durationTicks(3, 3, -1));
        assertEquals(0, GreedRules.durationTicks(3, 0, 6));
    }

    @Test
    void invalidVulnerabilityCannotWriteNonFiniteDamage() {
        for (double invalid : new double[]{-1, 0, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(1.0, GreedRules.vulnerability(3, 3, invalid, 1));
            assertEquals(1.0, GreedRules.vulnerability(3, 3, 0.25, invalid));
            assertEquals(10, GreedRules.damage(10, invalid));
        }
    }

    @Test
    void damagePreservesZeroAndRejectsOverflow() {
        assertEquals(17.5, GreedRules.damage(10, 1.75));
        assertEquals(20, GreedRules.damage(10, 10));
        assertEquals(0, GreedRules.damage(0, 2));
        assertEquals(-1, GreedRules.damage(-1, 2));
        assertEquals(Double.MAX_VALUE, GreedRules.damage(Double.MAX_VALUE, 2));
        assertTrue(Double.isNaN(GreedRules.damage(Double.NaN, 2)));
    }
}
