package com.enadd.enchantments.combat;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class ManaBurnEnchantment extends BaseEnchantment {
    public ManaBurnEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON,
            new EquipmentSlot[]{EquipmentSlot.HAND},
            null, "法力燃烧", "攻击时燃烧目标的蓝量或产生额外魔法伤害");
    }
}
