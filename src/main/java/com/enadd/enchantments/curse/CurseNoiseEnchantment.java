package com.enadd.enchantments.curse;

import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;


public class CurseNoiseEnchantment extends BaseEnchantment {
    @SuppressWarnings("removal")
    public CurseNoiseEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ALL,
            new EquipmentSlot[]{EquipmentSlot.HEAD},
            null, "噪音诅咒", "使穿戴者听到奇怪的声音");
    }

    @Override
    public boolean isCursed() {
        return true;
    }
}
