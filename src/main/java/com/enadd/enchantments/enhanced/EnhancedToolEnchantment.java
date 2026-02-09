package com.enadd.enchantments.enhanced;

import com.enadd.core.api.IEnchantmentConfig;
import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


/**
 * 强化版工具附魔基类
 */
public abstract class EnhancedToolEnchantment extends BaseEnchantment {
    protected final EnhancedEnchantmentData data;

    protected EnhancedToolEnchantment(EnhancedEnchantmentData data) {
        super(
            Rarity.fromString(data.getRarity()),
            EnchantmentTarget.TOOL,
            new EquipmentSlot[]{EquipmentSlot.HAND},
            createConfig(data),
            data.getChineseName(),
            data.getChineseDescription()
        );
        this.data = data;
    }

    private static IEnchantmentConfig createConfig(EnhancedEnchantmentData data) {
        return new IEnchantmentConfig() {
            @Override
            public int getBaseCost(int level) {
                return data.getBaseCost() + (level - 1) * 10;
            }

            @Override
            public int getCostPerLevel() {
                return 10;
            }

            @Override
            public int getMaxBonus() {
                return 20;
            }

            @Override
            public int getMaxLevel() {
                return data.getMaxLevel();
            }

            @Override
            public String getRarity() {
                return data.getRarity();
            }

            @Override
            public boolean isTreasure() {
                return false;
            }
        };
    }

    @Override
    public int getMaxLevel() {
        return data.getMaxLevel();
    }
}
