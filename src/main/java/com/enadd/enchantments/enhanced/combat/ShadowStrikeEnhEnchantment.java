package com.enadd.enchantments.enhanced.combat;

import com.enadd.enchantments.enhanced.EnhancedWeaponEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;


public class ShadowStrikeEnhEnchantment extends EnhancedWeaponEnchantment {
    public ShadowStrikeEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("shadow_strike_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof ShadowStrikeEnhEnchantment;
    }
}
