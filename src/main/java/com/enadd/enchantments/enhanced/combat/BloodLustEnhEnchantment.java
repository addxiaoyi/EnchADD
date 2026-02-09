package com.enadd.enchantments.enhanced.combat;

import com.enadd.enchantments.enhanced.EnhancedWeaponEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;


public class BloodLustEnhEnchantment extends EnhancedWeaponEnchantment {
    public BloodLustEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("blood_lust_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof BloodLustEnhEnchantment;
    }
}
