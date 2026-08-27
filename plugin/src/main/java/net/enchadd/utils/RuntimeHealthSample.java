package net.enchadd.utils;

import org.bukkit.plugin.java.JavaPlugin;

record RuntimeHealthSample(
        double tps1m,
        double tps5m,
        double tps15m,
        double errorRatePerMinute,
        long triggerRatePerMinute
) {

    static RuntimeHealthSample capture(JavaPlugin plugin, RuntimeHealthMonitorState state, long intervalTicks) {
        double[] tps = plugin.getServer().getTPS();
        double tps1m = sanitizeTps(tps, 0);
        double tps5m = sanitizeTps(tps, 1);
        double tps15m = sanitizeTps(tps, 2);
        state.updateTps(tps1m, tps5m, tps15m);

        double intervalMinutes = Math.max(1.0 / 60.0, intervalTicks / (20.0 * 60.0));

        long triggerTotal = EnchantStats.getTotalCount();
        long previousTriggerTotal = state.takePreviousTriggerTotal(triggerTotal);
        long triggerDelta = Math.max(0L, triggerTotal - previousTriggerTotal);
        long triggerRatePerMinute = Math.round(triggerDelta / intervalMinutes);

        long errorTotal = RuntimeErrorTracker.getTotalErrors();
        long previousErrorTotal = state.takePreviousErrorTotal(errorTotal);
        long errorDelta = Math.max(0L, errorTotal - previousErrorTotal);
        double errorRatePerMinute = errorDelta / intervalMinutes;

        state.updateRates(errorRatePerMinute, triggerRatePerMinute);
        return new RuntimeHealthSample(tps1m, tps5m, tps15m, errorRatePerMinute, triggerRatePerMinute);
    }

    static double sanitizeTps(double[] tps, int index) {
        if (tps == null || tps.length <= index) {
            return 20.0;
        }
        double value = tps[index];
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 20.0;
        }
        return Math.max(0.0, Math.min(20.0, value));
    }
}
