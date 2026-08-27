/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.registry.data.EnchantmentRegistryEntry$EnchantmentCost
 *  io.papermc.paper.registry.tag.TagKey
 *  io.papermc.paper.tag.TagEntry
 *  net.kyori.adventure.key.Key
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.inventory.EquipmentSlotGroup
 *  org.bukkit.inventory.ItemType
 *  org.jetbrains.annotations.NotNull
 */
package net.enchadd.enchants;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.CooldownEnchant;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

public class SonarEnchant
extends CooldownEnchant {
    public static final Key KEY = Key.key((String)"enchadd:sonar");
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<EquipmentSlotGroup>();
    private final double radiusPerLevel;
    private final int glowTicksPerLevel;

    @Override
    @NotNull
    public Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    private SonarEnchant(int anvilCost, int weight, EnchantmentRegistryEntry.EnchantmentCost minimumCost, EnchantmentRegistryEntry.EnchantmentCost maximumCost, Collection<TagKey<Enchantment>> enchantTagKeys, Collection<TagEntry<ItemType>> supportedItemTags, Collection<EquipmentSlotGroup> activeSlots, int maxLevel, int cooldownTicks, double radiusPerLevel, int glowTicksPerLevel, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.activeSlots.addAll(activeSlots);
        this.radiusPerLevel = radiusPerLevel;
        this.glowTicksPerLevel = glowTicksPerLevel;
    }

    public double getRadiusPerLevel() {
        return this.radiusPerLevel;
    }

    public int getGlowTicksPerLevel() {
        return this.glowTicksPerLevel;
    }

    public static SonarEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        SonarEnchant enchant = new SonarEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 2), EnchADDConfig.getInt(configurationSection, "weight", 8), EnchantmentRegistryEntry.EnchantmentCost.of((int)EnchADDConfig.getInt(configurationSection, "minimumCost.base", 20), (int)EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)), EnchantmentRegistryEntry.EnchantmentCost.of((int)EnchADDConfig.getInt(configurationSection, "maximumCost.base", 50), (int)EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 3)), EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(configurationSection, "enchantmentTags", List.of("#in_enchanting_table"))), EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(configurationSection, "supportedItemTags", List.of("minecraft:leather_helmet", "minecraft:chainmail_helmet", "minecraft:iron_helmet", "minecraft:golden_helmet", "minecraft:diamond_helmet", "minecraft:netherite_helmet", "minecraft:turtle_helmet"))), EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(configurationSection, "activeSlots", List.of("HEAD"))), EnchADDConfig.getInt(configurationSection, "maxLevel", 3), EnchADDConfig.getInt(configurationSection, "cooldownTicks", 80), EnchADDConfig.getDouble(configurationSection, "radiusPerLevel", 4.0), EnchADDConfig.getInt(configurationSection, "glowTicksPerLevel", 40), enabled, rarity);
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
