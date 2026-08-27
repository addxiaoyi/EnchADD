package net.enchadd.utils;

import net.enchadd.EnchADDConfig;

final class RuntimeHealthAlertPolicy {

    private RuntimeHealthAlertPolicy() {
    }

    static RuntimeHealthAlertDecision evaluate(RuntimeHealthSample sample) {
        boolean lowTps = sample.tps1m() < EnchADDConfig.getMonitoringMinTps();
        boolean highErrorRate = sample.errorRatePerMinute() > EnchADDConfig.getMonitoringMaxErrorsPerMinute();
        boolean triggerSpike = sample.triggerRatePerMinute() > EnchADDConfig.getMonitoringMaxTriggerRatePerMinute();
        boolean particleDropHigh = ParticleQueue.getLastWindowDropRate() > EnchADDConfig.getMonitoringMaxParticleWindowDropRate();
        boolean statsPendingHigh = EnchantStats.getPendingWriteCount() > EnchADDConfig.getMonitoringMaxStatsPending();
        boolean alertTriggered = lowTps || highErrorRate || triggerSpike || particleDropHigh || statsPendingHigh;
        return new RuntimeHealthAlertDecision(
                alertTriggered,
                buildAlertReason(lowTps, highErrorRate, triggerSpike, particleDropHigh, statsPendingHigh)
        );
    }

    static String buildAlertReason(boolean lowTps,
                                   boolean highErrorRate,
                                   boolean triggerSpike,
                                   boolean particleDropHigh,
                                   boolean statsPendingHigh) {
        StringBuilder reason = new StringBuilder();
        if (lowTps) {
            reason.append("low_tps");
        }
        if (highErrorRate) {
            appendReason(reason, "error_rate");
        }
        if (triggerSpike) {
            appendReason(reason, "trigger_spike");
        }
        if (particleDropHigh) {
            appendReason(reason, "particle_drop");
        }
        if (statsPendingHigh) {
            appendReason(reason, "stats_pending");
        }
        if (reason.isEmpty()) {
            return "none";
        }
        return reason.toString();
    }

    private static void appendReason(StringBuilder builder, String value) {
        if (!builder.isEmpty()) {
            builder.append('+');
        }
        builder.append(value);
    }
}
