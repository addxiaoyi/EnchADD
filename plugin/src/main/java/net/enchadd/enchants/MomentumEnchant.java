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
public class MomentumEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:momentum");

    
    
    
    
    
    
    
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }
    private final int speedSecondsPerLevel;
    private final int speedAmplifier;

    @SuppressWarnings("squid:S00107")
    private MomentumEnchant(int anvilCost,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            Collection<EquipmentSlotGroup> activeSlots,
            int maxLevel,
            int speedSecondsPerLevel,
            int speedAmplifier, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.speedSecondsPerLevel = speedSecondsPerLevel;
        this.speedAmplifier = speedAmplifier;
    }

    

    

    

    

    

    

    

    

    public int getSpeedSecondsPerLevel() {
        return speedSecondsPerLevel;
    }

    public int getSpeedAmplifier() {
        return speedAmplifier;
    }

    public static MomentumEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
MomentumEnchant enchant = new MomentumEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 10),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 30),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 2)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 60),
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
                                "minecraft:wooden_sword",
                                "minecraft:stone_sword",
                                "minecraft:iron_sword",
                                "minecraft:diamond_sword",
                                "minecraft:netherite_sword",
                                "minecraft:golden_sword",
                                "minecraft:wooden_axe",
                                "minecraft:stone_axe",
                                "minecraft:iron_axe",
                                "minecraft:diamond_axe",
                                "minecraft:netherite_axe",
                                "minecraft:golden_axe",
                                "minecraft:trident"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of(
                                "MAINHAND"
                        )
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getInt(configurationSection, "speedSecondsPerLevel", 2),
                EnchADDConfig.getInt(configurationSection, "speedAmplifier", 0), enabled, rarity);

        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(MomentumEnchant.KEY, enchant);
    }
        return enchant;
    }
}
