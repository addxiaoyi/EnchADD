package com.enadd.enchantments.combat;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class PhantomStrikeEnchantment extends BaseEnchantment {
    public PhantomStrikeEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON,
            new EquipmentSlot[]{EquipmentSlot.HAND},
            null, "幻影打击", "攻击时产生幻影，造成额外一次伤害");
    }
}
