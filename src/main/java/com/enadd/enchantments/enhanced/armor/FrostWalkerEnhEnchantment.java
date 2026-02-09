package com.enadd.enchantments.enhanced.armor;

import com.enadd.enchantments.enhanced.EnhancedArmorEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;


public class FrostWalkerEnhEnchantment extends EnhancedArmorEnchantment {
    public FrostWalkerEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("frost_walker_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof FrostWalkerEnhEnchantment;
    }
}
