package com.enadd.enchantments.tool;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class CatapultEnchantment extends BaseEnchantment {
    public CatapultEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.TOOL,
            new EquipmentSlot[]{EquipmentSlot.HAND},
            null, "弹射", "将采集到的物品弹射向指定方向");
    }
}
