package com.enadd.enchantments.defense;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class CounterAttackEnchantment extends BaseEnchantment {
    public CounterAttackEnchantment() {
        super(Rarity.EPIC, EnchantmentTarget.ARMOR,
            new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD},
            null, "防守反击", "受到伤害时有概率对攻击者造成反击伤害");
    }
}
