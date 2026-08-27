package net.enchadd.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public final class RuntimeSettingsLoader {

    private RuntimeSettingsLoader() {
    }

    public record RuntimeSettings(
            @NotNull String language,
            boolean debug,
            @NotNull RuntimeConfigLoader.RuntimeConfigSnapshot runtimeConfig
    ) {
        public static RuntimeSettings defaults() {
            return new RuntimeSettings(
                    "zh",
                    false,
                    RuntimeConfigLoader.RuntimeConfigSnapshot.defaults()
            );
        }
    }

    public static RuntimeSettings parse(@NotNull FileConfiguration configuration,
                                        @NotNull RuntimeSettings fallback) {
        String language = ConfigSupport.normalizeLanguage(
                ConfigSupport.getString(configuration, "language", fallback.language())
        );
        boolean debug = ConfigSupport.getBoolean(configuration, "debug", fallback.debug());
        RuntimeConfigLoader.RuntimeConfigSnapshot runtimeConfig = RuntimeConfigLoader.parse(configuration);
        return new RuntimeSettings(language, debug, runtimeConfig);
    }
}
