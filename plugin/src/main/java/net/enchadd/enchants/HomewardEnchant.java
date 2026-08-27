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
public class HomewardEnchant extends CooldownEnchant {

    public static final Key KEY = Key.key("enchadd:homeward");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final int escapeWindowTicks;
    private final int speedSecondsPerLevel;
    private final int speedAmplifier;
    private final double fallDamageReductionPerLevel;
    private final double maxFallDamageReduction;

    @SuppressWarnings("squid:S00107")
    private HomewardEnchant(int anvilCost,
                            int weight,
                            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                            Collection<TagKey<Enchantment>> enchantTagKeys,
                            Collection<TagEntry<ItemType>> supportedItemTags,
                            Collection<EquipmentSlotGroup> activeSlots,
                            int maxLevel,
                            int cooldownTicks,
                            int escapeWindowTicks,
                            int speedSecondsPerLevel,
                            int speedAmplifier,
                            double fallDamageReductionPerLevel,
                            double maxFallDamageReduction,
                            boolean enabled,
                            String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.activeSlots.addAll(activeSlots);
        this.escapeWindowTicks = escapeWindowTicks;
        this.speedSecondsPerLevel = speedSecondsPerLevel;
        this.speedAmplifier = speedAmplifier;
        this.fallDamageReductionPerLevel = fallDamageReductionPerLevel;
        this.maxFallDamageReduction = maxFallDamageReduction;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public int getEscapeWindowTicks() {
        return escapeWindowTicks;
    }

    public int getSpeedSecondsPerLevel() {
        return speedSecondsPerLevel;
    }

    public int getSpeedAmplifier() {
        return speedAmplifier;
    }

    public double getFallDamageReductionPerLevel() {
        return fallDamageReductionPerLevel;
    }

    public double getMaxFallDamageReduction() {
        return maxFallDamageReduction;
    }

    public static HomewardEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        HomewardEnchant enchant = new HomewardEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 5),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 22),
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
                        List.of(
                                "minecraft:leather_leggings",
                                "minecraft:chainmail_leggings",
                                "minecraft:iron_leggings",
                                "minecraft:golden_leggings",
                                "minecraft:diamond_leggings",
                                "minecraft:netherite_leggings"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("LEGS")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 80),
                EnchADDConfig.getInt(configurationSection, "escapeWindowTicks", 60),
                EnchADDConfig.getInt(configurationSection, "speedSecondsPerLevel", 1),
                EnchADDConfig.getInt(configurationSection, "speedAmplifier", 0),
                EnchADDConfig.getDouble(configurationSection, "fallDamageReductionPerLevel", 0.20),
                EnchADDConfig.getDouble(configurationSection, "maxFallDamageReduction", 0.60),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
