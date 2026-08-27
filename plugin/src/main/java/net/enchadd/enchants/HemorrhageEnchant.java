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

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class HemorrhageEnchant extends ChanceEnchant {

    public static final Key KEY = Key.key("enchadd:hemorrhage");

    
    
    
    
    
    
    
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }
    private final int witherSecondsPerLevel;
    private final int witherAmplifier;

    @SuppressWarnings("squid:S00107")
    private HemorrhageEnchant(int anvilCost,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            Collection<EquipmentSlotGroup> activeSlots,
            int maxLevel,
            int cooldownTicks,
            int witherSecondsPerLevel,
            int witherAmplifier,
            double triggerChance, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks, triggerChance);
        this.activeSlots.addAll(activeSlots);
        this.witherSecondsPerLevel = witherSecondsPerLevel;
        this.witherAmplifier = witherAmplifier;
    }

    

    

    

    

    

    

    

    

    public int getWitherSecondsPerLevel() {
        return witherSecondsPerLevel;
    }

    public int getWitherAmplifier() {
        return witherAmplifier;
    }
    public static HemorrhageEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        HemorrhageEnchant enchant = new HemorrhageEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 8),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 30),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 4)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 60),
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
                                "minecraft:wooden_sword",
                                "minecraft:stone_sword",
                                "minecraft:iron_sword",
                                "minecraft:golden_sword",
                                "minecraft:diamond_sword",
                                "minecraft:netherite_sword",
                                "minecraft:wooden_axe",
                                "minecraft:stone_axe",
                                "minecraft:iron_axe",
                                "minecraft:golden_axe",
                                "minecraft:diamond_axe",
                                "minecraft:netherite_axe"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 60),
                EnchADDConfig.getInt(configurationSection, "witherSecondsPerLevel", 2),
                EnchADDConfig.getInt(configurationSection, "witherAmplifier", 0),
                EnchADDConfig.getDouble(configurationSection, "triggerChance", 0.15),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(HemorrhageEnchant.KEY, enchant);
        }
        return enchant;
    }
}
