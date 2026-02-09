package com.enadd.enchantments.defense;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class AdaptationEnchantment extends BaseEnchantment {
    public AdaptationEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ARMOR,
            new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD},
            null, "环境适应", "减少来自特殊环境的伤害（如虚空、魔法等）");
    }
}
