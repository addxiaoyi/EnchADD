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
public class BackfireEnchant extends CooldownEnchant {

    public static final Key KEY = Key.key("enchadd:backfire_curse");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final double triggerChancePerLevel;
    private final double maxTriggerChance;
    private final double selfDamageMultiplier;

    @SuppressWarnings("squid:S00107")
    public BackfireEnchant(int anvilCost,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            Collection<EquipmentSlotGroup> activeSlots,
            int maxLevel,
            int cooldownTicks,
            double triggerChancePerLevel,
            double maxTriggerChance,
            double selfDamageMultiplier,
            boolean enabled,
            String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.activeSlots.addAll(activeSlots);
        this.triggerChancePerLevel = triggerChancePerLevel;
        this.maxTriggerChance = maxTriggerChance;
        this.selfDamageMultiplier = selfDamageMultiplier;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public double getTriggerChancePerLevel() {
        return triggerChancePerLevel;
    }

    public double getMaxTriggerChance() {
        return maxTriggerChance;
    }

    public double getSelfDamageMultiplier() {
        return selfDamageMultiplier;
    }

    public static BackfireEnchant create(ConfigurationSection configurationSection) {
        int anvilCost = EnchADDConfig.getInt(configurationSection, "anvilCost", 1);
        int weight = EnchADDConfig.getInt(configurationSection, "weight", 2);
        int maxLevel = EnchADDConfig.getInt(configurationSection, "maxLevel", 1);
        int cooldownTicks = EnchADDConfig.getInt(configurationSection, "cooldownTicks", 0);

        double triggerChancePerLevel = EnchADDConfig.getDouble(configurationSection, "triggerChancePerLevel", 0.05);
        double maxTriggerChance = EnchADDConfig.getDouble(configurationSection, "maxTriggerChance", 0.5);
        double selfDamageMultiplier = EnchADDConfig.getDouble(configurationSection, "selfDamageMultiplier", 0.5);

        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");

        BackfireEnchant backfireEnchant = new BackfireEnchant(
            anvilCost,
            weight,
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
                List.of("#in_enchanting_table")
            )),
            EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(
                configurationSection,
                "supportedItemTags",
                List.of("#minecraft:enchantable/armor")
            )),
            EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                configurationSection,
                "activeSlots",
                List.of("ANY")
            )),
            maxLevel,
            cooldownTicks,
            triggerChancePerLevel,
            maxTriggerChance,
            selfDamageMultiplier,
            enabled,
            rarity
        );

        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, backfireEnchant);
        }
        return backfireEnchant;
    }
}
