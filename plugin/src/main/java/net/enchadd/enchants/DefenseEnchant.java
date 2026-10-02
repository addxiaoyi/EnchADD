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
public final class DefenseEnchant extends AbstractEnchADDEnchant {

    public static final Key ARROWGUARD_KEY = Key.key("enchadd:arrowguard");
    public static final Key BLASTGUARD_KEY = Key.key("enchadd:blastguard");
    public static final Key FIREGUARD_KEY = Key.key("enchadd:fireguard");
    public static final Key FEATHERSTEP_KEY = Key.key("enchadd:featherstep");
    public static final Key FROSTGUARD_KEY = Key.key("enchadd:frostguard");

    public enum Mode {
        PROJECTILE,
        EXPLOSION,
        FIRE,
        FALL,
        FREEZE
    }

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final Mode mode;
    private final double reductionPerLevel;
    private final double maxReduction;

    private DefenseEnchant(Key key,
                           Mode mode,
                           int anvilCost,
                           int weight,
                           EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                           EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                           Collection<TagKey<Enchantment>> enchantTagKeys,
                           Collection<TagEntry<ItemType>> supportedItemTags,
                           Collection<EquipmentSlotGroup> activeSlots,
                           int maxLevel,
                           double reductionPerLevel,
                           double maxReduction,
                           boolean enabled,
                           String rarity) {
        super(key, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys,
                supportedItemTags, enabled, rarity);
        this.mode = mode;
        this.activeSlots.addAll(activeSlots);
        this.reductionPerLevel = reductionPerLevel;
        this.maxReduction = maxReduction;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(activeSlots);
    }

    public Mode getMode() {
        return mode;
    }

    public double getReductionPerLevel() {
        return reductionPerLevel;
    }

    public double getMaxReduction() {
        return maxReduction;
    }

    public static DefenseEnchant createArrowguard(ConfigurationSection section) {
        return create(section, ARROWGUARD_KEY, Mode.PROJECTILE, 2, 6, 30, 58, 3, 0.07, 0.21, "ARMOR");
    }

    public static DefenseEnchant createBlastguard(ConfigurationSection section) {
        return create(section, BLASTGUARD_KEY, Mode.EXPLOSION, 2, 5, 34, 62, 3, 0.08, 0.24, "ARMOR");
    }

    public static DefenseEnchant createFireguard(ConfigurationSection section) {
        return create(section, FIREGUARD_KEY, Mode.FIRE, 2, 6, 30, 58, 3, 0.08, 0.24, "ARMOR");
    }

    public static DefenseEnchant createFeatherstep(ConfigurationSection section) {
        return create(section, FEATHERSTEP_KEY, Mode.FALL, 2, 7, 28, 54, 3, 0.12, 0.36, "FEET");
    }

    public static DefenseEnchant createFrostguard(ConfigurationSection section) {
        return create(section, FROSTGUARD_KEY, Mode.FREEZE, 2, 6, 32, 60, 3, 0.10, 0.30, "ARMOR");
    }

    private static DefenseEnchant create(ConfigurationSection section,
                                         Key key,
                                         Mode mode,
                                         int defaultAnvilCost,
                                         int defaultWeight,
                                         int defaultMinimumCost,
                                         int defaultMaximumCost,
                                         int defaultMaxLevel,
                                         double defaultReduction,
                                         double defaultMaxReduction,
                                         String defaultSlot) {
        boolean enabled = EnchADDConfig.getBoolean(section, "enabled", true);
        String rarity = section.getString("rarity", "COMMON");
        DefenseEnchant enchant = new DefenseEnchant(
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
                        section, "supportedItemTags", List.of("#minecraft:enchantable/armor")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        section, "activeSlots", List.of(defaultSlot)
                )),
                EnchADDConfig.getInt(section, "maxLevel", defaultMaxLevel),
                EnchADDConfig.getDouble(section, "reductionPerLevel", defaultReduction),
                EnchADDConfig.getDouble(section, "maxReduction", defaultMaxReduction),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(key, enchant);
        }
        return enchant;
    }
}