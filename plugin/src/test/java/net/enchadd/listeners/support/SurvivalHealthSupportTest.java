package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurvivalHealthSupportTest {
    @Test
    void lethalCheckIncludesExactHealthButExcludesZeroOrInvalidDamage() {
        assertTrue(SurvivalHealthSupport.isLethal(10, 10));
        assertTrue(SurvivalHealthSupport.isLethal(10, 11));
        assertFalse(SurvivalHealthSupport.isLethal(10, 9));
        for (double damage : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertFalse(SurvivalHealthSupport.isLethal(10, damage));
        }
        assertFalse(SurvivalHealthSupport.isLethal(0, 10));
        assertFalse(SurvivalHealthSupport.isLethal(Double.NaN, 10));
    }

    @Test
    void rescuePreservesNormalRecoveryAndNeverExceedsMaxHealth() {
        assertEquals(4.0, SurvivalHealthSupport.rescueHealth(20));
        assertEquals(2.0, SurvivalHealthSupport.rescueHealth(5));
        assertEquals(0.5, SurvivalHealthSupport.rescueHealth(0.5));
        assertEquals(1.0, SurvivalHealthSupport.rescueHealth(1.0));
    }

    @Test
    void invalidMaxHealthCannotRescue() {
        for (double health : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(0.0, SurvivalHealthSupport.rescueHealth(health));
        }
    }

    @Test
    void fortitudeRequiresARealNonlethalHit() {
        assertTrue(SurvivalHealthSupport.survivesBelowThreshold(6, 2, 20, 4));
        assertFalse(SurvivalHealthSupport.survivesBelowThreshold(4, 0, 20, 4));
        assertFalse(SurvivalHealthSupport.survivesBelowThreshold(4, 4, 20, 4));
        assertFalse(SurvivalHealthSupport.survivesBelowThreshold(4, 5, 20, 4));
        assertFalse(SurvivalHealthSupport.survivesBelowThreshold(4, -1, 20, 4));
    }

    @Test
    void fortitudeHonorsHalfHealthCapAndDisabledThreshold() {
        assertTrue(SurvivalHealthSupport.survivesBelowThreshold(12, 2, 20, 100));
        assertFalse(SurvivalHealthSupport.survivesBelowThreshold(12, 1, 20, 100));
        assertFalse(SurvivalHealthSupport.survivesBelowThreshold(2, 1, 20, 0));
        assertFalse(SurvivalHealthSupport.survivesBelowThreshold(2, 1, 20, Double.NaN));
        assertFalse(SurvivalHealthSupport.survivesBelowThreshold(2, Double.NaN, 20, 4));
    }

    @Test
    void antiHealPreservesConfiguredReductionAndClampsFiniteScale() {
        assertEquals(2.0, SurvivalHealthSupport.reducedHealing(4, 0.5));
        assertEquals(0.0, SurvivalHealthSupport.reducedHealing(4, 0));
        assertEquals(0.0, SurvivalHealthSupport.reducedHealing(4, -1));
        assertEquals(4.0, SurvivalHealthSupport.reducedHealing(4, 2));
    }

    @Test
    void invalidAntiHealScaleDoesNotBecomeCompleteHealingSuppression() {
        for (double scale : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(4.0, SurvivalHealthSupport.reducedHealing(4, scale));
        }
        assertEquals(0.0, SurvivalHealthSupport.reducedHealing(0, 0.5));
    }

    @Test
    void healingRetainsDoublePrecisionAboveFloatRange() {
        assertEquals(5.0e39, SurvivalHealthSupport.reducedHealing(1.0e40, 0.5));
        assertEquals(0.0617283945061725, SurvivalHealthSupport.reducedHealing(0.123456789012345, 0.5));
    }
}
