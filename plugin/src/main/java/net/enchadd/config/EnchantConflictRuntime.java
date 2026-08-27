package net.enchadd.config;

import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public final class EnchantConflictRuntime {

    private EnchantConflictRuntime() {
    }

    public static void applyDefaultsAndLoad(@NotNull FileConfiguration configuration,
                                            @NotNull ConfigurationSection conflictsSection,
                                            @NotNull Map<Key, Set<Key>> incompatible,
                                            @NotNull EnchantConflictPolicy.DebugLogger debugLogger) {
        EnchantConflictPolicy.applyDefaults(configuration, conflictsSection);
        EnchantConflictPolicy.loadFromConfig(conflictsSection, incompatible, debugLogger);
    }

    public static boolean areIncompatible(@NotNull Map<Key, Set<Key>> incompatible,
                                          @NotNull Key first,
                                          @NotNull Key second) {
        if (first.equals(second)) {
            return false;
        }
        Set<Key> set = incompatible.get(first);
        if (set != null && set.contains(second)) {
            return true;
        }
        set = incompatible.get(second);
        return set != null && set.contains(first);
    }
}
