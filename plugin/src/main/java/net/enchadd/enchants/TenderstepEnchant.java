package net.enchadd.enchants;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.enchadd.EnchADDConfig;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public final class TenderstepEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:tenderstep");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final boolean protectTurtleEggs;

    @SuppressWarnings("squid:S00107")
    private TenderstepEnchant(int anvilCost,
                              int weight,
                              EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                              EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                              Collection<TagKey<Enchantment>> enchantTagKeys,
                              Collection<TagEntry<ItemType>> supportedItemTags,
                              Collection<EquipmentSlotGroup> activeSlots,
                              boolean protectTurtleEggs,
                              boolean enabled,
                              String rarity) {
        super(KEY, anvilCost, 1, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.protectTurtleEggs = protectTurtleEggs;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public boolean isProtectTurtleEggs() {
        return protectTurtleEggs;
    }

    public static TenderstepEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "UNCOMMON");
        EnchADDConfig.getBoolean(configurationSection, "canGetFromEnchantingTable", true);

        TenderstepEnchant enchant = new TenderstepEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 2),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 12),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 2)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 32),
                        EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 2)
                ),
                EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "enchantmentTags",
                        List.of(
                                "#non_treasure",
                                "#in_enchanting_table",
                                "#on_random_loot",
                                "#tradeable",
                                "#trades/desert_common",
                                "#trades/jungle_common",
                                "#trades/plains_common",
                                "#trades/savanna_common",
                                "#trades/snow_common",
                                "#trades/swamp_common",
                                "#trades/taiga_common"
                        )
                )),
                EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "supportedItemTags",
                        List.of(
                                "minecraft:leather_boots",
                                "minecraft:chainmail_boots",
                                "minecraft:iron_boots",
                                "minecraft:golden_boots",
                                "minecraft:diamond_boots",
                                "minecraft:netherite_boots"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("FEET")
                )),
                EnchADDConfig.getBoolean(configurationSection, "protectTurtleEggs", true),
                enabled,
                rarity
        );

        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
