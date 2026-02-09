package com.enadd.enchantments.curse;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class CurseBindingPlusEnchantment extends BaseEnchantment {
    @SuppressWarnings("removal")
    public CurseBindingPlusEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ALL,
            new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD},
            null, "高级绑定诅咒", "物品更难从装备栏中移除");
    }

    @Override
    public boolean isCursed() {
        return true;
    }
}
