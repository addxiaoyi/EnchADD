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
public final class SpearEnchant extends ChanceEnchant {

    public static final Key LANCER_KEY = Key.key("enchadd:lancer");
    public static final Key REACH_KEY = Key.key("enchadd:reach");
    public static final Key SKEWER_KEY = Key.key("enchadd:skewer");
    public static final Key COUNTERTHRUST_KEY = Key.key("enchadd:counterthrust");

    private static final List<String> SPEAR_ITEMS = List.of(
            "minecraft:wooden_spear",
            "minecraft:stone_spear",
            "minecraft:copper_spear",
            "minecraft:iron_spear",
            "minecraft:golden_spear",
            "minecraft:diamond_spear",
            "minecraft:netherite_spear",
            "minecraft:trident"
    );

    public enum Mode {
        LANCER,
        REACH,
        SKEWER,
        COUNTERTHRUST
    }

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final Mode mode;
    private final double powerPerLevel;
    private final double maxPower;
    private final double minDistance;
    private final double maxDistance;
    private final int durationTicksPerLevel;

    private SpearEnchant(Key key,
                          Mode mode,
                          int anvilCost,
                          int weight,
                          EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                          EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                          Collection<TagKey<Enchantment>> enchantTagKeys,
                          Collection<TagEntry<ItemType>> supportedItemTags,
                          Collection<EquipmentSlotGroup> activeSlots,
                          int maxLevel,
                          int cooldownTicks,
                          double triggerChance,
                          double powerPerLevel,
                          double maxPower,
                          double minDistance,
                          double maxDistance,
                          int durationTicksPerLevel,
                          boolean enabled,
                          String rarity) {
        super(key, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys,
                supportedItemTags, enabled, rarity, cooldownTicks, triggerChance);
        this.mode = mode;
        this.activeSlots.addAll(activeSlots);
        this.powerPerLevel = powerPerLevel;
        this.maxPower = maxPower;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.durationTicksPerLevel = durationTicksPerLevel;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(activeSlots);
    }

    public Mode getMode() {
        return mode;
    }

    public double getPowerPerLevel() {
        return powerPerLevel;
    }

    public double getMaxPower() {
        return maxPower;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public int getDurationTicksPerLevel() {
        return durationTicksPerLevel;
    }

    public static SpearEnchant createLancer(ConfigurationSection section) {
        return create(section, LANCER_KEY, Mode.LANCER, 2, 6, 28, 56, 3, 24, 1.0, 0.08, 0.24, 0.0, 0.0, 0);
    }

    public static SpearEnchant createReach(ConfigurationSection section) {
        return create(section, REACH_KEY, Mode.REACH, 2, 7, 30, 58, 3, 18, 1.0, 0.06, 0.18, 3.5, 7.0, 0);
    }

    public static SpearEnchant createSkewer(ConfigurationSection section) {
        return create(section, SKEWER_KEY, Mode.SKEWER, 2, 7, 32, 60, 3, 60, 0.22, 0.0, 40.0, 0.0, 0.0, 10);
    }

    public static SpearEnchant createCounterthrust(ConfigurationSection section) {
        return create(section, COUNTERTHRUST_KEY, Mode.COUNTERTHRUST, 2, 6, 34, 62, 3, 30, 1.0, 0.06, 0.18, 0.0, 0.0, 0);
    }

    private static SpearEnchant create(ConfigurationSection section,
                                        Key key,
                                        Mode mode,
                                        int defaultAnvilCost,
                                        int defaultWeight,
                                        int defaultMinimumCost,
                                        int defaultMaximumCost,
                                        int defaultMaxLevel,
                                        int defaultCooldown,
                                        double defaultChance,
                                        double defaultPower,
                                        double defaultMaxPower,
                                        double defaultMinDistance,
                                        double defaultMaxDistance,
                                        int defaultDurationPerLevel) {
        boolean enabled = EnchADDConfig.getBoolean(section, "enabled", true);
        String rarity = section.getString("rarity", "COMMON");
        SpearEnchant enchant = new SpearEnchant(
                key,
                mode,
                EnchADDConfig.getInt(section, "anvilCost", defaultAnvilCost),
                EnchADDConfig.getInt(section, "weight", defaultWeight),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(section, "minimumCost.base", defaultMinimumCost),
                        EnchADDConfig.getInt(section, "minimumCost.additionalPerLevel", 3)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(section, "maximumCost.base", defaultMaximumCost),
                        EnchADDConfig.getInt(section, "maximumCost.additionalPerLevel", 2)
                ),
                EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(
                        section, "enchantmentTags", List.of("#in_enchanting_table")
                )),
                EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(
                        section, "supportedItemTags", SPEAR_ITEMS
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        section, "activeSlots", List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(section, "maxLevel", defaultMaxLevel),
                EnchADDConfig.getInt(section, "cooldownTicks", defaultCooldown),
                EnchADDConfig.getDouble(section, "triggerChance", defaultChance),
                EnchADDConfig.getDouble(section, "powerPerLevel", defaultPower),
                EnchADDConfig.getDouble(section, "maxPower", defaultMaxPower),
                EnchADDConfig.getDouble(section, "minDistance", defaultMinDistance),
                EnchADDConfig.getDouble(section, "maxDistance", defaultMaxDistance),
                EnchADDConfig.getInt(section, "durationTicksPerLevel", defaultDurationPerLevel),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(key, enchant);
        }
        return enchant;
    }
}