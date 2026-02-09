package com.enadd.enchantments.tool;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class MinerEnchantment extends BaseEnchantment {
    public MinerEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.TOOL,
            new EquipmentSlot[]{EquipmentSlot.HAND},
            null, "矿工", "提升在矿洞中的采集效率");
    }
}
