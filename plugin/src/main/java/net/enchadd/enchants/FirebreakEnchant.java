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
public class FirebreakEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:firebreak");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final double combustionReductionPerLevel;
    private final double maxCombustionReduction;
    private final double directDamageReductionPerLevel;
    private final double maxDirectDamageReduction;

    @SuppressWarnings("squid:S00107")
    private FirebreakEnchant(int anvilCost,
                             int weight,
                             EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                             EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                             Collection<TagKey<Enchantment>> enchantTagKeys,
                             Collection<TagEntry<ItemType>> supportedItemTags,
                             Collection<EquipmentSlotGroup> activeSlots,
                             int maxLevel,
                             double combustionReductionPerLevel,
                             double maxCombustionReduction,
                             double directDamageReductionPerLevel,
                             double maxDirectDamageReduction,
                             boolean enabled,
                             String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.combustionReductionPerLevel = combustionReductionPerLevel;
        this.maxCombustionReduction = maxCombustionReduction;
        this.directDamageReductionPerLevel = directDamageReductionPerLevel;
        this.maxDirectDamageReduction = maxDirectDamageReduction;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public double getCombustionReductionPerLevel() {
        return combustionReductionPerLevel;
    }

    public double getMaxCombustionReduction() {
        return maxCombustionReduction;
    }

    public double getDirectDamageReductionPerLevel() {
        return directDamageReductionPerLevel;
    }

    public double getMaxDirectDamageReduction() {
        return maxDirectDamageReduction;
    }

    public static FirebreakEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        FirebreakEnchant enchant = new FirebreakEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 6),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 18),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 46),
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
                                "minecraft:leather_chestplate",
                                "minecraft:chainmail_chestplate",
                                "minecraft:iron_chestplate",
                                "minecraft:golden_chestplate",
                                "minecraft:diamond_chestplate",
                                "minecraft:netherite_chestplate"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("CHEST")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getDouble(configurationSection, "combustionReductionPerLevel", 0.20),
                EnchADDConfig.getDouble(configurationSection, "maxCombustionReduction", 0.60),
                EnchADDConfig.getDouble(configurationSection, "directDamageReductionPerLevel", 0.08),
                EnchADDConfig.getDouble(configurationSection, "maxDirectDamageReduction", 0.24),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
