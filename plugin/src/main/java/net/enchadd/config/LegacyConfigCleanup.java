package net.enchadd.config;

import net.enchadd.legacy.LegacyEnchantDefinition;
import net.enchadd.legacy.LegacyEnchantDefinitions;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public final class LegacyConfigCleanup {

    private LegacyConfigCleanup() {
    }

    public static void removeDeletedEnchantSections(@NotNull ConfigurationSection enchantsSection) {
        for (LegacyEnchantDefinition definition : LegacyEnchantDefinitions.entries()) {
            enchantsSection.set(definition.sectionKey(), null);
            enchantsSection.set(definition.sectionKeyWithoutCurseSuffix(), null);
        }
    }
}
