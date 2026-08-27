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
public class LucidityEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:lucidity");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final double durationReductionPerLevel;
    private final double maxDurationReduction;

    @SuppressWarnings("squid:S00107")
    private LucidityEnchant(int anvilCost,
                            int weight,
                            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                            Collection<TagKey<Enchantment>> enchantTagKeys,
                            Collection<TagEntry<ItemType>> supportedItemTags,
                            Collection<EquipmentSlotGroup> activeSlots,
                            int maxLevel,
                            double durationReductionPerLevel,
                            double maxDurationReduction,
                            boolean enabled,
                            String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.durationReductionPerLevel = durationReductionPerLevel;
        this.maxDurationReduction = maxDurationReduction;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public double getDurationReductionPerLevel() {
        return durationReductionPerLevel;
    }

    public double getMaxDurationReduction() {
        return maxDurationReduction;
    }

    public static LucidityEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        LucidityEnchant enchant = new LucidityEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 8),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 18),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 2)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 44),
                        EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 2)
                ),
                EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "enchantmentTags",
                        List.of("#in_enchanting_table")
                )),
                EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "supportedItemTags",
                        List.of(
                                "minecraft:leather_helmet",
                                "minecraft:chainmail_helmet",
                                "minecraft:iron_helmet",
                                "minecraft:golden_helmet",
                                "minecraft:diamond_helmet",
                                "minecraft:netherite_helmet",
                                "minecraft:turtle_helmet"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("HEAD")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getDouble(configurationSection, "durationReductionPerLevel", 0.25),
                EnchADDConfig.getDouble(configurationSection, "maxDurationReduction", 0.75),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
