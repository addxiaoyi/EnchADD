package com.enadd.enchantments.combat;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class GrimHarvestEnchantment extends BaseEnchantment {
    public GrimHarvestEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON,
            new EquipmentSlot[]{EquipmentSlot.HAND},
            null, "残酷收割", "击杀生物时获得额外的经验 and 掉落");
    }
}
