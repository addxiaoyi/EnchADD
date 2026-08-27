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
public class ParryEnchant extends CooldownEnchant {

    public static final Key KEY = Key.key("enchadd:parry");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final int retaliationWindowTicks;
    private final double bonusDamagePerLevel;
    private final double maxBonusDamage;

    @SuppressWarnings("squid:S00107")
    private ParryEnchant(int anvilCost,
                         int weight,
                         EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                         EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                         Collection<TagKey<Enchantment>> enchantTagKeys,
                         Collection<TagEntry<ItemType>> supportedItemTags,
                         Collection<EquipmentSlotGroup> activeSlots,
                         int maxLevel,
                         int cooldownTicks,
                         int retaliationWindowTicks,
                         double bonusDamagePerLevel,
                         double maxBonusDamage,
                         boolean enabled,
                         String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.activeSlots.addAll(activeSlots);
        this.retaliationWindowTicks = retaliationWindowTicks;
        this.bonusDamagePerLevel = bonusDamagePerLevel;
        this.maxBonusDamage = maxBonusDamage;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public int getRetaliationWindowTicks() {
        return retaliationWindowTicks;
    }

    public double getBonusDamagePerLevel() {
        return bonusDamagePerLevel;
    }

    public double getMaxBonusDamage() {
        return maxBonusDamage;
    }

    public static ParryEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        ParryEnchant enchant = new ParryEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 6),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 24),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 52),
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
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 80),
                EnchADDConfig.getInt(configurationSection, "retaliationWindowTicks", 60),
                EnchADDConfig.getDouble(configurationSection, "bonusDamagePerLevel", 1.0),
                EnchADDConfig.getDouble(configurationSection, "maxBonusDamage", 2.5),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
