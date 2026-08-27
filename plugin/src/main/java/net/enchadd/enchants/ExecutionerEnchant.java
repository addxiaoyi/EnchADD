package net.enchadd.enchants;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.enchadd.EnchADDConfig;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class ExecutionerEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:executioner");

    
    
    
    
    private final double damageMultiplierPerLevel, maxDamageHpThreshold;
    
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public ExecutionerEnchant(int anvilCost,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            Collection<EquipmentSlotGroup> activeSlots,
            int maxLevel,
            double damageMultiplierPerLevel,
            double maxDamageHpThreshold, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.damageMultiplierPerLevel = damageMultiplierPerLevel;
        this.maxDamageHpThreshold = maxDamageHpThreshold;
    }

    

    

    

    public double getDamageMultiplierPerLevel() {
        return damageMultiplierPerLevel;
    }

    public double getMaxDamageHpThreshold() {
        return maxDamageHpThreshold;
    }

    

    

    

    

    

    public static ExecutionerEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
ExecutionerEnchant executionerEnchant = new ExecutionerEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 10),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 40),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 65),
                        EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 1)
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
                                "#minecraft:enchantable/weapon"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of(
                                "MAINHAND"
                        )
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 5),
                EnchADDConfig.getDouble(configurationSection, "damageMultiplierPerLevel", 0.05),
                EnchADDConfig.getDouble(configurationSection, "maxDamageHpThreshold", 0.25), enabled, rarity);

        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(ExecutionerEnchant.KEY, executionerEnchant);
    }

        return executionerEnchant;
    }

}
