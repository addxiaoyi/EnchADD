package net.enchadd.utils;

import net.enchadd.EnchADDConfig;

import java.util.Locale;

final class RuntimeHealthAlertFormatter {

    private RuntimeHealthAlertFormatter() {
    }

    static String format(RuntimeHealthSample sample, RuntimeHealthAlertDecision decision) {
        return String.format(
                Locale.ROOT,
                "[ENCHADD-ALERT] tps1m=%.2f minTps=%.2f errorRatePerMin=%.2f maxErrorRatePerMin=%.2f triggerRatePerMin=%d maxTriggerRatePerMin=%d particleWindowDropRate=%.4f maxParticleWindowDropRate=%.4f statsPending=%d maxStatsPending=%d reason=%s safetyMode=%s safetyModeEnabled=%s",
                sample.tps1m(),
                EnchADDConfig.getMonitoringMinTps(),
                sample.errorRatePerMinute(),
                EnchADDConfig.getMonitoringMaxErrorsPerMinute(),
                sample.triggerRatePerMinute(),
                EnchADDConfig.getMonitoringMaxTriggerRatePerMinute(),
                ParticleQueue.getLastWindowDropRate(),
                EnchADDConfig.getMonitoringMaxParticleWindowDropRate(),
                EnchantStats.getPendingWriteCount(),
                EnchADDConfig.getMonitoringMaxStatsPending(),
                decision.reason(),
                SafetyModeManager.getStateLabel(),
                SafetyModeManager.isEnabled()
        );
    }
}
