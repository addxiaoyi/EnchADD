package com.enadd.enchantments.defense;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class ImmunityEnchantment extends BaseEnchantment {
    public ImmunityEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR,
            new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD},
            null, "免疫", "概率免疫负面效果");
    }
}
