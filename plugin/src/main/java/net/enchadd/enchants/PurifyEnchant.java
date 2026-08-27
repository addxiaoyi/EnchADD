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
public class PurifyEnchant extends ChanceEnchant {

    public static final Key KEY = Key.key("enchadd:purify");

    
    
    
    
    
    
    
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    @SuppressWarnings("squid:S00107")
    private PurifyEnchant(int anvilCost,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            Collection<EquipmentSlotGroup> activeSlots,
            int maxLevel,
            int cooldownTicks,
            double triggerChance, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks, triggerChance);
        this.activeSlots.addAll(activeSlots);
    }

    

    

    

    

    

    

    

    

    public static PurifyEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
PurifyEnchant enchant = new PurifyEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 10),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 24),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 2)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 52),
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
                                "minecraft:leather_helmet",
                                "minecraft:chainmail_helmet",
                                "minecraft:iron_helmet",
                                "minecraft:golden_helmet",
                                "minecraft:diamond_helmet",
                                "minecraft:netherite_helmet",
                                "minecraft:turtle_helmet"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("HEAD")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 2),
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 100),
                EnchADDConfig.getDouble(configurationSection, "triggerChance", 0.2), enabled, rarity);
        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(PurifyEnchant.KEY, enchant);
    }
        return enchant;
    }
}
