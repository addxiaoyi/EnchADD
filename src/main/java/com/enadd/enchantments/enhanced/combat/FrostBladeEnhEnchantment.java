package com.enadd.enchantments.enhanced.combat;

import com.enadd.enchantments.enhanced.EnhancedWeaponEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;


public class FrostBladeEnhEnchantment extends EnhancedWeaponEnchantment {
    public FrostBladeEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("frost_blade_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof FrostBladeEnhEnchantment;
    }
}
