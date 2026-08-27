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
public class ThirstEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:thirst_curse");

    
    
    
    
    
    
    
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }
    private final int combatWindowTicks;
    private final double regenReductionPerLevel;
    private final int extraHungerLossPerLevel;

    @SuppressWarnings("squid:S00107")
    public ThirstEnchant(int anvilCost,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            Collection<EquipmentSlotGroup> activeSlots,
            int maxLevel,
            int combatWindowTicks,
            double regenReductionPerLevel,
            int extraHungerLossPerLevel, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.combatWindowTicks = combatWindowTicks;
        this.regenReductionPerLevel = regenReductionPerLevel;
        this.extraHungerLossPerLevel = extraHungerLossPerLevel;
    }

    

    

    

    

    

    

    

    

    public int getCombatWindowTicks() {
        return combatWindowTicks;
    }

    public double getRegenReductionPerLevel() {
        return regenReductionPerLevel;
    }

    public int getExtraHungerLossPerLevel() {
        return extraHungerLossPerLevel;
    }

    public static ThirstEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
ThirstEnchant enchant = new ThirstEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
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
                                "minecraft:leather_chestplate",
                                "minecraft:chainmail_chestplate",
                                "minecraft:iron_chestplate",
                                "minecraft:golden_chestplate",
                                "minecraft:diamond_chestplate",
                                "minecraft:netherite_chestplate"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("CHEST")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getInt(configurationSection, "combatWindowTicks", 160),
                EnchADDConfig.getDouble(configurationSection, "regenReductionPerLevel", 0.3),
                EnchADDConfig.getInt(configurationSection, "extraHungerLossPerLevel", 1), enabled, rarity);

        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(ThirstEnchant.KEY, enchant);
    }

        return enchant;
    }
}

