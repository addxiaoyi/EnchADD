package com.enadd.enchantments.enhanced.combat;

import com.enadd.enchantments.enhanced.EnhancedWeaponEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;


public class BerserkerRageEnhEnchantment extends EnhancedWeaponEnchantment {
    public BerserkerRageEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("berserker_rage_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof BerserkerRageEnhEnchantment;
    }
}
