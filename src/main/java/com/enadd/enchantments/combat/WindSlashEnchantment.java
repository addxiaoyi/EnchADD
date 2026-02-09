package com.enadd.enchantments.combat;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class WindSlashEnchantment extends BaseEnchantment {
    public WindSlashEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON,
            new EquipmentSlot[]{EquipmentSlot.HAND},
            null, "风刃", "攻击时产生向前的风刃波");
    }
}
