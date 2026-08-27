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
import net.enchadd.enchants.AbstractEnchADDEnchant;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

public class TelepathyEnchant
extends AbstractEnchADDEnchant {
    public static final Key KEY = Key.key("enchadd:telepathy");
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<EquipmentSlotGroup>();
    private final boolean onlyUserCanPickupItems;

    @Override
    @NotNull
    public Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public TelepathyEnchant(int anvilCost, int weight, EnchantmentRegistryEntry.EnchantmentCost minimumCost, EnchantmentRegistryEntry.EnchantmentCost maximumCost, Collection<TagKey<Enchantment>> enchantTagKeys, Collection<TagEntry<ItemType>> supportedItemTags, Collection<EquipmentSlotGroup> activeSlots, boolean onlyUserCanPickupItems, boolean enabled, String rarity) {
        super(KEY, anvilCost, 1, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.onlyUserCanPickupItems = onlyUserCanPickupItems;
    }

    public boolean isOnlyUserCanPickupItems() {
        return this.onlyUserCanPickupItems;
    }

    public static TelepathyEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        TelepathyEnchant telepathyEnchant = new TelepathyEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 1), EnchADDConfig.getInt(configurationSection, "weight", 5), EnchantmentRegistryEntry.EnchantmentCost.of((int)EnchADDConfig.getInt(configurationSection, "minimumCost.base", 15), (int)EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 1)), EnchantmentRegistryEntry.EnchantmentCost.of((int)EnchADDConfig.getInt(configurationSection, "maximumCost.base", 65), (int)EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 1)), EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(configurationSection, "enchantmentTags", List.of("#in_enchanting_table"))), EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(configurationSection, "supportedItemTags", List.of("#minecraft:enchantable/mining"))), EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(configurationSection, "activeSlots", List.of("MAINHAND"))), EnchADDConfig.getBoolean(configurationSection, "onlyUserCanPickupItems", false), enabled, rarity);
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, telepathyEnchant);
        }
        return telepathyEnchant;
    }
}
