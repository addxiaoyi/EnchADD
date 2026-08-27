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
public final class FreshcatchEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:freshcatch");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final int foodLevelPerLevel;
    private final double saturationPerLevel;

    @SuppressWarnings("squid:S00107")
    private FreshcatchEnchant(int anvilCost,
                              int weight,
                              EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                              EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                              Collection<TagKey<Enchantment>> enchantTagKeys,
                              Collection<TagEntry<ItemType>> supportedItemTags,
                              Collection<EquipmentSlotGroup> activeSlots,
                              int maxLevel,
                              int foodLevelPerLevel,
                              double saturationPerLevel,
                              boolean enabled,
                              String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.foodLevelPerLevel = Math.max(0, foodLevelPerLevel);
        this.saturationPerLevel = Math.max(0.0, saturationPerLevel);
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public int getFoodLevelPerLevel() {
        return foodLevelPerLevel;
    }

    public double getSaturationPerLevel() {
        return saturationPerLevel;
    }

    public static FreshcatchEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        EnchADDConfig.getBoolean(configurationSection, "canGetFromEnchantingTable", true);

        FreshcatchEnchant enchant = new FreshcatchEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 3),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 12),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 2)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 30),
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
                        List.of("minecraft:fishing_rod")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 2),
                EnchADDConfig.getInt(configurationSection, "foodLevelPerLevel", 1),
                EnchADDConfig.getDouble(configurationSection, "saturationPerLevel", 0.5),
                enabled,
                rarity
        );

        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
