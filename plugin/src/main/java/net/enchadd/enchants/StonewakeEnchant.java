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
public final class StonewakeEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:stonewake");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final int durationSecondsPerLevel;
    private final int hasteAmplifier;

    @SuppressWarnings("squid:S00107")
    private StonewakeEnchant(int anvilCost,
                             int weight,
                             EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                             EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                             Collection<TagKey<Enchantment>> enchantTagKeys,
                             Collection<TagEntry<ItemType>> supportedItemTags,
                             Collection<EquipmentSlotGroup> activeSlots,
                             int maxLevel,
                             int durationSecondsPerLevel,
                             int hasteAmplifier,
                             boolean enabled,
                             String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.durationSecondsPerLevel = Math.max(1, durationSecondsPerLevel);
        this.hasteAmplifier = Math.max(0, hasteAmplifier);
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public int getDurationSecondsPerLevel() {
        return durationSecondsPerLevel;
    }

    public int getHasteAmplifier() {
        return hasteAmplifier;
    }

    public static StonewakeEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "UNCOMMON");
        EnchADDConfig.getBoolean(configurationSection, "canGetFromEnchantingTable", true);

        StonewakeEnchant enchant = new StonewakeEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 3),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 16),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 40),
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
                                "minecraft:wooden_pickaxe",
                                "minecraft:stone_pickaxe",
                                "minecraft:iron_pickaxe",
                                "minecraft:golden_pickaxe",
                                "minecraft:diamond_pickaxe",
                                "minecraft:netherite_pickaxe"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getInt(configurationSection, "durationSecondsPerLevel", 1),
                EnchADDConfig.getInt(configurationSection, "hasteAmplifier", 0),
                enabled,
                rarity
        );

        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
