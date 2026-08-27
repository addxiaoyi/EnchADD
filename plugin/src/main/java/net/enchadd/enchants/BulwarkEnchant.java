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
public class BulwarkEnchant extends CooldownEnchant {

    public static final Key KEY = Key.key("enchadd:bulwark");

    
    
    
    
    
    
    
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }
    private final int resistanceSecondsPerLevel;
    private final int resistanceAmplifier;

    @SuppressWarnings("squid:S00107")
    private BulwarkEnchant(int anvilCost,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            Collection<EquipmentSlotGroup> activeSlots,
            int maxLevel,
            int resistanceSecondsPerLevel,
            int resistanceAmplifier,
            int cooldownTicks, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.activeSlots.addAll(activeSlots);
        this.resistanceSecondsPerLevel = resistanceSecondsPerLevel;
        this.resistanceAmplifier = resistanceAmplifier;
    }

    

    

    

    

    

    

    

    

    public int getResistanceSecondsPerLevel() {
        return resistanceSecondsPerLevel;
    }

    public int getResistanceAmplifier() {
        return resistanceAmplifier;
    }


    public static BulwarkEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
BulwarkEnchant enchant = new BulwarkEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 9),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 28),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 2)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 58),
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
                        List.of("minecraft:shield")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("OFFHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getInt(configurationSection, "resistanceSecondsPerLevel", 2),
                EnchADDConfig.getInt(configurationSection, "resistanceAmplifier", 0),
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 80), enabled, rarity);
        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(BulwarkEnchant.KEY, enchant);
    }
        return enchant;
    }
}
