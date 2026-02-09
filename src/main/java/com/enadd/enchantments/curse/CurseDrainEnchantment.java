package com.enadd.enchantments.curse;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class CurseDrainEnchantment extends BaseEnchantment {
    @SuppressWarnings("removal")
    public CurseDrainEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ALL,
            new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.OFF_HAND},
            null, "汲取诅咒", "缓慢消耗持有者的生命值或饱食度");
    }

    @Override
    public boolean isCursed() {
        return true;
    }
}
