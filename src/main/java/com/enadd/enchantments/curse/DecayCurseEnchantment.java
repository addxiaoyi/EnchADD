package com.enadd.enchantments.curse;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class DecayCurseEnchantment extends BaseEnchantment {
    @SuppressWarnings("removal")
    public DecayCurseEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ALL,
            new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD},
            null, "腐朽诅咒", "物品耐久度消耗大幅增加");
    }

    @Override
    public boolean isCursed() {
        return true;
    }
}
