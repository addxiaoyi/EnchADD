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
public class ResonanceEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:resonance");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final int hitsPerProcBase;
    private final int hitsPerProcReductionPerLevel;
    private final int minHitsPerProc;
    private final double bonusDamagePerLevel;
    private final double maxBonusDamage;

    @SuppressWarnings("squid:S00107")
    private ResonanceEnchant(int anvilCost,
                             int weight,
                             EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                             EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                             Collection<TagKey<Enchantment>> enchantTagKeys,
                             Collection<TagEntry<ItemType>> supportedItemTags,
                             Collection<EquipmentSlotGroup> activeSlots,
                             int maxLevel,
                             int hitsPerProcBase,
                             int hitsPerProcReductionPerLevel,
                             int minHitsPerProc,
                             double bonusDamagePerLevel,
                             double maxBonusDamage,
                             boolean enabled,
                             String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.hitsPerProcBase = hitsPerProcBase;
        this.hitsPerProcReductionPerLevel = hitsPerProcReductionPerLevel;
        this.minHitsPerProc = minHitsPerProc;
        this.bonusDamagePerLevel = bonusDamagePerLevel;
        this.maxBonusDamage = maxBonusDamage;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public int getHitsPerProcBase() {
        return hitsPerProcBase;
    }

    public int getHitsPerProcReductionPerLevel() {
        return hitsPerProcReductionPerLevel;
    }

    public int getMinHitsPerProc() {
        return minHitsPerProc;
    }

    public double getBonusDamagePerLevel() {
        return bonusDamagePerLevel;
    }

    public double getMaxBonusDamage() {
        return maxBonusDamage;
    }

    public static ResonanceEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        ResonanceEnchant enchant = new ResonanceEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 6),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 22),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 50),
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
                                "minecraft:wooden_sword",
                                "minecraft:stone_sword",
                                "minecraft:iron_sword",
                                "minecraft:golden_sword",
                                "minecraft:diamond_sword",
                                "minecraft:netherite_sword",
                                "minecraft:wooden_axe",
                                "minecraft:stone_axe",
                                "minecraft:iron_axe",
                                "minecraft:golden_axe",
                                "minecraft:diamond_axe",
                                "minecraft:netherite_axe",
                                "minecraft:trident"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getInt(configurationSection, "hitsPerProcBase", 5),
                EnchADDConfig.getInt(configurationSection, "hitsPerProcReductionPerLevel", 1),
                EnchADDConfig.getInt(configurationSection, "minHitsPerProc", 2),
                EnchADDConfig.getDouble(configurationSection, "bonusDamagePerLevel", 0.9),
                EnchADDConfig.getDouble(configurationSection, "maxBonusDamage", 3.0),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
