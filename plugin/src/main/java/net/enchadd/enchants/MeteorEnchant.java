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
public class MeteorEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:meteor");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final double requiredFallDistance;
    private final double bonusDamagePerLevel;
    private final double maxBonusDamage;

    @SuppressWarnings("squid:S00107")
    private MeteorEnchant(int anvilCost,
                          int weight,
                          EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                          EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                          Collection<TagKey<Enchantment>> enchantTagKeys,
                          Collection<TagEntry<ItemType>> supportedItemTags,
                          Collection<EquipmentSlotGroup> activeSlots,
                          int maxLevel,
                          double requiredFallDistance,
                          double bonusDamagePerLevel,
                          double maxBonusDamage,
                          boolean enabled,
                          String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.requiredFallDistance = requiredFallDistance;
        this.bonusDamagePerLevel = bonusDamagePerLevel;
        this.maxBonusDamage = maxBonusDamage;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public double getRequiredFallDistance() {
        return requiredFallDistance;
    }

    public double getBonusDamagePerLevel() {
        return bonusDamagePerLevel;
    }

    public double getMaxBonusDamage() {
        return maxBonusDamage;
    }

    public static MeteorEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "UNCOMMON");
        MeteorEnchant enchant = new MeteorEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 4),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 24),
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
                        List.of("minecraft:mace")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getDouble(configurationSection, "requiredFallDistance", 1.5),
                EnchADDConfig.getDouble(configurationSection, "bonusDamagePerLevel", 0.9),
                EnchADDConfig.getDouble(configurationSection, "maxBonusDamage", 2.7),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
