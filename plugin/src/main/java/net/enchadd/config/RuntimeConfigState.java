package net.enchadd.config;

import org.jetbrains.annotations.NotNull;

public final class RuntimeConfigState {

    private volatile RuntimeSettingsLoader.RuntimeSettings runtimeSettings =
            RuntimeSettingsLoader.RuntimeSettings.defaults();

    public @NotNull String getLanguage() {
        return runtimeSettings.language();
    }

    public boolean isDebug() {
        return runtimeSettings.debug();
    }

    public @NotNull RuntimeConfigLoader.RuntimeConfigSnapshot snapshot() {
        return runtimeSettings.runtimeConfig();
    }

    public void apply(@NotNull RuntimeSettingsLoader.RuntimeSettings settings) {
        this.runtimeSettings = settings;
    }
}
