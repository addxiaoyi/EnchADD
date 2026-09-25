package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SprintEffectRulesTest {
    @Test
    void defaultDurationAndSlownessProgressionArePreserved() {
        for (int level = 1; level <= 3; level++) {
            assertEquals(level * 2, SprintEffectRules.seconds(level, 3, 2));
            assertEquals(level - 1, SprintEffectRules.slowAmplifier(0, level, 3));
        }
    }

    @Test
    void overlevelEquipmentCannotExtendDurationOrIncreaseSlowness() {
        assertEquals(6, SprintEffectRules.seconds(Integer.MAX_VALUE, 3, 2));
        assertEquals(0, SprintEffectRules.slowAmplifier(0, Integer.MAX_VALUE, 1));
        assertEquals(1, SprintEffectRules.slowAmplifier(0, Integer.MAX_VALUE, 2));
    }

    @Test
    void disabledSettingsCannotProduceDuration() {
        assertEquals(0, SprintEffectRules.seconds(3, 3, 0));
        assertEquals(0, SprintEffectRules.seconds(3, 3, -1));
        assertEquals(0, SprintEffectRules.seconds(0, 3, 2));
        assertEquals(0, SprintEffectRules.seconds(3, 0, 2));
        assertEquals(0, SprintEffectRules.seconds(-1, 3, 2));
    }

    @Test
    void extremeSettingsCannotOverflowDurationOrAmplifier() {
        assertEquals(120, SprintEffectRules.seconds(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(2, SprintEffectRules.slowAmplifier(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(0, SprintEffectRules.slowAmplifier(Integer.MIN_VALUE, 3, 3));
    }

    @Test
    void strongerOrLongerExistingEffectsArePreserved() {
        assertFalse(ActiveBuffSupport.canUpgrade(2, 20, 0, 120));
        assertFalse(ActiveBuffSupport.canUpgrade(0, -1, 0, 120));
        assertFalse(ActiveBuffSupport.canUpgrade(0, 200, 0, 120));
        assertFalse(ActiveBuffSupport.canUpgrade(0, 120, 0, 120));
        assertTrue(ActiveBuffSupport.canUpgrade(0, 20, 0, 120));
    }
}
