package com.enadd.enchantments.enhanced.combat;

import com.enadd.enchantments.enhanced.EnhancedWeaponEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;


public class ThunderBladeEnhEnchantment extends EnhancedWeaponEnchantment {
    public ThunderBladeEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("thunder_blade_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof ThunderBladeEnhEnchantment;
    }
}
