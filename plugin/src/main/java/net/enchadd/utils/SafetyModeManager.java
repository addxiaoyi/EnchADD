package net.enchadd.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Global runtime safety-mode switch.
 */
public final class SafetyModeManager {

    private static final SafetyModeState STATE = new SafetyModeState();

    private SafetyModeManager() {
    }

    public static void initialize(@NotNull JavaPlugin owningPlugin) {
        STATE.initialize(owningPlugin);
    }

    public static void shutdown() {
        STATE.shutdown();
    }

    public static boolean isEnabled() {
        return STATE.isEnabled();
    }

    public static boolean isManualEnabled() {
        return STATE.isManualEnabled();
    }

    public static boolean isAutoEnabled() {
        return STATE.isAutoEnabled();
    }

    public static String getStateLabel() {
        return STATE.getStateLabel();
    }

    public static long getLastStateChangeEpochMillis() {
        return STATE.getLastStateChangeEpochMillis();
    }

    public static String getLastReason() {
        return STATE.getLastReason();
    }

    public static int getCurrentAlertBurstCount() {
        return STATE.getCurrentAlertBurstCount();
    }

    public static int getHealthySampleStreak() {
        return STATE.getHealthySampleStreak();
    }

    public static void setManualEnabled(boolean enabled, @NotNull String reason) {
        STATE.setManualEnabled(enabled, reason);
    }

    public static void onRuntimeSample(boolean alertTriggered, @NotNull String alertReason) {
        STATE.onRuntimeSample(alertTriggered, alertReason);
    }

    public static double getChanceMultiplier() {
        return STATE.getChanceMultiplier();
    }

    public static int getTickModuloMultiplier() {
        return STATE.getTickModuloMultiplier();
    }

    public static boolean shouldSuppressParticles() {
        return STATE.shouldSuppressParticles();
    }
}
