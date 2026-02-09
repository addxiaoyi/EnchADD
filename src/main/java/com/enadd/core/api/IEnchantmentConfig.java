package com.enadd.core.api;

/**
 * Configuration interface for enchantment properties.
 * Provides a standardized way to define enchantment behavior.
 */
public interface IEnchantmentConfig {

    IEnchantmentConfig DEFAULT = new IEnchantmentConfig() {
        @Override
        public int getBaseCost(int level) {
            return 10 + (level - 1) * 5;
        }

        @Override
        public int getCostPerLevel() {
            return 5;
        }

        @Override
        public int getMaxBonus() {
            return 15;
        }

        @Override
        public int getMaxLevel() {
            return 5;
        }

        @Override
        public String getRarity() {
            return "common";
        }

        @Override
        public boolean isTreasure() {
            return false;
        }
    };

    /**
     * Get the base cost for the first level of this enchantment.
     *
     * @param level the enchantment level (1-based)
     * @return the minimum cost in experience levels
     */
    int getBaseCost(int level);

    /**
     * Get the cost increase per level.
     *
     * @return the additional cost per level
     */
    int getCostPerLevel();

    /**
     * Get the maximum bonus added to the minimum cost.
     *
     * @return the maximum bonus value
     */
    int getMaxBonus();

    /**
     * Get the maximum level for this enchantment.
     *
     * @return the maximum enchantment level
     */
    int getMaxLevel();

    /**
     * Get the rarity of this enchantment.
     *
     * @return the rarity identifier
     */
    String getRarity();

    /**
     * Check if this enchantment is treasure-only.
     *
     * @return true if treasure-only
     */
    boolean isTreasure();

    /**
     * Check if this enchantment is a curse.
     *
     * @return true if this is a curse enchantment
     */
    default boolean isCursed() {
        return false;
    }

    /**
     * Get the minimum level required to use this enchantment.
     *
     * @return the minimum player level required
     */
    default int getMinLevel() {
        return 1;
    }

    /**
     * Check if this enchantment is enabled.
     *
     * @return true if enabled
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Get the weight of this enchantment.
     *
     * @return the weight value
     */
    default int getWeight() {
        return 10;
    }
}
