package com.enadd.core.enchantment;

import java.util.HashSet;
import java.util.Set;


/**
 * 附魔数据类 - 存储附魔的完整信息
 *
 * @author ADDXIAOYI2048
 * @version 1.0.0
 * @since 1.0.0
 */
public class EnchantmentData {

    /** 附魔ID */
    private final String id;

    /** 附魔名称 */
    private final String name;

    /** 附魔描述 */
    private final String description;

    /** 附魔类型 */
    private final String type;

    /** 稀有度 */
    private final String rarity;

    /** 最大等级 */
    private final int maxLevel;

    /** 冲突附魔列表 */
    private final Set<String> conflicts;

    /** 适用的物品类型 */
    private final Set<String> applicableItems;

    /** 是否启用 */
    private boolean enabled;

    public EnchantmentData(String id, String name, String description, String type,
                          String rarity, int maxLevel) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.rarity = rarity;
        this.maxLevel = maxLevel;
        this.conflicts = new HashSet<>();
        this.applicableItems = new HashSet<>();
        this.enabled = true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getRarity() {
        return rarity;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public Set<String> getConflicts() {
        return new HashSet<>(conflicts);
    }

    public void addConflict(String enchantmentId) {
        conflicts.add(enchantmentId);
    }

    public void removeConflict(String enchantmentId) {
        conflicts.remove(enchantmentId);
    }

    public Set<String> getApplicableItems() {
        return new HashSet<>(applicableItems);
    }

    public void addApplicableItem(String itemType) {
        applicableItems.add(itemType);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 检查是否可以应用到指定物品
     *
     * @param itemType 物品类型
     * @return 是否可以应用
     */
    public boolean canApplyTo(String itemType) {
        return applicableItems.isEmpty() || applicableItems.contains(itemType);
    }

    @Override
    public String toString() {
        return String.format("EnchantmentData[id=%s, name=%s, type=%s, rarity=%s, maxLevel=%d]",
            id, name, type, rarity, maxLevel);
    }
}
