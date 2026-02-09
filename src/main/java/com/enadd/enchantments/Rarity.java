package com.enadd.enchantments;

/**
 * 附魔稀有度枚举
 */
public enum Rarity {
    COMMON,
    UNCOMMON,
    RARE,
    VERY_RARE,
    EPIC,
    LEGENDARY;

    public static Rarity fromString(String rarity) {
        if (rarity == null) return COMMON;
        try {
            return valueOf(rarity.toUpperCase());
        } catch (IllegalArgumentException e) {
            return COMMON;
        }
    }
}
