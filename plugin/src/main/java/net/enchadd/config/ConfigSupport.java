package net.enchadd.config;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public final class ConfigSupport {

    private ConfigSupport() {
    }

    public static List<String> getStringList(@NotNull ConfigurationSection section, @NotNull String key, @NotNull List<String> defaultValue) {
        List<String> list = section.contains(key) ? section.getStringList(key) : null;
        if (list == null) {
            section.set(key, defaultValue);
            return defaultValue;
        }
        return list;
    }

    public static String getString(@NotNull ConfigurationSection section, @NotNull String key, @NotNull String defaultValue) {
        String value = section.contains(key) ? section.getString(key) : null;
        if (value == null) {
            section.set(key, defaultValue);
            return defaultValue;
        }
        return value;
    }

    public static int getInt(@NotNull ConfigurationSection section, @NotNull String key, int defaultValue) {
        if (!section.contains(key)) {
            section.set(key, defaultValue);
            return defaultValue;
        }
        return section.getInt(key);
    }

    public static long getLong(@NotNull ConfigurationSection section, @NotNull String key, long defaultValue) {
        if (!section.contains(key)) {
            section.set(key, defaultValue);
            return defaultValue;
        }
        return section.getLong(key);
    }

    public static double getDouble(@NotNull ConfigurationSection section, @NotNull String key, double defaultValue) {
        if (!section.contains(key)) {
            section.set(key, defaultValue);
            return defaultValue;
        }
        return section.getDouble(key);
    }

    public static boolean getBoolean(@NotNull ConfigurationSection section, @NotNull String key, boolean defaultValue) {
        if (!section.contains(key)) {
            section.set(key, defaultValue);
            return defaultValue;
        }
        return section.getBoolean(key);
    }

    public static @NotNull ConfigurationSection getConfigSection(@NotNull ConfigurationSection section, @NotNull String key) {
        ConfigurationSection value = section.getConfigurationSection(key);
        if (value == null) {
            value = section.createSection(key);
        }
        return value;
    }

    public static @NotNull Set<EquipmentSlotGroup> getEquipmentSlotGroups(@NotNull List<String> slots) {
        Set<EquipmentSlotGroup> equipmentSlotGroups = new HashSet<>();
        for (String slot : slots) {
            if (slot == null) {
                continue;
            }
            String normalizedSlot = slot.trim();
            if (normalizedSlot.isEmpty()) {
                continue;
            }
            EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.getByName(normalizedSlot.toUpperCase(Locale.ROOT));
            if (equipmentSlotGroup == null) {
                continue;
            }
            equipmentSlotGroups.add(equipmentSlotGroup);
        }
        return equipmentSlotGroups;
    }

    public static @NotNull Set<TagEntry<ItemType>> getItemTagEntriesFromList(@NotNull List<String> tags) {
        Set<TagEntry<ItemType>> supportedItemTags = new HashSet<>();
        for (String itemTag : tags) {
            if (itemTag == null) {
                continue;
            }
            String normalizedTag = itemTag.trim();
            if (normalizedTag.isEmpty()) {
                continue;
            }
            if (normalizedTag.startsWith("#")) {
                String tagName = normalizedTag.substring(1).trim();
                if (tagName.isEmpty()) {
                    continue;
                }
                try {
                    Key key = Key.key(tagName);
                    TagKey<ItemType> tagKey = ItemTypeTagKeys.create(key);
                    supportedItemTags.add(TagEntry.tagEntry(tagKey));
                } catch (IllegalArgumentException ignored) {
                    // Ignore invalid item tag entries and keep the rest of the list usable.
                }
            } else {
                try {
                    Key key = Key.key(normalizedTag);
                    TypedKey<ItemType> typedKey = TypedKey.create(RegistryKey.ITEM, key);
                    supportedItemTags.add(TagEntry.valueEntry(typedKey));
                } catch (IllegalArgumentException ignored) {
                    // Ignore invalid item ids and keep the rest of the list usable.
                }
            }
        }
        return supportedItemTags;
    }

    public static @NotNull Set<TagKey<Enchantment>> getEnchantmentTagKeysFromList(@NotNull List<String> tags) {
        Set<TagKey<Enchantment>> enchantTagKeys = new HashSet<>();
        for (String enchantmentTag : tags) {
            if (enchantmentTag == null) {
                continue;
            }
            String normalizedTag = enchantmentTag.trim();
            if (normalizedTag.isEmpty()) {
                continue;
            }
            if (normalizedTag.startsWith("#")) {
                String tagName = normalizedTag.substring(1).trim();
                if (tagName.isEmpty()) {
                    continue;
                }
                try {
                    enchantTagKeys.add(EnchantmentTagKeys.create(Key.key(tagName)));
                } catch (IllegalArgumentException ignored) {
                    // Ignore invalid enchantment tag entries and keep the rest of the list usable.
                }
            } else {
                try {
                    enchantTagKeys.add(EnchantmentTagKeys.create(Key.key(normalizedTag)));
                } catch (IllegalArgumentException ignored) {
                    // Ignore invalid enchantment ids and keep the rest of the list usable.
                }
            }
        }
        return enchantTagKeys;
    }

    public static String normalizeLanguage(String language) {
        if (language == null) {
            return "zh";
        }
        String normalized = language.trim().toLowerCase(Locale.ROOT);
        return ("en".equals(normalized) || "zh".equals(normalized)) ? normalized : "zh";
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
