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
public class BraceEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:brace");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final double knockbackReductionPerLevel;
    private final double maxReduction;

    @SuppressWarnings("squid:S00107")
    private BraceEnchant(int anvilCost,
                         int weight,
                         EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                         EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                         Collection<TagKey<Enchantment>> enchantTagKeys,
                         Collection<TagEntry<ItemType>> supportedItemTags,
                         Collection<EquipmentSlotGroup> activeSlots,
                         int maxLevel,
                         double knockbackReductionPerLevel,
                         double maxReduction,
                         boolean enabled,
                         String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.knockbackReductionPerLevel = knockbackReductionPerLevel;
        this.maxReduction = maxReduction;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public double getKnockbackReductionPerLevel() {
        return knockbackReductionPerLevel;
    }

    public double getMaxReduction() {
        return maxReduction;
    }

    public static BraceEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        BraceEnchant enchant = new BraceEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 6),
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
                        List.of("minecraft:shield")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("OFFHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getDouble(configurationSection, "knockbackReductionPerLevel", 0.15),
                EnchADDConfig.getDouble(configurationSection, "maxReduction", 0.45),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
