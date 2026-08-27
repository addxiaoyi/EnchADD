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
public class OverwhelmEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:overwhelm");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final int weaknessSecondsPerLevel;
    private final int weaknessAmplifier;

    @SuppressWarnings("squid:S00107")
    private OverwhelmEnchant(int anvilCost,
                             int weight,
                             EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                             EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                             Collection<TagKey<Enchantment>> enchantTagKeys,
                             Collection<TagEntry<ItemType>> supportedItemTags,
                             Collection<EquipmentSlotGroup> activeSlots,
                             int maxLevel,
                             int weaknessSecondsPerLevel,
                             int weaknessAmplifier,
                             boolean enabled,
                             String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.weaknessSecondsPerLevel = weaknessSecondsPerLevel;
        this.weaknessAmplifier = weaknessAmplifier;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public int getWeaknessSecondsPerLevel() {
        return weaknessSecondsPerLevel;
    }

    public int getWeaknessAmplifier() {
        return weaknessAmplifier;
    }

    public static OverwhelmEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "UNCOMMON");
        OverwhelmEnchant enchant = new OverwhelmEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 4),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 24),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 48),
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
                        List.of("minecraft:mace")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 2),
                EnchADDConfig.getInt(configurationSection, "weaknessSecondsPerLevel", 1),
                EnchADDConfig.getInt(configurationSection, "weaknessAmplifier", 0),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
