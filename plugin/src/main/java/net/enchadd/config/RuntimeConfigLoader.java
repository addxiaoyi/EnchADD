package net.enchadd.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public final class RuntimeConfigLoader {

    private RuntimeConfigLoader() {
    }

    public record RuntimeConfigSnapshot(
            boolean monitoringEnabled,
            int monitoringSampleIntervalSeconds,
            double monitoringMinTps,
            double monitoringMaxErrorsPerMinute,
            long monitoringMaxTriggerRatePerMinute,
            double monitoringMaxParticleWindowDropRate,
            int monitoringMaxStatsPending,
            boolean safetyModeEnabled,
            boolean safetyModeAutoOnAlert,
            int safetyModeAlertBurstThreshold,
            int safetyModeAlertWindowSeconds,
            int safetyModeAutoRecoverStableSamples,
            double safetyModeChanceMultiplier,
            int safetyModeTickModuloMultiplier,
            boolean safetyModeSuppressParticles,
            long enchantBudgetNanosPerTick,
            double enchantBudgetDegradedChanceMultiplier,
            int enchantBudgetDegradedTickModuloMultiplier,
            boolean enchantBudgetSuppressParticles,
            int enchantBudgetBreakerConsecutiveOverruns,
            int enchantBudgetBreakerCooldownTicks,
            boolean enchantBudgetSkipExecutionOnBreaker
    ) {
        public static RuntimeConfigSnapshot defaults() {
            return new RuntimeConfigSnapshot(
                    true, 20, 18.0, 0.50, 60000L, 0.10, 32,
                    true, true, 3, 60, 6, 0.60, 2, true,
                    1_200_000L, 0.65, 2, true, 4, 80, false
            );
        }
    }

    public static RuntimeConfigSnapshot parse(@NotNull FileConfiguration configuration) {
        ConfigurationSection particleQueueSection = ConfigSupport.getConfigSection(configuration, "particle-queue");
        int particleMax = ConfigSupport.getInt(particleQueueSection, "max-per-tick", 100);
        ConfigSupport.getInt(particleQueueSection, "min-per-tick", Math.max(1, particleMax / 2));
        ConfigSupport.getInt(particleQueueSection, "max-dynamic-per-tick", Math.max(particleMax, particleMax * 4));
        ConfigSupport.getDouble(particleQueueSection, "warn-drop-rate", 0.05);

        ConfigurationSection monitoringSection = ConfigSupport.getConfigSection(configuration, "runtime-monitoring");
        boolean monitoringEnabled = ConfigSupport.getBoolean(monitoringSection, "enabled", true);
        int monitoringSampleIntervalSeconds = Math.max(5, ConfigSupport.getInt(monitoringSection, "sample-interval-seconds", 20));
        double monitoringMinTps = ConfigSupport.clamp(ConfigSupport.getDouble(monitoringSection, "min-tps", 18.0), 1.0, 20.0);
        double monitoringMaxErrorsPerMinute = Math.max(0.0, ConfigSupport.getDouble(monitoringSection, "max-errors-per-minute", 0.5));
        long monitoringMaxTriggerRatePerMinute = Math.max(1L, ConfigSupport.getLong(monitoringSection, "max-trigger-rate-per-minute", 60000L));
        double monitoringMaxParticleWindowDropRate = ConfigSupport.clamp(ConfigSupport.getDouble(monitoringSection, "max-particle-window-drop-rate", 0.10), 0.0, 1.0);
        int monitoringMaxStatsPending = Math.max(1, ConfigSupport.getInt(monitoringSection, "max-stats-pending", 32));

        ConfigurationSection safetyModeSection = ConfigSupport.getConfigSection(configuration, "safety-mode");
        boolean safetyModeEnabled = ConfigSupport.getBoolean(safetyModeSection, "enabled", true);
        boolean safetyModeAutoOnAlert = ConfigSupport.getBoolean(safetyModeSection, "auto-on-alert", true);
        int safetyModeAlertBurstThreshold = Math.max(1, ConfigSupport.getInt(safetyModeSection, "alert-burst-threshold", 3));
        int safetyModeAlertWindowSeconds = Math.max(5, ConfigSupport.getInt(safetyModeSection, "alert-window-seconds", 60));
        int safetyModeAutoRecoverStableSamples = Math.max(1, ConfigSupport.getInt(safetyModeSection, "auto-recover-stable-samples", 6));
        double safetyModeChanceMultiplier = ConfigSupport.clamp(ConfigSupport.getDouble(safetyModeSection, "chance-multiplier", 0.60), 0.0, 1.0);
        int safetyModeTickModuloMultiplier = Math.max(1, ConfigSupport.getInt(safetyModeSection, "tick-modulo-multiplier", 2));
        boolean safetyModeSuppressParticles = ConfigSupport.getBoolean(safetyModeSection, "suppress-particles", true);

        ConfigurationSection budgetSection = ConfigSupport.getConfigSection(configuration, "enchant-budget");
        long budgetNanosPerTick = Math.max(100_000L, ConfigSupport.getLong(budgetSection, "nanos-per-tick", 1_200_000L));
        double budgetChanceMultiplier = ConfigSupport.clamp(ConfigSupport.getDouble(budgetSection, "degraded-chance-multiplier", 0.65), 0.0, 1.0);
        int budgetTickModuloMultiplier = Math.max(1, ConfigSupport.getInt(budgetSection, "degraded-tick-modulo-multiplier", 2));
        boolean budgetSuppressParticles = ConfigSupport.getBoolean(budgetSection, "suppress-particles", true);
        int budgetBreakerConsecutiveOverruns = Math.max(1, ConfigSupport.getInt(budgetSection, "breaker-consecutive-overruns", 4));
        int budgetBreakerCooldownTicks = Math.max(1, ConfigSupport.getInt(budgetSection, "breaker-cooldown-ticks", 80));
        boolean budgetSkipExecutionOnBreaker = ConfigSupport.getBoolean(budgetSection, "breaker-skip-execution", false);

        ConfigurationSection updateCheckerSection = ConfigSupport.getConfigSection(configuration, "update-checker");
        ConfigSupport.getBoolean(updateCheckerSection, "enabled", false);
        ConfigSupport.getString(updateCheckerSection, "repository", "EnchADD/EnchADD");
        ConfigSupport.getInt(updateCheckerSection, "timeout-seconds", 8);

        return new RuntimeConfigSnapshot(
                monitoringEnabled,
                monitoringSampleIntervalSeconds,
                monitoringMinTps,
                monitoringMaxErrorsPerMinute,
                monitoringMaxTriggerRatePerMinute,
                monitoringMaxParticleWindowDropRate,
                monitoringMaxStatsPending,
                safetyModeEnabled,
                safetyModeAutoOnAlert,
                safetyModeAlertBurstThreshold,
                safetyModeAlertWindowSeconds,
                safetyModeAutoRecoverStableSamples,
                safetyModeChanceMultiplier,
                safetyModeTickModuloMultiplier,
                safetyModeSuppressParticles,
                budgetNanosPerTick,
                budgetChanceMultiplier,
                budgetTickModuloMultiplier,
                budgetSuppressParticles,
                budgetBreakerConsecutiveOverruns,
                budgetBreakerCooldownTicks,
                budgetSkipExecutionOnBreaker
        );
    }

}
