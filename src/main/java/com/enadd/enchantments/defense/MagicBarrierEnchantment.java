package com.enadd.enchantments.defense;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class MagicBarrierEnchantment extends BaseEnchantment {
    public MagicBarrierEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR,
            new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD},
            null, "魔法屏障", "减少来自魔法和药水的伤害");
    }
}
