package net.enchadd.utils;

import net.enchadd.EnchADDConfig;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Periodic runtime guardrail sampler for online alerting.
 */
public final class RuntimeHealthMonitor {

    private static final RuntimeHealthMonitorState STATE = new RuntimeHealthMonitorState();
    private static volatile BukkitTask monitorTask;

    private RuntimeHealthMonitor() {
    }

    public static void start(JavaPlugin plugin) {
        stop();
        STATE.reset(EnchantStats.getTotalCount(), RuntimeErrorTracker.getTotalErrors());

        if (!EnchADDConfig.isMonitoringEnabled()) {
            return;
        }

        long intervalTicks = Math.max(20L, EnchADDConfig.getMonitoringSampleIntervalSeconds() * 20L);
        monitorTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> sample(plugin, intervalTicks), intervalTicks, intervalTicks);
    }

    public static void stop() {
        BukkitTask task = monitorTask;
        monitorTask = null;
        if (task != null) {
            task.cancel();
        }
    }

    public static long getSampleCount() {
        return STATE.getSampleCount();
    }

    public static long getAlertCount() {
        return STATE.getAlertCount();
    }

    public static double getLastTps1m() {
        return STATE.getLastTps1m();
    }

    public static double getLastTps5m() {
        return STATE.getLastTps5m();
    }

    public static double getLastTps15m() {
        return STATE.getLastTps15m();
    }

    public static double getLastErrorRatePerMinute() {
        return STATE.getLastErrorRatePerMinute();
    }

    public static long getLastTriggerRatePerMinute() {
        return STATE.getLastTriggerRatePerMinute();
    }

    private static void sample(JavaPlugin plugin, long intervalTicks) {
        RuntimeHealthMonitorSampler.sample(plugin, STATE, intervalTicks);
    }
}
