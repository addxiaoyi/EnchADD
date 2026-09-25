package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DefenseEffectRulesTest {
    @Test
    void pivotSpeedStaysAtOneAndTwoSecondsEvenWithUnsafeLevels() {
        assertEquals(20, DefenseEffectRules.seconds(1, 2, 1) * 20);
        assertEquals(40, DefenseEffectRules.seconds(2, 2, 1) * 20);
        assertEquals(40, DefenseEffectRules.seconds(Integer.MAX_VALUE, 2, 1) * 20);
        assertEquals(20, DefenseEffectRules.seconds(2, 1, 1) * 20);
        assertEquals(0, DefenseEffectRules.seconds(2, 2, 0));
    }

    @Test
    void pivotCannotShortenOrReplaceStrongerExistingSpeed() {
        int ticks = DefenseEffectRules.seconds(2, 2, 1) * 20;
        assertFalse(ActiveBuffSupport.canUpgrade(1, 10, 0, ticks));
        assertFalse(ActiveBuffSupport.canUpgrade(0, 80, 0, ticks));
        assertFalse(ActiveBuffSupport.canUpgrade(0, ticks, 0, ticks));
        assertFalse(ActiveBuffSupport.canUpgrade(0, -1, 0, ticks));
        assertTrue(ActiveBuffSupport.canUpgrade(0, ticks - 1, 0, ticks));
    }
    @Test
    void defaultSidestepProgressionIsPreserved() {
        for (int level = 1; level <= 3; level++) {
            assertEquals(Math.min(0.6, level * 0.25), DefenseEffectRules.chance(level, 3, 0.25, 0.65));
            assertEquals(10 * (1 - level * 0.12), DefenseEffectRules.damage(10, level, 3, 0.12), 1e-12);
            assertEquals(level * 2, DefenseEffectRules.seconds(level, 3, 2));
        }
    }

    @Test
    void overlevelEquipmentCannotExceedConfiguredLimits() {
        assertEquals(0.25, DefenseEffectRules.chance(Integer.MAX_VALUE, 1, 0.25, 0.65));
        assertEquals(8.8, DefenseEffectRules.damage(10, Integer.MAX_VALUE, 1, 0.12), 1e-12);
        assertEquals(6, DefenseEffectRules.seconds(Integer.MAX_VALUE, 3, 2));
    }

    @Test
    void disabledDurationsAndLevelsProduceNoEffect() {
        assertEquals(0, DefenseEffectRules.seconds(3, 3, 0));
        assertEquals(0, DefenseEffectRules.seconds(3, 3, -1));
        assertEquals(0, DefenseEffectRules.seconds(0, 3, 2));
        assertEquals(0, DefenseEffectRules.seconds(3, 0, 2));
        assertEquals(10, DefenseEffectRules.damage(10, 0, 3, 0.12));
        assertEquals(10, DefenseEffectRules.damage(10, 3, 0, 0.12));
        assertEquals(0, DefenseEffectRules.chance(3, 0, 0.25, 0.65));
    }

    @Test
    void invalidConfigurationCannotCorruptDamageOrChance() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(10, DefenseEffectRules.damage(10, 3, 3, invalid));
            assertEquals(0, DefenseEffectRules.chance(3, 3, invalid, 0.65));
            assertEquals(0, DefenseEffectRules.chance(3, 3, 0.25, invalid));
        }
    }

    @Test
    void extremeFiniteConfigurationKeepsExistingSafetyCaps() {
        assertEquals(2, DefenseEffectRules.damage(10, 3, 3, Double.MAX_VALUE), 1e-12);
        assertEquals(0.6, DefenseEffectRules.chance(3, 3, Double.MAX_VALUE, 1));
        assertEquals(0.2, DefenseEffectRules.chance(3, 3, Double.MAX_VALUE, 0.2));
        assertEquals(120, DefenseEffectRules.seconds(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    void invalidBaseDamageIsNotTurnedIntoAnotherValue() {
        for (double base : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(base, DefenseEffectRules.damage(base, 3, 3, 0.12));
        }
    }
}
