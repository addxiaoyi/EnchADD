package com.enadd.enchantments.armor;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class AegisArmorEnchantment extends BaseEnchantment {
    public AegisArmorEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR,
            new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD},
            null, "神盾护甲", "周期性抵挡一次伤害");
    }
}
