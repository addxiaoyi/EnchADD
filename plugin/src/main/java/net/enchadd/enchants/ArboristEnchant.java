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
public final class ArboristEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:arborist");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final int extraBlocks;
    private final boolean bypassWhenSneaking;

    @SuppressWarnings("squid:S00107")
    private ArboristEnchant(int anvilCost,
                            int weight,
                            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                            Collection<TagKey<Enchantment>> enchantTagKeys,
                            Collection<TagEntry<ItemType>> supportedItemTags,
                            Collection<EquipmentSlotGroup> activeSlots,
                            int extraBlocks,
                            boolean bypassWhenSneaking,
                            boolean enabled,
                            String rarity) {
        super(KEY, anvilCost, 1, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.extraBlocks = Math.max(0, extraBlocks);
        this.bypassWhenSneaking = bypassWhenSneaking;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public int getExtraBlocks() {
        return extraBlocks;
    }

    public boolean isBypassWhenSneaking() {
        return bypassWhenSneaking;
    }

    public static ArboristEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "UNCOMMON");
        EnchADDConfig.getBoolean(configurationSection, "canGetFromEnchantingTable", true);

        ArboristEnchant enchant = new ArboristEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 2),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 10),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 2)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 28),
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
                        List.of("#minecraft:axes")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "extraBlocks", 2),
                EnchADDConfig.getBoolean(configurationSection, "bypassWhenSneaking", true),
                enabled,
                rarity
        );

        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
