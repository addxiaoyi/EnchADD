package com.enadd.enchantments.enhanced.tool;

import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;
import com.enadd.enchantments.enhanced.EnhancedToolEnchantment;


public class AutoSmeltEnhEnchantment extends EnhancedToolEnchantment {
    public AutoSmeltEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("auto_smelt_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof AutoSmeltEnhEnchantment;
    }
}
