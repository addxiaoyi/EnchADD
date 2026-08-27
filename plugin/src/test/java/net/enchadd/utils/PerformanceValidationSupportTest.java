package net.enchadd.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class PerformanceValidationSupportTest {

    @AfterEach
    void tearDown() {
        SafetyModeManager.shutdown();
    }

    @Test
    void safeAccessorsAndValidityRejectNulls() {
        assertNull(PerformanceValidationSupport.getEquipmentSafe(null));
        assertNull(PerformanceValidationSupport.getPDCSafe(null));
        assertFalse(PerformanceValidationSupport.isPlayerValid(null));
        assertFalse(PerformanceValidationSupport.isEntityValid(null));
    }

    @Test
    void shouldTickRejectsNullEntity() {
        assertFalse(PerformanceValidationSupport.shouldTick(null, 1));
    }
}
