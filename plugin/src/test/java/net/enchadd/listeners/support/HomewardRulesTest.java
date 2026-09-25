package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HomewardRulesTest {
    @Test
    void defaultProgressionKeepsItsSpeedAndFallProtection() {
        for (int level = 1; level <= 3; level++) {
            assertEquals(level, HomewardRules.speedSeconds(level, 3, 1));
            double reduction = HomewardRules.fallReduction(level, 3, 0.20, 0.60);
            assertEquals(level * 0.20, reduction, 1.0e-12);
            assertEquals(20 * (1 - level * 0.20), HomewardRules.reducedFallDamage(20, 12, reduction), 1.0e-12);
        }
        assertEquals(60, HomewardRules.windowTicks(60));
    }

    @Test
    void commandsAndStoredLevelsCannotExceedConfiguredMaximum() {
        assertEquals(3, HomewardRules.effectiveLevel(Integer.MAX_VALUE, 3));
        assertEquals(3, HomewardRules.speedSeconds(Integer.MAX_VALUE, 3, 1));
        assertEquals(0.60, HomewardRules.fallReduction(Integer.MAX_VALUE, 3, 0.20, 0.60));
        assertEquals(1, HomewardRules.speedSeconds(3, 1, 1));
        assertEquals(0.20, HomewardRules.fallReduction(3, 1, 0.20, 0.60));
    }

    @Test
    void disabledLevelsAndDurationsDoNotCreateMinimumEffects() {
        for (int invalid : new int[]{0, -1, Integer.MIN_VALUE}) {
            assertEquals(0, HomewardRules.effectiveLevel(invalid, 3));
            assertEquals(0, HomewardRules.effectiveLevel(3, invalid));
            assertEquals(0, HomewardRules.speedSeconds(invalid, 3, 1));
            assertEquals(0, HomewardRules.speedSeconds(3, invalid, 1));
            assertEquals(0, HomewardRules.speedSeconds(3, 3, invalid));
            assertEquals(0, HomewardRules.windowTicks(invalid));
            assertEquals(0, HomewardRules.fallReduction(invalid, 3, 0.20, 0.60));
            assertEquals(0, HomewardRules.fallReduction(3, invalid, 0.20, 0.60));
        }
    }

    @Test
    void extremeConfigurationsStayFiniteAndBounded() {
        assertEquals(120, HomewardRules.speedSeconds(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(2400, HomewardRules.windowTicks(Integer.MAX_VALUE));
        double reduction = HomewardRules.fallReduction(Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        assertEquals(0.90, reduction);
        assertEquals(2, HomewardRules.reducedFallDamage(20, 20, reduction), 1.0e-12);
        double remaining = HomewardRules.reducedFallDamage(Double.MAX_VALUE, Double.MAX_VALUE, reduction);
        assertTrue(Double.isFinite(remaining) && remaining > 0 && remaining < Double.MAX_VALUE);
    }

    @Test
    void invalidOrNegligibleReductionDoesNotArmProtection() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MIN_VALUE}) {
            assertEquals(0, HomewardRules.fallReduction(3, 3, invalid, 0.60));
            assertEquals(0, HomewardRules.fallReduction(3, 3, 0.20, invalid));
        }
    }

    @Test
    void cancelledAndAbsorbedHitsCannotTriggerOrConsumeProtection() {
        assertFalse(HomewardRules.hasDamage(true, 20, 12));
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertFalse(HomewardRules.hasDamage(false, invalid, 12));
            assertFalse(HomewardRules.hasDamage(false, 20, invalid));
            assertEquals(20, HomewardRules.reducedFallDamage(20, invalid, 0.60));
            assertEquals(invalid, HomewardRules.reducedFallDamage(invalid, 12, 0.60));
        }
        assertTrue(HomewardRules.hasDamage(false, 20, 12));
    }

    @Test
    void fallReductionScalesBaseDamageOnceAndPreservesNoOps() {
        assertEquals(10, HomewardRules.reducedFallDamage(20, 8, 0.50));
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MIN_VALUE}) {
            assertEquals(20, HomewardRules.reducedFallDamage(20, 8, invalid));
        }
    }

    @Test
    void weakerLoadoutsCannotRefreshAnActiveStrongerWindow() {
        assertEquals(3, HomewardRules.windowLevel(1, 3, true, 3));
        assertFalse(HomewardRules.canRefreshWindow(1, 3, true, 3));
        assertTrue(HomewardRules.canRefreshWindow(3, 3, true, 3));
        assertTrue(HomewardRules.canRefreshWindow(3, 1, true, 3));
    }

    @Test
    void expiredOrInvalidStoredLevelsCannotCarryStrongerProtectionForward() {
        assertEquals(1, HomewardRules.windowLevel(1, 3, false, 3));
        assertTrue(HomewardRules.canRefreshWindow(1, 3, false, 3));
        assertEquals(2, HomewardRules.windowLevel(2, Integer.MIN_VALUE, true, 3));
        assertEquals(3, HomewardRules.windowLevel(2, Integer.MAX_VALUE, true, 3));
        assertEquals(1, HomewardRules.windowLevel(3, Integer.MAX_VALUE, true, 1));
        assertFalse(HomewardRules.canRefreshWindow(0, 3, false, 3));
        assertFalse(HomewardRules.canRefreshWindow(3, 3, false, 0));
    }

    @Test
    void speedRefreshPreservesStrongerLongerAndInfiniteEffects() {
        int ticks = HomewardRules.speedSeconds(3, 3, 1) * 20;
        assertFalse(ActiveBuffSupport.canUpgrade(1, 20, 0, ticks));
        assertFalse(ActiveBuffSupport.canUpgrade(0, 100, 0, ticks));
        assertFalse(ActiveBuffSupport.canUpgrade(0, -1, 0, ticks));
        assertTrue(ActiveBuffSupport.canUpgrade(0, 20, 0, ticks));
    }
}
