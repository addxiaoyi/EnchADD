package net.enchadd.utils;

import net.enchadd.EnchADDConfig;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Locale;

final class SafetyModeState {

    private static final String DEFAULT_REASON = "init";

    private final ArrayDeque<Long> recentAlertTimestamps = new ArrayDeque<>();

    private volatile JavaPlugin plugin;
    private volatile boolean manualEnabled;
    private volatile boolean autoEnabled;
    private volatile int healthySampleStreak;
    private volatile long lastStateChangeEpochMillis;
    private volatile String lastReason = DEFAULT_REASON;

    synchronized void initialize(@NotNull JavaPlugin owningPlugin) {
        plugin = owningPlugin;
        reset("initialize");
    }

    synchronized void shutdown() {
        reset("shutdown");
        plugin = null;
    }

    boolean isEnabled() {
        return EnchADDConfig.isSafetyModeEnabled() && (manualEnabled || autoEnabled);
    }

    boolean isManualEnabled() {
        return EnchADDConfig.isSafetyModeEnabled() && manualEnabled;
    }

    boolean isAutoEnabled() {
        return EnchADDConfig.isSafetyModeEnabled() && autoEnabled;
    }

    String getStateLabel() {
        return SafetyModePolicySupport.buildStateLabel(
                EnchADDConfig.isSafetyModeEnabled(),
                manualEnabled,
                autoEnabled
        );
    }

    long getLastStateChangeEpochMillis() {
        return lastStateChangeEpochMillis;
    }

    String getLastReason() {
        return lastReason;
    }

    synchronized int getCurrentAlertBurstCount() {
        pruneOldAlerts(System.currentTimeMillis());
        return recentAlertTimestamps.size();
    }

    int getHealthySampleStreak() {
        return healthySampleStreak;
    }

    synchronized void setManualEnabled(boolean enabled, @NotNull String reason) {
        if (!EnchADDConfig.isSafetyModeEnabled()) {
            disableFeatureMode();
            return;
        }
        manualEnabled = enabled;
        lastReason = SafetyModePolicySupport.sanitizeReason(reason);
        lastStateChangeEpochMillis = System.currentTimeMillis();
        if (!enabled) {
            healthySampleStreak = 0;
        }
        logInfo(String.format(
                Locale.ROOT,
                "[ENCHADD-SAFEMODE] manual=%s auto=%s state=%s reason=%s",
                manualEnabled,
                autoEnabled,
                getStateLabel(),
                SafetyModePolicySupport.sanitizeReason(reason)
        ));
    }

    synchronized void onRuntimeSample(boolean alertTriggered, @NotNull String alertReason) {
        if (!EnchADDConfig.isSafetyModeEnabled() || !EnchADDConfig.isSafetyModeAutoOnAlert()) {
            return;
        }
        long now = System.currentTimeMillis();
        pruneOldAlerts(now);

        if (alertTriggered) {
            recentAlertTimestamps.addLast(now);
            pruneOldAlerts(now);
            healthySampleStreak = 0;
            int burstThreshold = Math.max(1, EnchADDConfig.getSafetyModeAlertBurstThreshold());
            if (!autoEnabled && recentAlertTimestamps.size() >= burstThreshold) {
                autoEnabled = true;
                lastReason = SafetyModePolicySupport.sanitizeReason("auto_alert_" + SafetyModePolicySupport.sanitizeReason(alertReason));
                lastStateChangeEpochMillis = now;
                logInfo(String.format(
                        Locale.ROOT,
                        "[ENCHADD-SAFEMODE] auto-enabled burst=%d threshold=%d windowSec=%d reason=%s",
                        recentAlertTimestamps.size(),
                        burstThreshold,
                        EnchADDConfig.getSafetyModeAlertWindowSeconds(),
                        lastReason
                ));
            }
            return;
        }

        if (!autoEnabled) {
            return;
        }

        healthySampleStreak++;
        int stableSamples = Math.max(1, EnchADDConfig.getSafetyModeAutoRecoverStableSamples());
        if (healthySampleStreak < stableSamples) {
            return;
        }

        autoEnabled = false;
        healthySampleStreak = 0;
        recentAlertTimestamps.clear();
        lastReason = "auto_recovered";
        lastStateChangeEpochMillis = now;
        logInfo(String.format(
                Locale.ROOT,
                "[ENCHADD-SAFEMODE] auto-disabled stableSamples=%d",
                stableSamples
        ));
    }

    double getChanceMultiplier() {
        if (!isEnabled()) {
            return 1.0;
        }
        return SafetyModePolicySupport.clamp(EnchADDConfig.getSafetyModeChanceMultiplier(), 0.0, 1.0);
    }

    int getTickModuloMultiplier() {
        if (!isEnabled()) {
            return 1;
        }
        return Math.max(1, EnchADDConfig.getSafetyModeTickModuloMultiplier());
    }

    boolean shouldSuppressParticles() {
        return isEnabled() && EnchADDConfig.isSafetyModeSuppressParticles();
    }

    private void reset(String reason) {
        manualEnabled = false;
        autoEnabled = false;
        healthySampleStreak = 0;
        recentAlertTimestamps.clear();
        lastStateChangeEpochMillis = System.currentTimeMillis();
        lastReason = reason;
    }

    private void disableFeatureMode() {
        manualEnabled = false;
        autoEnabled = false;
        healthySampleStreak = 0;
        recentAlertTimestamps.clear();
        lastReason = "feature_disabled";
        lastStateChangeEpochMillis = System.currentTimeMillis();
    }

    private void pruneOldAlerts(long now) {
        long ttlMillis = Math.max(1L, EnchADDConfig.getSafetyModeAlertWindowSeconds()) * 1000L;
        long oldestAllowed = now - ttlMillis;
        while (!recentAlertTimestamps.isEmpty()) {
            long ts = recentAlertTimestamps.peekFirst();
            if (ts >= oldestAllowed) {
                break;
            }
            recentAlertTimestamps.pollFirst();
        }
    }

    private void logInfo(String message) {
        JavaPlugin current = plugin;
        if (current == null) {
            return;
        }
        current.getLogger().warning(message);
    }
}
