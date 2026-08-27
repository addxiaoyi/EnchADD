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

public class TideRunnerEnchant
extends CooldownEnchant {
    public static final Key KEY = Key.key((String)"enchadd:tide_runner");
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<EquipmentSlotGroup>();
    private final int graceTicksPerLevel;
    private final double speedAmplifierPerLevel;

    @Override
    @NotNull
    public Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    private TideRunnerEnchant(int anvilCost, int weight, EnchantmentRegistryEntry.EnchantmentCost minimumCost, EnchantmentRegistryEntry.EnchantmentCost maximumCost, Collection<TagKey<Enchantment>> enchantTagKeys, Collection<TagEntry<ItemType>> supportedItemTags, Collection<EquipmentSlotGroup> activeSlots, int maxLevel, int cooldownTicks, int graceTicksPerLevel, double speedAmplifierPerLevel, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.activeSlots.addAll(activeSlots);
        this.graceTicksPerLevel = graceTicksPerLevel;
        this.speedAmplifierPerLevel = speedAmplifierPerLevel;
    }

    public int getGraceTicksPerLevel() {
        return this.graceTicksPerLevel;
    }

    public double getSpeedAmplifierPerLevel() {
        return this.speedAmplifierPerLevel;
    }

    public static TideRunnerEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        TideRunnerEnchant enchant = new TideRunnerEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 2), EnchADDConfig.getInt(configurationSection, "weight", 6), EnchantmentRegistryEntry.EnchantmentCost.of((int)EnchADDConfig.getInt(configurationSection, "minimumCost.base", 15), (int)EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 4)), EnchantmentRegistryEntry.EnchantmentCost.of((int)EnchADDConfig.getInt(configurationSection, "maximumCost.base", 45), (int)EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 4)), EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(configurationSection, "enchantmentTags", List.of("#in_enchanting_table"))), EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(configurationSection, "supportedItemTags", List.of("minecraft:leather_boots", "minecraft:chainmail_boots", "minecraft:iron_boots", "minecraft:golden_boots", "minecraft:diamond_boots", "minecraft:netherite_boots"))), EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(configurationSection, "activeSlots", List.of("FEET"))), EnchADDConfig.getInt(configurationSection, "maxLevel", 3), EnchADDConfig.getInt(configurationSection, "cooldownTicks", 200), EnchADDConfig.getInt(configurationSection, "graceTicksPerLevel", 60), EnchADDConfig.getDouble(configurationSection, "speedAmplifierPerLevel", 0.3), enabled, rarity);
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
