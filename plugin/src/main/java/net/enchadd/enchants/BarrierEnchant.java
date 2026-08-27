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
public class BarrierEnchant extends ChanceEnchant {

    public static final Key KEY = Key.key("enchadd:barrier");

    
    
    
    
    
    
    
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }
    private final double maxTriggerChance;
    private final double knockbackRadiusPerLevel;

    @SuppressWarnings("squid:S00107")
    private BarrierEnchant(int anvilCost,
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
            double knockbackRadiusPerLevel, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks, triggerChance);
        this.activeSlots.addAll(activeSlots);
        this.maxTriggerChance = maxTriggerChance;
        this.knockbackRadiusPerLevel = knockbackRadiusPerLevel;
    }

    

    

    

    

    

    

    

    

    public double getMaxTriggerChance() {
        return maxTriggerChance;
    }


    public double getKnockbackRadiusPerLevel() {
        return knockbackRadiusPerLevel;
    }

    public static BarrierEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
BarrierEnchant enchant = new BarrierEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 4),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 28),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 4)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 64),
                        EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 3)
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
                EnchADDConfig.getInt(configurationSection, "maxLevel", 2),
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 200),
                EnchADDConfig.getDouble(configurationSection, "triggerChance", 0.3),
                EnchADDConfig.getDouble(configurationSection, "maxTriggerChance", 0.6),
                EnchADDConfig.getDouble(configurationSection, "knockbackRadiusPerLevel", 2.0), enabled, rarity);
        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(BarrierEnchant.KEY, enchant);
    }
        return enchant;
    }
}

