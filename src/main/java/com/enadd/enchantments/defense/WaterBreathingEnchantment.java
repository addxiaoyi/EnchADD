package com.enadd.enchantments.defense;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class WaterBreathingEnchantment extends BaseEnchantment {
    public WaterBreathingEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR,
            new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD},
            null, "水下呼吸", "获得水下呼吸效果");
    }
}
