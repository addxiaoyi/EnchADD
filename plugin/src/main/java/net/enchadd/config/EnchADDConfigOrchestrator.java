package net.enchadd.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public final class EnchADDConfigOrchestrator {

    @FunctionalInterface
    public interface RuntimeSettingsApplier {
        void apply(@NotNull RuntimeSettingsLoader.RuntimeSettings settings);
    }

    @FunctionalInterface
    public interface ConflictLoader {
        void load(@NotNull ConfigurationSection conflictsSection);
    }

    private final @NotNull RuntimeSettingsApplier runtimeSettingsApplier;
    private final @NotNull ConflictLoader conflictLoader;

    public EnchADDConfigOrchestrator(@NotNull RuntimeSettingsApplier runtimeSettingsApplier,
                                     @NotNull ConflictLoader conflictLoader) {
        this.runtimeSettingsApplier = runtimeSettingsApplier;
        this.conflictLoader = conflictLoader;
    }

    public void initialize(@NotNull Path filePath) throws IOException {
        FileConfiguration configuration = EnchADDConfigBootstrapper.loadOrCreate(filePath);
        RuntimeSettingsLoader.RuntimeSettings parsedSettings = RuntimeSettingsLoader.parse(
                configuration,
                RuntimeSettingsLoader.RuntimeSettings.defaults()
        );
        runtimeSettingsApplier.apply(parsedSettings);

        ConfigurationSection conflictsSection = EnchADDConfigValueBridge.getConfigSection(configuration, "conflicts");
        conflictLoader.load(conflictsSection);

        EnchADDConfigBootstrapper.applyTopLevelDocumentation(configuration);
        EnchantConfigurationAssembler.applyCommentsAndRegister(configuration);
        EnchADDConfigBootstrapper.save(configuration, filePath);
    }

    public static @NotNull EnchADDConfigOrchestrator forEnchADD(
            @NotNull RuntimeSettingsApplier runtimeSettingsApplier,
            @NotNull ConflictLoader conflictLoader
    ) {
        return new EnchADDConfigOrchestrator(runtimeSettingsApplier, conflictLoader);
    }
}
