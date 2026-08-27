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
public class RetchingEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:retching_curse");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final int foodGainReductionPerLevel;
    private final int maxFoodGainReduction;
    private final int nauseaSecondsPerLevel;
    private final int nauseaAmplifier;

    @SuppressWarnings("squid:S00107")
    private RetchingEnchant(int anvilCost,
                            int weight,
                            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                            Collection<TagKey<Enchantment>> enchantTagKeys,
                            Collection<TagEntry<ItemType>> supportedItemTags,
                            Collection<EquipmentSlotGroup> activeSlots,
                            int maxLevel,
                            int foodGainReductionPerLevel,
                            int maxFoodGainReduction,
                            int nauseaSecondsPerLevel,
                            int nauseaAmplifier,
                            boolean enabled,
                            String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.foodGainReductionPerLevel = foodGainReductionPerLevel;
        this.maxFoodGainReduction = maxFoodGainReduction;
        this.nauseaSecondsPerLevel = nauseaSecondsPerLevel;
        this.nauseaAmplifier = nauseaAmplifier;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public int getFoodGainReductionPerLevel() {
        return foodGainReductionPerLevel;
    }

    public int getMaxFoodGainReduction() {
        return maxFoodGainReduction;
    }

    public int getNauseaSecondsPerLevel() {
        return nauseaSecondsPerLevel;
    }

    public int getNauseaAmplifier() {
        return nauseaAmplifier;
    }

    public static RetchingEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        RetchingEnchant enchant = new RetchingEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 2),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 0),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 24),
                        EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 1)
                ),
                EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "enchantmentTags",
                        List.of("#in_enchanting_table", "#curse")
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
                EnchADDConfig.getInt(configurationSection, "foodGainReductionPerLevel", 1),
                EnchADDConfig.getInt(configurationSection, "maxFoodGainReduction", 3),
                EnchADDConfig.getInt(configurationSection, "nauseaSecondsPerLevel", 2),
                EnchADDConfig.getInt(configurationSection, "nauseaAmplifier", 0),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
