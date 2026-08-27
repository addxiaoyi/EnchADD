package net.enchadd.commands;

import net.enchadd.utils.SafetyModeManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeModeActionSupportTest {

    @AfterEach
    void tearDown() {
        SafetyModeManager.shutdown();
    }

    @Test
    void actionDefaultsToToggleAndLowercasesProvidedAction() {
        assertEquals("toggle", SafeModeActionSupport.action(new String[] {"safemode"}));
        assertEquals("status", SafeModeActionSupport.action(new String[] {"safemode", "STATUS"}));
    }

    @Test
    void applyUpdatesManualStateForSupportedActions() {
        SafetyModeManager.shutdown();

        assertTrue(SafeModeActionSupport.apply(new String[] {"safemode", "on"}));
        assertTrue(SafetyModeManager.isManualEnabled());

        assertTrue(SafeModeActionSupport.apply(new String[] {"safemode", "off"}));
        assertFalse(SafetyModeManager.isManualEnabled());

        assertTrue(SafeModeActionSupport.apply(new String[] {"safemode", "toggle"}));
        assertTrue(SafetyModeManager.isManualEnabled());
    }

    @Test
    void applyStatusDoesNotChangeManualStateAndUnknownActionFails() {
        SafetyModeManager.shutdown();
        SafetyModeManager.setManualEnabled(true, "test_setup");

        assertTrue(SafeModeActionSupport.apply(new String[] {"safemode", "status"}));
        assertTrue(SafetyModeManager.isManualEnabled());

        assertFalse(SafeModeActionSupport.apply(new String[] {"safemode", "invalid"}));
        assertTrue(SafetyModeManager.isManualEnabled());
    }
}
