package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import net.enchadd.legacy.LegacyEnchantStats;
import net.enchadd.utils.EnchantExecutionBudgetManager;
import net.enchadd.utils.EnchantStats;
import net.enchadd.utils.LangManager;
import net.enchadd.utils.ParticleQueue;
import net.enchadd.utils.RuntimeErrorTracker;
import net.enchadd.utils.RuntimeHealthMonitor;
import net.enchadd.utils.SafetyModeManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.management.ManagementFactory;
import java.util.function.Function;

record CiStatusSnapshot(
        String status,
        int enchants,
        boolean hasAirbag,
        String version,
        String activeLang,
        boolean translationReady,
        String airbagName,
        String braceName,
        String airbagCodepoints,
        String braceCodepoints,
        long totalTriggers,
        long triggerRatePerMin,
        int particleDropped,
        long particleSubmitted,
        long particleSuppressed,
        double particleDropRate,
        long particleDroppedUnits,
        long particleSubmittedUnits,
        long particleSuppressedUnits,
        double particleUnitDropRate,
        double particleWindowDropRate,
        double particlePeakWindowDropRate,
        int particleQueueSize,
        int particlePeakQueueSize,
        int particleMaxPerTick,
        int statsPending,
        long statsDropped,
        long runtimeErrors,
        long monitorSamples,
        long monitorAlerts,
        double errorRatePerMin,
        double tps1m,
        double tps5m,
        double tps15m,
        String safetyModeState,
        boolean safetyModeEnabled,
        boolean safetyModeManual,
        boolean safetyModeAuto,
        int safetyModeAlertBurst,
        int safetyModeHealthyStreak,
        double safetyModeChanceMultiplier,
        int safetyModeTickModuloMultiplier,
        boolean safetyModeSuppressParticles,
        long safetyModeLastChangeEpochMs,
        String safetyModeReason,
        long budgetDegradedExecutions,
        long budgetSkippedExecutions,
        int budgetOpenBreakers,
        long budgetTrackedEnchants,
        long budgetNanosPerTick,
        double budgetChanceMultiplier,
        int budgetTickModuloMultiplier,
        boolean budgetSuppressParticles,
        long legacySanitizedTotal,
        String legacySanitizedKeys,
        long heapUsedMb,
        long heapCommittedMb,
        long heapMaxMb,
        long uptimeSeconds
) {
    static CiStatusSnapshot capture(JavaPlugin plugin,
                                    Function<String, String> resolveCiEnchantName,
                                    Function<String, Boolean> translationReadyForVerify) {
        boolean hasEnchants = !EnchADDConfig.ENCHANTS.isEmpty();
        boolean hasAirbag = EnchADDConfig.ENCHANTS.containsKey(net.kyori.adventure.key.Key.key("enchadd:airbag"));
        String activeLang = LangManager.getActiveLang();
        String airbagName = resolveCiEnchantName.apply("airbag");
        String braceName = resolveCiEnchantName.apply("brace");
        String airbagCodepoints = CiStatusMetricFormatter.encodeMetricCodepoints(airbagName);
        String braceCodepoints = CiStatusMetricFormatter.encodeMetricCodepoints(braceName);
        boolean translationReady = translationReadyForVerify.apply("airbag") && translationReadyForVerify.apply("brace");
        Runtime runtime = Runtime.getRuntime();
        return new CiStatusSnapshot(
                hasEnchants && hasAirbag ? "OK" : "FAIL",
                EnchADDConfig.ENCHANTS.size(),
                hasAirbag,
                plugin.getPluginMeta().getVersion(),
                activeLang,
                translationReady,
                airbagName,
                braceName,
                airbagCodepoints,
                braceCodepoints,
                EnchantStats.getTotalCount(),
                RuntimeHealthMonitor.getLastTriggerRatePerMinute(),
                ParticleQueue.getDroppedCount(),
                ParticleQueue.getSubmittedCount(),
                ParticleQueue.getSuppressedCount(),
                ParticleQueue.getCumulativeDropRate(),
                ParticleQueue.getDroppedParticleUnits(),
                ParticleQueue.getSubmittedParticleUnits(),
                ParticleQueue.getSuppressedParticleUnits(),
                ParticleQueue.getCumulativeUnitDropRate(),
                ParticleQueue.getLastWindowDropRate(),
                ParticleQueue.getMaxWindowDropRate(),
                ParticleQueue.getQueueSize(),
                ParticleQueue.getPeakQueueSize(),
                ParticleQueue.getCurrentMaxPerTick(),
                EnchantStats.getPendingWriteCount(),
                EnchantStats.getDroppedSnapshotCount(),
                RuntimeErrorTracker.getTotalErrors(),
                RuntimeHealthMonitor.getSampleCount(),
                RuntimeHealthMonitor.getAlertCount(),
                RuntimeHealthMonitor.getLastErrorRatePerMinute(),
                RuntimeHealthMonitor.getLastTps1m(),
                RuntimeHealthMonitor.getLastTps5m(),
                RuntimeHealthMonitor.getLastTps15m(),
                SafetyModeManager.getStateLabel(),
                SafetyModeManager.isEnabled(),
                SafetyModeManager.isManualEnabled(),
                SafetyModeManager.isAutoEnabled(),
                SafetyModeManager.getCurrentAlertBurstCount(),
                SafetyModeManager.getHealthySampleStreak(),
                SafetyModeManager.getChanceMultiplier(),
                SafetyModeManager.getTickModuloMultiplier(),
                SafetyModeManager.shouldSuppressParticles(),
                SafetyModeManager.getLastStateChangeEpochMillis(),
                SafetyModeManager.getLastReason(),
                EnchantExecutionBudgetManager.getDegradedExecutionCount(),
                EnchantExecutionBudgetManager.getSkippedExecutionCount(),
                EnchantExecutionBudgetManager.getOpenBreakerCount(),
                EnchantExecutionBudgetManager.getTrackedEnchantCount(),
                EnchADDConfig.getEnchantBudgetNanosPerTick(),
                EnchADDConfig.getEnchantBudgetDegradedChanceMultiplier(),
                EnchADDConfig.getEnchantBudgetDegradedTickModuloMultiplier(),
                EnchADDConfig.isEnchantBudgetSuppressParticles(),
                LegacyEnchantStats.getTotalSanitized(),
                CiStatusLegacySupport.summarizeLegacyKeys(LegacyEnchantStats.snapshotByKey()),
                (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L),
                runtime.totalMemory() / (1024L * 1024L),
                runtime.maxMemory() / (1024L * 1024L),
                ManagementFactory.getRuntimeMXBean().getUptime() / 1000L
        );
    }
}
