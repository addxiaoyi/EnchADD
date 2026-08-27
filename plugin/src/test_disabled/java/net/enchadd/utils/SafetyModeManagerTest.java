package net.enchadd.utils;

import net.enchadd.EnchADDConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafetyModeManagerTest {

    @AfterEach
    void tearDown() {
        SafetyModeManager.shutdown();
    }

    @Test
    void autoModeEnablesOnAlertBurstAndRecoversAfterHealthySamples() {
        Assumptions.assumeTrue(EnchADDConfig.isSafetyModeEnabled(), "safety-mode is disabled in current config");
        Assumptions.assumeTrue(EnchADDConfig.isSafetyModeAutoOnAlert(), "auto-on-alert is disabled in current config");

        SafetyModeManager.shutdown();
        assertFalse(SafetyModeManager.isAutoEnabled());

        int burstThreshold = Math.max(1, EnchADDConfig.getSafetyModeAlertBurstThreshold());
        for (int i = 0; i < burstThreshold; i++) {
            SafetyModeManager.onRuntimeSample(true, "unit_test_alert");
        }
        assertTrue(SafetyModeManager.isAutoEnabled());
        assertTrue(SafetyModeManager.isEnabled());

        int recoverSamples = Math.max(1, EnchADDConfig.getSafetyModeAutoRecoverStableSamples());
        for (int i = 0; i < recoverSamples; i++) {
            SafetyModeManager.onRuntimeSample(false, "unit_test_healthy");
        }
        assertFalse(SafetyModeManager.isAutoEnabled());
        assertFalse(SafetyModeManager.isEnabled());
    }

    @Test
    void multipliersApplyOnlyWhenSafetyModeIsActive() {
        Assumptions.assumeTrue(EnchADDConfig.isSafetyModeEnabled(), "safety-mode is disabled in current config");
        Assumptions.assumeTrue(EnchADDConfig.isSafetyModeAutoOnAlert(), "auto-on-alert is disabled in current config");

        SafetyModeManager.shutdown();
        assertEquals(1.0, SafetyModeManager.getChanceMultiplier(), 1.0e-9);
        assertEquals(1, SafetyModeManager.getTickModuloMultiplier());
        assertFalse(SafetyModeManager.shouldSuppressParticles());

        int burstThreshold = Math.max(1, EnchADDConfig.getSafetyModeAlertBurstThreshold());
        for (int i = 0; i < burstThreshold; i++) {
            SafetyModeManager.onRuntimeSample(true, "unit_test_alert");
        }
        assertTrue(SafetyModeManager.isEnabled());
        assertEquals(EnchADDConfig.getSafetyModeChanceMultiplier(), SafetyModeManager.getChanceMultiplier(), 1.0e-9);
        assertEquals(Math.max(1, EnchADDConfig.getSafetyModeTickModuloMultiplier()), SafetyModeManager.getTickModuloMultiplier());
        assertEquals(EnchADDConfig.isSafetyModeSuppressParticles(), SafetyModeManager.shouldSuppressParticles());
    }
}
