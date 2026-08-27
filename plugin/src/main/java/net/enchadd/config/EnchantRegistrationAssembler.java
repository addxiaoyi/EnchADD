package net.enchadd.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public final class EnchantRegistrationAssembler {

    private EnchantRegistrationAssembler() {
    }

    public static void registerConfiguredEnchants(@NotNull ConfigurationSection enchantsSection,
                                                  @NotNull ConfigurationSection cursesSection) {
        for (EnchantRegistrationPlan.Entry entry : EnchantRegistrationPlan.normalEntries()) {
            ConfigurationSection section = ConfigSupport.getConfigSection(enchantsSection, entry.key());
            entry.registrar().accept(section);
        }
        for (EnchantRegistrationPlan.Entry entry : EnchantRegistrationPlan.curseEntries()) {
            ConfigurationSection section = ConfigSupport.getConfigSection(cursesSection, entry.key());
            entry.registrar().accept(section);
        }
    }
}
