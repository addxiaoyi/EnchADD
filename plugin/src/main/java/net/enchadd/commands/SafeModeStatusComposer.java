package net.enchadd.commands;

import net.enchadd.utils.SafetyModeManager;

import java.util.Locale;

final class SafeModeStatusComposer {

    private SafeModeStatusComposer() {
    }

    static String compose() {
        return String.format(
                Locale.ROOT,
                "[ENCHADD-SAFEMODE] state=%s enabled=%s manual=%s auto=%s alertBurst=%d healthyStreak=%d chanceMultiplier=%.2f tickModuloMultiplier=%d suppressParticles=%s lastReason=%s",
                SafetyModeManager.getStateLabel(),
                SafetyModeManager.isEnabled(),
                SafetyModeManager.isManualEnabled(),
                SafetyModeManager.isAutoEnabled(),
                SafetyModeManager.getCurrentAlertBurstCount(),
                SafetyModeManager.getHealthySampleStreak(),
                SafetyModeManager.getChanceMultiplier(),
                SafetyModeManager.getTickModuloMultiplier(),
                SafetyModeManager.shouldSuppressParticles(),
                EnchantListAdminIdentitySupport.sanitizeMetricValue(SafetyModeManager.getLastReason())
        );
    }
}
