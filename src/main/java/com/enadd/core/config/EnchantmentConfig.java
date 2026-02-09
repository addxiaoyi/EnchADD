package com.enadd.core.config;

import com.enadd.core.api.IEnchantmentConfig;
import com.enadd.enchantments.Rarity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.enchantments.Enchantment;


/**
 * Immutable configuration for enchantment properties.
 * Reduces code duplication by providing a standardized configuration pattern.
 */
public final class EnchantmentConfig implements IEnchantmentConfig {

    private final int baseCost;
    private final int costPerLevel;
    private final int maxBonus;
    private final int maxLevel;
    private final String rarity;
    private final boolean treasureOnly;
    private final int minLevel;
    private final int weight;

    private EnchantmentConfig(Builder builder) {
        this.baseCost = builder.baseCost;
        this.costPerLevel = builder.costPerLevel;
        this.maxBonus = builder.maxBonus;
        this.maxLevel = builder.maxLevel;
        this.rarity = builder.rarity;
        this.treasureOnly = builder.treasureOnly;
        this.minLevel = builder.minLevel;
        this.weight = builder.weight;
    }

    @Override
    public int getBaseCost(int level) {
        return baseCost + (level - 1) * costPerLevel;
    }

    @Override
    public int getCostPerLevel() {
        return costPerLevel;
    }

    @Override
    public int getMaxBonus() {
        return maxBonus;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public boolean isTreasure() {
        return treasureOnly;
    }

    @Override
    public String getRarity() {
        return rarity;
    }

    @Override
    public int getMinLevel() {
        return minLevel;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    /**
     * Convert to Minecraft Rarity.
     *
     * @return the corresponding Minecraft rarity
     */
    public Rarity toMinecraftRarity() {
        switch (rarity.toLowerCase()) {
            case "common":
                return Rarity.COMMON;
            case "uncommon":
                return Rarity.UNCOMMON;
            case "rare":
                return Rarity.RARE;
            case "very_rare":
                return Rarity.VERY_RARE;
            case "epic":
                return Rarity.EPIC;
            case "legendary":
                return Rarity.LEGENDARY;
            default:
                return Rarity.COMMON;
        }
    }

    /**
     * Create default equipment slots for a category.
     *
     * @param categoryName the category name
     * @return the default equipment slots
     */
    public static EquipmentSlot[] getDefaultSlots(String categoryName) {
        if (categoryName == null) {
            return new EquipmentSlot[]{EquipmentSlot.HAND};
        }

        switch (categoryName.toLowerCase()) {
            case "weapon":
            case "digger":
            case "bow":
            case "trident":
            case "crossbow":
            case "fishing_rod":
                return new EquipmentSlot[]{EquipmentSlot.HAND};
            case "armor":
            case "armor_head":
            case "armor_chest":
            case "armor_legs":
            case "armor_feet":
            case "wearable":
            case "vanishable":
                return new EquipmentSlot[]{
                    EquipmentSlot.HEAD,
                    EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS,
                    EquipmentSlot.FEET
                };
            case "breakable":
            default:
                return new EquipmentSlot[]{EquipmentSlot.HAND};
        }
    }

    /**
     * Create a new builder instance.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for EnchantmentConfig.
     */
    public static final class Builder {
        private int baseCost = 25;
        private int costPerLevel = 12;
        private int maxBonus = 22;
        private int maxLevel = 3;
        private String rarity = "rare";
        private boolean treasureOnly = false;
        private int minLevel = 1;
        private int weight = 1;

        public Builder baseCost(int baseCost) {
            this.baseCost = Math.max(1, baseCost);
            return this;
        }

        public Builder costPerLevel(int costPerLevel) {
            this.costPerLevel = Math.max(1, costPerLevel);
            return this;
        }

        public Builder maxBonus(int maxBonus) {
            this.maxBonus = Math.max(0, maxBonus);
            return this;
        }

        public Builder maxLevel(int maxLevel) {
            this.maxLevel = Math.max(1, Math.min(maxLevel, 10));
            return this;
        }

        public Builder rarity(String rarity) {
            this.rarity = rarity.toLowerCase();
            return this;
        }

        public Builder treasureOnly(boolean treasureOnly) {
            this.treasureOnly = treasureOnly;
            return this;
        }

        public Builder minLevel(int minLevel) {
            this.minLevel = Math.max(1, minLevel);
            return this;
        }

        public Builder weight(int weight) {
            this.weight = Math.max(1, weight);
            return this;
        }

        public EnchantmentConfig build() {
            return new EnchantmentConfig(this);
        }
    }

    @Override
    public String toString() {
        return String.format(
            "EnchantmentConfig{rarity=%s, maxLevel=%d, cost=%d-%d}",
            rarity, maxLevel, getBaseCost(1), getBaseCost(maxLevel) + maxBonus
        );
    }
}
