package com.enadd.enchantments.enhanced.armor;

import com.enadd.enchantments.enhanced.EnhancedArmorEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;


public class AegisEnhEnchantment extends EnhancedArmorEnchantment {
    public AegisEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("aegis_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof AegisEnhEnchantment;
    }
}
