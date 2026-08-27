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
public class GreedEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:greed_curse");

    
    
    
    
    
    
    
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }
    private final double xpBonusPerLevel;
    private final double maxXpMultiplier;
    private final double vulnerabilityPerLevel;
    private final double maxVulnerabilityMultiplier;
    private final int vulnerabilitySecondsPerLevel;

    @SuppressWarnings("squid:S00107")
    public GreedEnchant(int anvilCost,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            Collection<EquipmentSlotGroup> activeSlots,
            int maxLevel,
            double xpBonusPerLevel,
            double maxXpMultiplier,
            double vulnerabilityPerLevel,
            double maxVulnerabilityMultiplier,
            int vulnerabilitySecondsPerLevel, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.xpBonusPerLevel = xpBonusPerLevel;
        this.maxXpMultiplier = maxXpMultiplier;
        this.vulnerabilityPerLevel = vulnerabilityPerLevel;
        this.maxVulnerabilityMultiplier = maxVulnerabilityMultiplier;
        this.vulnerabilitySecondsPerLevel = vulnerabilitySecondsPerLevel;
    }

    

    

    

    

    

    

    

    

    public double getXpBonusPerLevel() {
        return xpBonusPerLevel;
    }

    public double getMaxXpMultiplier() {
        return maxXpMultiplier;
    }

    public double getVulnerabilityPerLevel() {
        return vulnerabilityPerLevel;
    }

    public double getMaxVulnerabilityMultiplier() {
        return maxVulnerabilityMultiplier;
    }

    public int getVulnerabilitySecondsPerLevel() {
        return vulnerabilitySecondsPerLevel;
    }

    public static GreedEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
GreedEnchant enchant = new GreedEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
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
                                "#minecraft:enchantable/weapon"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getDouble(configurationSection, "xpBonusPerLevel", 0.3),
                EnchADDConfig.getDouble(configurationSection, "maxXpMultiplier", 3.0),
                EnchADDConfig.getDouble(configurationSection, "vulnerabilityPerLevel", 0.25),
                EnchADDConfig.getDouble(configurationSection, "maxVulnerabilityMultiplier", 1.0),
                EnchADDConfig.getInt(configurationSection, "vulnerabilitySecondsPerLevel", 6), enabled, rarity);

        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(GreedEnchant.KEY, enchant);
    }

        return enchant;
    }
}

