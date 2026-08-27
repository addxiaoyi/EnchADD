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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.AbstractEnchADDEnchant;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

public class CloakingEnchant
extends AbstractEnchADDEnchant {
    public static final Key KEY = Key.key((String)"enchadd:cloaking");
    private final int ticksToActivate;
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<EquipmentSlotGroup>();

    private CloakingEnchant(int anvilCost, int weight, EnchantmentRegistryEntry.EnchantmentCost minimumCost, EnchantmentRegistryEntry.EnchantmentCost maximumCost, Set<TagKey<Enchantment>> enchantmentTagKeys, Set<TagEntry<ItemType>> supportedItemTags, Set<EquipmentSlotGroup> activeSlots, int ticksToActivate, boolean enabled, String rarity) {
        super(KEY, anvilCost, 1, weight, minimumCost, maximumCost, enchantmentTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.ticksToActivate = ticksToActivate;
    }

    @Override
    @NotNull
    public Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public int getTicksToActivate() {
        return this.ticksToActivate;
    }

    public static CloakingEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        CloakingEnchant cloakingEnchant = new CloakingEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 1), EnchADDConfig.getInt(configurationSection, "weight", 10), EnchantmentRegistryEntry.EnchantmentCost.of((int)EnchADDConfig.getInt(configurationSection, "minimumCost.base", 25), (int)EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 1)), EnchantmentRegistryEntry.EnchantmentCost.of((int)EnchADDConfig.getInt(configurationSection, "maximumCost.base", 65), (int)EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 1)), EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(configurationSection, "enchantmentTags", List.of("#in_enchanting_table"))), EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(configurationSection, "supportedItemTags", List.of("#minecraft:leg_armor"))), EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(configurationSection, "activeSlots", List.of("LEGS"))), EnchADDConfig.getInt(configurationSection, "ticksToActivate", 30), enabled, rarity);
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, cloakingEnchant);
        }
        return cloakingEnchant;
    }
}
