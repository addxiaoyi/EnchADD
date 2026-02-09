package com.enadd.enchantments.armor;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class WardenBaneEnchantment extends BaseEnchantment {
    public WardenBaneEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR,
            new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD},
            null, "监守者之灾", "对监守者造成额外伤害");
    }
}
