package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThirstRulesTest {
    @Test
    void onlySuccessfulDamagingHitsCountAsCombat() {
        assertTrue(ThirstRules.isCombatHit(false, 1));
        assertFalse(ThirstRules.isCombatHit(true, 1));
        for (double damage : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertFalse(ThirstRules.isCombatHit(false, damage));
        }
    }

    @Test
    void defaultLevelsKeepHealingAndHungerProgression() {
        for (int level = 1; level <= 3; level++) {
            assertEquals(10 * (1 - 0.3 * level), ThirstRules.healing(10, level, 3, 0.3), 1e-12);
            assertEquals(19 - level, ThirstRules.food(20, 19, level, 3, 1));
        }
    }

    @Test
    void UnsafeLevelsCannotIncreasePenaltiesBeyondConfiguredMaximum() {
        assertEquals(3, ThirstRules.effectiveLevel(Integer.MAX_VALUE, 3));
        assertEquals(7, ThirstRules.healing(10, Integer.MAX_VALUE, 1, 0.3), 1e-12);
        assertEquals(18, ThirstRules.food(20, 19, Integer.MAX_VALUE, 1, 1));
        assertEquals(0, ThirstRules.effectiveLevel(-1, 3));
        assertEquals(0, ThirstRules.effectiveLevel(3, -1));
    }

    @Test
    void invalidHealingAndConfigurationAreNotForwardedAsChangedAmounts() {
        for (double amount : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(amount, ThirstRules.healing(amount, 3, 3, 0.3));
        }
        for (double rate : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(10, ThirstRules.healing(10, 3, 3, rate));
        }
    }

    @Test
    void largeFinitePenaltiesRemainBoundedWithoutIntegerOverflow() {
        assertEquals(1, ThirstRules.healing(10, Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE), 1e-12);
        assertEquals(15, ThirstRules.food(20, 19, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(0, ThirstRules.food(2, 1, 3, 3, 1));
    }

    @Test
    void eatingOrUnchangedHungerDoesNotIncurExtraLoss() {
        assertEquals(15, ThirstRules.food(10, 15, 3, 3, 1));
        assertEquals(10, ThirstRules.food(10, 10, 3, 3, 1));
        assertEquals(-1, ThirstRules.food(10, -1, 3, 3, 1));
    }

    @Test
    void disabledLevelsAndHungerSettingsPreserveOriginalValues() {
        assertEquals(10, ThirstRules.healing(10, 0, 3, 0.3));
        assertEquals(10, ThirstRules.healing(10, 3, 0, 0.3));
        assertEquals(19, ThirstRules.food(20, 19, 0, 3, 1));
        assertEquals(19, ThirstRules.food(20, 19, 3, 0, 1));
        assertEquals(19, ThirstRules.food(20, 19, 3, 3, 0));
        assertEquals(19, ThirstRules.food(20, 19, 3, 3, -1));
    }
}
