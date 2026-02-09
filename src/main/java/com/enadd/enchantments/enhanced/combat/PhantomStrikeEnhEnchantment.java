package com.enadd.enchantments.enhanced.combat;

import com.enadd.enchantments.enhanced.EnhancedWeaponEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;


public class PhantomStrikeEnhEnchantment extends EnhancedWeaponEnchantment {
    public PhantomStrikeEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("phantom_strike_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof PhantomStrikeEnhEnchantment;
    }
}
