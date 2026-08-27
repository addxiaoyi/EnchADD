package net.enchadd.config;

import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class EnchADDConfigValueBridge {

    private EnchADDConfigValueBridge() {
    }

    public static void migrateEnchantTags(@NotNull ConfigurationSection section) {
        EnchantAcquisitionPolicy.migrateEnchantTags(section);
    }

    public static List<String> getStringList(ConfigurationSection section, String key, List<String> defaultValue) {
        return ConfigSupport.getStringList(section, key, defaultValue);
    }

    public static String getString(ConfigurationSection section, String key, String defaultValue) {
        return ConfigSupport.getString(section, key, defaultValue);
    }

    public static int getInt(ConfigurationSection section, String key, int defaultValue) {
        return ConfigSupport.getInt(section, key, defaultValue);
    }

    public static long getLong(ConfigurationSection section, String key, long defaultValue) {
        return ConfigSupport.getLong(section, key, defaultValue);
    }

    public static double getDouble(ConfigurationSection section, String key, double defaultValue) {
        return ConfigSupport.getDouble(section, key, defaultValue);
    }

    public static boolean getBoolean(ConfigurationSection section, String key, boolean defaultValue) {
        return ConfigSupport.getBoolean(section, key, defaultValue);
    }

    public static Set<EquipmentSlotGroup> getEquipmentSlotGroups(@NotNull List<String> slots) {
        return ConfigSupport.getEquipmentSlotGroups(slots);
    }

    public static Set<TagEntry<ItemType>> getItemTagEntriesFromList(@NotNull List<String> tags) {
        return ConfigSupport.getItemTagEntriesFromList(tags);
    }

    public static Set<TagKey<Enchantment>> getEnchantmentTagKeysFromList(@NotNull List<String> tags) {
        return ConfigSupport.getEnchantmentTagKeysFromList(tags);
    }

    public static ConfigurationSection getConfigSection(ConfigurationSection section, String key) {
        return ConfigSupport.getConfigSection(section, key);
    }

    public static String normalizeLanguage(String language) {
        return ConfigSupport.normalizeLanguage(language);
    }

    public static double clamp(double value, double min, double max) {
        return ConfigSupport.clamp(value, min, max);
    }
}
