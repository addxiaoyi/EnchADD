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
public class RashnessEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:rashness_curse");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final double requiredAttackCooldown;
    private final double selfDamagePerLevel;
    private final double maxSelfDamage;

    @SuppressWarnings("squid:S00107")
    private RashnessEnchant(int anvilCost,
                            int weight,
                            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                            Collection<TagKey<Enchantment>> enchantTagKeys,
                            Collection<TagEntry<ItemType>> supportedItemTags,
                            Collection<EquipmentSlotGroup> activeSlots,
                            int maxLevel,
                            double requiredAttackCooldown,
                            double selfDamagePerLevel,
                            double maxSelfDamage,
                            boolean enabled,
                            String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
        this.requiredAttackCooldown = requiredAttackCooldown;
        this.selfDamagePerLevel = selfDamagePerLevel;
        this.maxSelfDamage = maxSelfDamage;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public double getRequiredAttackCooldown() {
        return requiredAttackCooldown;
    }

    public double getSelfDamagePerLevel() {
        return selfDamagePerLevel;
    }

    public double getMaxSelfDamage() {
        return maxSelfDamage;
    }

    public static RashnessEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        RashnessEnchant enchant = new RashnessEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
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
                        List.of("#minecraft:enchantable/weapon")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getDouble(configurationSection, "requiredAttackCooldown", 0.85),
                EnchADDConfig.getDouble(configurationSection, "selfDamagePerLevel", 0.5),
                EnchADDConfig.getDouble(configurationSection, "maxSelfDamage", 1.5),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
