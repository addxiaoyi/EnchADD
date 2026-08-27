package net.enchadd.config;

import net.enchadd.EnchADDConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EnchantConfigurationAssembler {

    private EnchantConfigurationAssembler() {
    }

    public static void applyCommentsAndRegister(@NotNull FileConfiguration configuration) {
        configuration.setComments("enchants", List.of("以下为各类常规附魔的配置项"));
        ConfigurationSection enchantsSection = ConfigSupport.getConfigSection(configuration, "enchants");
        configuration.setComments("curses", List.of("以下为各类诅咒附魔的配置项"));
        ConfigurationSection cursesSection = ConfigSupport.getConfigSection(configuration, "curses");

        EnchantAcquisitionPolicy.migrateEnchantTags(enchantsSection);
        EnchantAcquisitionPolicy.migrateEnchantTags(cursesSection);
        EnchantRegistrationAssembler.registerConfiguredEnchants(enchantsSection, cursesSection);

        if (EnchantAcquisitionPolicy.applyAcquisitionPolicyDefaults(configuration, enchantsSection, cursesSection)) {
            LegacyConfigCleanup.removeDeletedEnchantSections(enchantsSection);
            EnchADDConfig.ENCHANTS.clear();
            EnchantRegistrationAssembler.registerConfiguredEnchants(enchantsSection, cursesSection);
        }
    }
}
