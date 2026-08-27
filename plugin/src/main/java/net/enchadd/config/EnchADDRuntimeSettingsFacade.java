package net.enchadd.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class EnchADDRuntimeSettingsFacade {

    @FunctionalInterface
    public interface RuntimeSnapshotReader {
        @NotNull RuntimeConfigLoader.RuntimeConfigSnapshot get();
    }

    @FunctionalInterface
    public interface RuntimeSnapshotWriter {
        void set(@NotNull RuntimeConfigLoader.RuntimeConfigSnapshot snapshot);
    }

    @FunctionalInterface
    public interface DebugFlagWriter {
        void set(boolean debugEnabled);
    }

    private final @NotNull RuntimeConfigState runtimeState;
    private final @NotNull RuntimeSnapshotReader runtimeSnapshotReader;
    private final @NotNull RuntimeSnapshotWriter runtimeSnapshotWriter;
    private final @NotNull DebugFlagWriter debugFlagWriter;

    public EnchADDRuntimeSettingsFacade(@NotNull RuntimeConfigState runtimeState,
                                        @NotNull RuntimeSnapshotReader runtimeSnapshotReader,
                                        @NotNull RuntimeSnapshotWriter runtimeSnapshotWriter,
                                        @NotNull DebugFlagWriter debugFlagWriter) {
        this.runtimeState = runtimeState;
        this.runtimeSnapshotReader = runtimeSnapshotReader;
        this.runtimeSnapshotWriter = runtimeSnapshotWriter;
        this.debugFlagWriter = debugFlagWriter;
    }

    public @NotNull String getLanguage() {
        return runtimeState.getLanguage();
    }

    public boolean isMonitoringEnabled() {
        return snapshot().monitoringEnabled();
    }

    public int getMonitoringSampleIntervalSeconds() {
        return snapshot().monitoringSampleIntervalSeconds();
    }

    public double getMonitoringMinTps() {
        return snapshot().monitoringMinTps();
    }

    public double getMonitoringMaxErrorsPerMinute() {
        return snapshot().monitoringMaxErrorsPerMinute();
    }

    public long getMonitoringMaxTriggerRatePerMinute() {
        return snapshot().monitoringMaxTriggerRatePerMinute();
    }

    public double getMonitoringMaxParticleWindowDropRate() {
        return snapshot().monitoringMaxParticleWindowDropRate();
    }

    public int getMonitoringMaxStatsPending() {
        return snapshot().monitoringMaxStatsPending();
    }

    public boolean isSafetyModeEnabled() {
        return snapshot().safetyModeEnabled();
    }

    public boolean isSafetyModeAutoOnAlert() {
        return snapshot().safetyModeAutoOnAlert();
    }

    public int getSafetyModeAlertBurstThreshold() {
        return snapshot().safetyModeAlertBurstThreshold();
    }

    public int getSafetyModeAlertWindowSeconds() {
        return snapshot().safetyModeAlertWindowSeconds();
    }

    public int getSafetyModeAutoRecoverStableSamples() {
        return snapshot().safetyModeAutoRecoverStableSamples();
    }

    public double getSafetyModeChanceMultiplier() {
        return snapshot().safetyModeChanceMultiplier();
    }

    public int getSafetyModeTickModuloMultiplier() {
        return snapshot().safetyModeTickModuloMultiplier();
    }

    public boolean isSafetyModeSuppressParticles() {
        return snapshot().safetyModeSuppressParticles();
    }

    public long getEnchantBudgetNanosPerTick() {
        return snapshot().enchantBudgetNanosPerTick();
    }

    public double getEnchantBudgetDegradedChanceMultiplier() {
        return snapshot().enchantBudgetDegradedChanceMultiplier();
    }

    public int getEnchantBudgetDegradedTickModuloMultiplier() {
        return snapshot().enchantBudgetDegradedTickModuloMultiplier();
    }

    public boolean isEnchantBudgetSuppressParticles() {
        return snapshot().enchantBudgetSuppressParticles();
    }

    public int getEnchantBudgetBreakerConsecutiveOverruns() {
        return snapshot().enchantBudgetBreakerConsecutiveOverruns();
    }

    public int getEnchantBudgetBreakerCooldownTicks() {
        return snapshot().enchantBudgetBreakerCooldownTicks();
    }

    public boolean isEnchantBudgetSkipExecutionOnBreaker() {
        return snapshot().enchantBudgetSkipExecutionOnBreaker();
    }

    public void apply(@NotNull RuntimeSettingsLoader.RuntimeSettings settings) {
        runtimeState.apply(settings);
        debugFlagWriter.set(settings.debug());
        runtimeSnapshotWriter.set(settings.runtimeConfig());
    }

    public boolean reload(@NotNull Path filePath) {
        try {
            File configFile = filePath.resolve("config.yml").toFile();
            if (!configFile.exists()) {
                return false;
            }
            FileConfiguration configuration = EnchADDConfigBootstrapper.loadOrCreate(filePath);
            RuntimeSettingsLoader.RuntimeSettings parsedSettings = RuntimeSettingsLoader.parse(
                    configuration,
                    currentSettings()
            );
            apply(parsedSettings);
            EnchADDConfigBootstrapper.save(configuration, filePath);
            return true;
        } catch (IOException | RuntimeException ex) {
            Bukkit.getLogger().warning("[EnchADD] 配置热重载失败: " + ex.getMessage());
            return false;
        }
    }

    private @NotNull RuntimeSettingsLoader.RuntimeSettings currentSettings() {
        return new RuntimeSettingsLoader.RuntimeSettings(
                runtimeState.getLanguage(),
                runtimeState.isDebug(),
                snapshot()
        );
    }

    private @NotNull RuntimeConfigLoader.RuntimeConfigSnapshot snapshot() {
        RuntimeConfigLoader.RuntimeConfigSnapshot snapshot = runtimeSnapshotReader.get();
        if (snapshot == null) {
            snapshot = RuntimeConfigLoader.RuntimeConfigSnapshot.defaults();
            runtimeSnapshotWriter.set(snapshot);
        }
        return snapshot;
    }
}
