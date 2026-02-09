package com.enadd.enchantments.curse;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class CurseConfusionEnchantment extends BaseEnchantment {
    @SuppressWarnings("removal")
    public CurseConfusionEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ALL,
            new EquipmentSlot[]{EquipmentSlot.HEAD},
            null, "混乱诅咒", "使穿戴者感到困惑");
    }

    @Override
    public boolean isCursed() {
        return true;
    }
}
