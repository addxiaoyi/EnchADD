package com.enadd.enchantments.curse;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class VanishingCurseEnchantment extends BaseEnchantment {
    @SuppressWarnings("removal")
    public VanishingCurseEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ALL,
            new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD},
            null, "消失诅咒", "死亡时物品直接消失");
    }

    @Override
    public boolean isCursed() {
        return true;
    }
}
