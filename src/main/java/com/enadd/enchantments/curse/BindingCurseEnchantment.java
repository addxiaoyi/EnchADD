package com.enadd.enchantments.curse;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class BindingCurseEnchantment extends BaseEnchantment {
    @SuppressWarnings("removal")
    public BindingCurseEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ALL,
            new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD},
            null, "绑定诅咒", "物品无法从装备栏中移除");
    }

    @Override
    public boolean isCursed() {
        return true;
    }
}
