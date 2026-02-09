package com.enadd.enchantments.combat;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class BerserkerRageEnchantment extends BaseEnchantment {
    public BerserkerRageEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON,
            new EquipmentSlot[]{EquipmentSlot.HAND},
            null, "狂战士之怒", "受到的伤害越多攻击力越高");
    }
}
