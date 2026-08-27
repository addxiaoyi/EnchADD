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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class GravitationEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:gravitation_curse");

    
    
    
    
    
    
    
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    @Override
    public @org.jetbrains.annotations.NotNull java.lang.Iterable<org.bukkit.inventory.EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }
    private final double extraFallDamagePerLevel;
    private final double maxFallDamageMultiplier;

    @SuppressWarnings("squid:S00107")
    private GravitationEnchant(int anvilCost,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            Collection<EquipmentSlotGroup> activeSlots,
            int maxLevel,
            double extraFallDamagePerLevel,
            double maxFallDamageMultiplier, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.extraFallDamagePerLevel = extraFallDamagePerLevel;
        this.maxFallDamageMultiplier = maxFallDamageMultiplier;
    }

    

    

    

    

    

    

    

    

    public double getExtraFallDamagePerLevel() {
        return extraFallDamagePerLevel;
    }

    public double getMaxFallDamageMultiplier() {
        return maxFallDamageMultiplier;
    }

    public static GravitationEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
GravitationEnchant enchant = new GravitationEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 2),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 0),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 30),
                        EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 1)
                ),
                EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "enchantmentTags",
                        List.of("#in_enchanting_table", "#curse")
                )),
                EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "supportedItemTags",
                        List.of(
                                "minecraft:leather_boots",
                                "minecraft:chainmail_boots",
                                "minecraft:iron_boots",
                                "minecraft:golden_boots",
                                "minecraft:diamond_boots",
                                "minecraft:netherite_boots"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("FEET")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getDouble(configurationSection, "extraFallDamagePerLevel", 0.35),
                EnchADDConfig.getDouble(configurationSection, "maxFallDamageMultiplier", 2.0), enabled, rarity);
        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(GravitationEnchant.KEY, enchant);
    }
        return enchant;
    }
}

