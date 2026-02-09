package com.enadd.enchantments.enhanced.combat;

import org.bukkit.enchantments.Enchantment;
import com.enadd.enchantments.enhanced.EnhancedWeaponEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;








public class CriticalStrikeEnhEnchantment extends EnhancedWeaponEnchantment {

    public CriticalStrikeEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("critical_strike_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof CriticalStrikeEnhEnchantment;
    }
}




