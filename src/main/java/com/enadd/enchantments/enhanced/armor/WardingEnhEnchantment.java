package com.enadd.enchantments.enhanced.armor;

import com.enadd.enchantments.enhanced.EnhancedArmorEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;


public class WardingEnhEnchantment extends EnhancedArmorEnchantment {
    public WardingEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("warding_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof WardingEnhEnchantment;
    }
}
