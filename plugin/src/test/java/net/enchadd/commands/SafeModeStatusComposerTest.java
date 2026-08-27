package net.enchadd.commands;

import net.enchadd.utils.SafetyModeManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeModeStatusComposerTest {

    @AfterEach
    void tearDown() {
        SafetyModeManager.shutdown();
    }

    @Test
    void composeIncludesStableFieldsAndSanitizesReason() {
        SafetyModeManager.shutdown();
        SafetyModeManager.setManualEnabled(true, "manual reason with spaces");

        String status = SafeModeStatusComposer.compose();

        assertTrue(status.startsWith("[ENCHADD-SAFEMODE]"));
        assertTrue(status.contains("state="));
        assertTrue(status.contains("enabled=true"));
        assertTrue(status.contains("manual=true"));
        assertTrue(status.contains("auto=false"));
        assertTrue(status.contains("alertBurst="));
        assertTrue(status.contains("healthyStreak="));
        assertTrue(status.contains("chanceMultiplier="));
        assertTrue(status.contains("tickModuloMultiplier="));
        assertTrue(status.contains("suppressParticles="));
        assertTrue(status.contains("lastReason=manual_reason_with_spaces"));
        assertFalse(status.contains("manual reason with spaces"));
    }
}
