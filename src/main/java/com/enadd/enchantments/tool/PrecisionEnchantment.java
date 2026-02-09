package com.enadd.enchantments.tool;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class PrecisionEnchantment extends BaseEnchantment {
    public PrecisionEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.TOOL,
            new EquipmentSlot[]{EquipmentSlot.HAND},
            null, "精准", "精准采集，不破坏周围方块");
    }
}
