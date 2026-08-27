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
public class SidestepEnchant extends ChanceEnchant {

    public static final Key KEY = Key.key("enchadd:sidestep");

    
    
    
    
    
    
    
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }
    private final double maxTriggerChance;
    private final double damageReductionPerLevel;
    private final int speedSecondsPerLevel;

    @SuppressWarnings("squid:S00107")
    private SidestepEnchant(int anvilCost,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            Collection<EquipmentSlotGroup> activeSlots,
            int maxLevel,
            int cooldownTicks,
            double triggerChance,
            double maxTriggerChance,
            double damageReductionPerLevel,
            int speedSecondsPerLevel, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks, triggerChance);
        this.activeSlots.addAll(activeSlots);
        this.maxTriggerChance = maxTriggerChance;
        this.damageReductionPerLevel = damageReductionPerLevel;
        this.speedSecondsPerLevel = speedSecondsPerLevel;
    }

    

    

    

    

    

    

    

    

    public double getMaxTriggerChance() {
        return maxTriggerChance;
    }


    public double getDamageReductionPerLevel() {
        return damageReductionPerLevel;
    }

    public int getSpeedSecondsPerLevel() {
        return speedSecondsPerLevel;
    }

    public static SidestepEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
SidestepEnchant enchant = new SidestepEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 6),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 18),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 4)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 50),
                        EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 4)
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
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 160),
                EnchADDConfig.getDouble(configurationSection, "triggerChance", 0.25),
                EnchADDConfig.getDouble(configurationSection, "maxTriggerChance", 0.65),
                EnchADDConfig.getDouble(configurationSection, "damageReductionPerLevel", 0.12),
                EnchADDConfig.getInt(configurationSection, "speedSecondsPerLevel", 2), enabled, rarity);
        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(SidestepEnchant.KEY, enchant);
    }
        return enchant;
    }
}

