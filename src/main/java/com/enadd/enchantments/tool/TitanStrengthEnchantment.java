package com.enadd.enchantments.tool;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class TitanStrengthEnchantment extends BaseEnchantment {
    public TitanStrengthEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.TOOL,
            new EquipmentSlot[]{EquipmentSlot.HAND},
            null, "泰坦之力", "极大提升力量，可挖掘极坚硬方块");
    }
}
