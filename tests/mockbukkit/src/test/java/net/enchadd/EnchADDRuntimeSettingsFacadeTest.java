package net.enchadd;

import net.enchadd.config.EnchADDRuntimeSettingsFacade;
import net.enchadd.config.RuntimeConfigLoader;
import net.enchadd.config.RuntimeConfigState;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchADDRuntimeSettingsFacadeTest {

    @Test
    void delegatesRuntimeSnapshotValuesAndDebugWriter() {
        AtomicReference<RuntimeConfigLoader.RuntimeConfigSnapshot> snapshotRef =
                new AtomicReference<>(RuntimeConfigLoader.RuntimeConfigSnapshot.defaults());
        AtomicBoolean debugFlag = new AtomicBoolean(false);

        EnchADDRuntimeSettingsFacade facade = new EnchADDRuntimeSettingsFacade(
                new RuntimeConfigState(),
                snapshotRef::get,
                snapshotRef::set,
                debugFlag::set
        );

        RuntimeConfigLoader.RuntimeConfigSnapshot custom = new RuntimeConfigLoader.RuntimeConfigSnapshot(
                true, 30, 19.2, 0.9, 12345L, 0.2, 77,
                false, false, 7, 99, 8, 0.5, 3, false,
                7_654_321L, 0.42, 4, false, 6, 88, true
        );
        facade.apply(new net.enchadd.config.RuntimeSettingsLoader.RuntimeSettings("en", true, custom));

        assertEquals("en", facade.getLanguage());
        assertTrue(facade.isMonitoringEnabled());
        assertEquals(30, facade.getMonitoringSampleIntervalSeconds());
        assertEquals(19.2, facade.getMonitoringMinTps());
        assertEquals(0.9, facade.getMonitoringMaxErrorsPerMinute());
        assertEquals(12345L, facade.getMonitoringMaxTriggerRatePerMinute());
        assertEquals(0.2, facade.getMonitoringMaxParticleWindowDropRate());
        assertEquals(77, facade.getMonitoringMaxStatsPending());

        assertFalse(facade.isSafetyModeEnabled());
        assertFalse(facade.isSafetyModeAutoOnAlert());
        assertEquals(7, facade.getSafetyModeAlertBurstThreshold());
        assertEquals(99, facade.getSafetyModeAlertWindowSeconds());
        assertEquals(8, facade.getSafetyModeAutoRecoverStableSamples());
        assertEquals(0.5, facade.getSafetyModeChanceMultiplier());
        assertEquals(3, facade.getSafetyModeTickModuloMultiplier());
        assertFalse(facade.isSafetyModeSuppressParticles());

        assertEquals(7_654_321L, facade.getEnchantBudgetNanosPerTick());
        assertEquals(0.42, facade.getEnchantBudgetDegradedChanceMultiplier());
        assertEquals(4, facade.getEnchantBudgetDegradedTickModuloMultiplier());
        assertFalse(facade.isEnchantBudgetSuppressParticles());
        assertEquals(6, facade.getEnchantBudgetBreakerConsecutiveOverruns());
        assertEquals(88, facade.getEnchantBudgetBreakerCooldownTicks());
        assertTrue(facade.isEnchantBudgetSkipExecutionOnBreaker());

        assertTrue(debugFlag.get());
    }
}
