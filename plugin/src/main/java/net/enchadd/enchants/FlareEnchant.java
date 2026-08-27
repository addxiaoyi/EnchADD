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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class FlareEnchant extends ChanceEnchant {

    public static final Key KEY = Key.key("enchadd:flare");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    private final double maxTriggerChance;
    private final int glowSecondsPerLevel;

    @SuppressWarnings("squid:S00107")
    private FlareEnchant(int anvilCost,
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
            int glowSecondsPerLevel, boolean enabled, String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks, triggerChance);
        this.activeSlots.addAll(activeSlots);
        this.maxTriggerChance = maxTriggerChance;
        this.glowSecondsPerLevel = glowSecondsPerLevel;
    }

    public double getMaxTriggerChance() {
        return maxTriggerChance;
    }

    public int getGlowSecondsPerLevel() {
        return glowSecondsPerLevel;
    }

    public static FlareEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        FlareEnchant enchant = new FlareEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 6),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 24),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 2)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 50),
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
                                "minecraft:crossbow"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 100),
                EnchADDConfig.getDouble(configurationSection, "triggerChance", 0.25),
                EnchADDConfig.getDouble(configurationSection, "maxTriggerChance", 0.6),
                EnchADDConfig.getInt(configurationSection, "glowSecondsPerLevel", 4), enabled, rarity);
        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(FlareEnchant.KEY, enchant);
    }
        return enchant;
    }
}
