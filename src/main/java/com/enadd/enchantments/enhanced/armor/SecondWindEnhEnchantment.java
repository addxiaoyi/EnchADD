package com.enadd.enchantments.enhanced.armor;

import com.enadd.enchantments.enhanced.EnhancedArmorEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;


public class SecondWindEnhEnchantment extends EnhancedArmorEnchantment {
    public SecondWindEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("second_wind_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof SecondWindEnhEnchantment;
    }
}
