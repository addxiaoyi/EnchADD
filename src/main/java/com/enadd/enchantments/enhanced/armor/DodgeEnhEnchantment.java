package com.enadd.enchantments.enhanced.armor;

import com.enadd.enchantments.enhanced.EnhancedArmorEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;


public class DodgeEnhEnchantment extends EnhancedArmorEnchantment {
    public DodgeEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("dodge_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof DodgeEnhEnchantment;
    }
}
