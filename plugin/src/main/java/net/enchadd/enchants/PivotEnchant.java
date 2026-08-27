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
public class PivotEnchant extends CooldownEnchant {

    public static final Key KEY = Key.key("enchadd:pivot");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final int speedSecondsPerLevel;
    private final int speedAmplifier;

    @SuppressWarnings("squid:S00107")
    private PivotEnchant(int anvilCost,
                         int weight,
                         EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                         EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                         Collection<TagKey<Enchantment>> enchantTagKeys,
                         Collection<TagEntry<ItemType>> supportedItemTags,
                         Collection<EquipmentSlotGroup> activeSlots,
                         int maxLevel,
                         int cooldownTicks,
                         int speedSecondsPerLevel,
                         int speedAmplifier,
                         boolean enabled,
                         String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.activeSlots.addAll(activeSlots);
        this.speedSecondsPerLevel = speedSecondsPerLevel;
        this.speedAmplifier = speedAmplifier;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public int getSpeedSecondsPerLevel() {
        return speedSecondsPerLevel;
    }

    public int getSpeedAmplifier() {
        return speedAmplifier;
    }

    public static PivotEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        PivotEnchant enchant = new PivotEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 7),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 18),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 2)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 40),
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
                        List.of("minecraft:shield")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("OFFHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 2),
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 50),
                EnchADDConfig.getInt(configurationSection, "speedSecondsPerLevel", 1),
                EnchADDConfig.getInt(configurationSection, "speedAmplifier", 0),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
