package com.enadd.enchantments.tool;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class EtherealStepEnchantment extends BaseEnchantment {
    public EtherealStepEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.TOOL,
            new EquipmentSlot[]{EquipmentSlot.HAND},
            null, "飘渺步伐", "在采集时保持轻盈，不触发陷阱");
    }
}
