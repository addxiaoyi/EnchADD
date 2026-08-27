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
import net.enchadd.enchants.ChanceEnchant;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

public class RicochetEnchant
extends ChanceEnchant {
    public static final Key KEY = Key.key((String)"enchadd:ricochet");
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<EquipmentSlotGroup>();
    private final double speedScale;
    private final double radius;

    @Override
    @NotNull
    public Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    private RicochetEnchant(int anvilCost, int weight, EnchantmentRegistryEntry.EnchantmentCost minimumCost, EnchantmentRegistryEntry.EnchantmentCost maximumCost, Collection<TagKey<Enchantment>> enchantTagKeys, Collection<TagEntry<ItemType>> supportedItemTags, Collection<EquipmentSlotGroup> activeSlots, int maxLevel, int cooldownTicks, double triggerChance, double speedScale, double radius, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks, triggerChance);
        this.activeSlots.addAll(activeSlots);
        this.speedScale = speedScale;
        this.radius = radius;
    }

    public double getSpeedScale() {
        return this.speedScale;
    }

    public double getRadius() {
        return this.radius;
    }

    public static RicochetEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        RicochetEnchant enchant = new RicochetEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 2), EnchADDConfig.getInt(configurationSection, "weight", 8), EnchantmentRegistryEntry.EnchantmentCost.of((int)EnchADDConfig.getInt(configurationSection, "minimumCost.base", 28), (int)EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)), EnchantmentRegistryEntry.EnchantmentCost.of((int)EnchADDConfig.getInt(configurationSection, "maximumCost.base", 55), (int)EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 2)), EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(configurationSection, "enchantmentTags", List.of("#in_enchanting_table"))), EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(configurationSection, "supportedItemTags", List.of("minecraft:bow", "minecraft:crossbow"))), EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(configurationSection, "activeSlots", List.of("MAINHAND"))), EnchADDConfig.getInt(configurationSection, "maxLevel", 2), EnchADDConfig.getInt(configurationSection, "cooldownTicks", 100), EnchADDConfig.getDouble(configurationSection, "triggerChance", 0.2), EnchADDConfig.getDouble(configurationSection, "speedScale", 0.7), EnchADDConfig.getDouble(configurationSection, "radius", 6.0), enabled, rarity);
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
