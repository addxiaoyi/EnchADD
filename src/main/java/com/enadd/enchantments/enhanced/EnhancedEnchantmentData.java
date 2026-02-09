package com.enadd.enchantments.enhanced;

/**
 * 增强附魔数据类
 */
public class EnhancedEnchantmentData {
    private final String id;
    private final String chineseName;
    private final String chineseDescription;
    private final String rarity;
    private final int maxLevel;
    private final int baseCost;
    private final EnhancedEnchantmentRegistry.EnhancedCategory category;

    public EnhancedEnchantmentData(String id, String chineseName, String chineseDescription,
                                  String rarity, int maxLevel, int baseCost,
                                  EnhancedEnchantmentRegistry.EnhancedCategory category) {
        this.id = id;
        this.chineseName = chineseName;
        this.chineseDescription = chineseDescription;
        this.rarity = rarity;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
        this.category = category;
    }

    public String getId() { return id; }
    public String getChineseName() { return chineseName; }
    public String getChineseDescription() { return chineseDescription; }
    public String getRarity() { return rarity; }
    public int getMaxLevel() { return maxLevel; }
    public int getBaseCost() { return baseCost; }
    public EnhancedEnchantmentRegistry.EnhancedCategory getCategory() { return category; }
}
