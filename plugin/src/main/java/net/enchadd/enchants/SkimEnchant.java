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
public class SkimEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:skim");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final double flatDamageReductionPerLevel;
    private final double maxDamageReduction;

    @SuppressWarnings("squid:S00107")
    private SkimEnchant(int anvilCost,
                        int weight,
                        EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                        EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                        Collection<TagKey<Enchantment>> enchantTagKeys,
                        Collection<TagEntry<ItemType>> supportedItemTags,
                        Collection<EquipmentSlotGroup> activeSlots,
                        int maxLevel,
                        double flatDamageReductionPerLevel,
                        double maxDamageReduction,
                        boolean enabled,
                        String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.flatDamageReductionPerLevel = flatDamageReductionPerLevel;
        this.maxDamageReduction = maxDamageReduction;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public double getFlatDamageReductionPerLevel() {
        return flatDamageReductionPerLevel;
    }

    public double getMaxDamageReduction() {
        return maxDamageReduction;
    }

    public static SkimEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        SkimEnchant enchant = new SkimEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 5),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 20),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)
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
                        List.of("minecraft:elytra")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("CHEST")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getDouble(configurationSection, "flatDamageReductionPerLevel", 1.0),
                EnchADDConfig.getDouble(configurationSection, "maxDamageReduction", 3.0),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
