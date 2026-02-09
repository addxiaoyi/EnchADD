package com.enadd.enchantments.enhanced.tool;

import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;
import com.enadd.enchantments.enhanced.EnhancedToolEnchantment;


public class BuilderEnhEnchantment extends EnhancedToolEnchantment {
    public BuilderEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("builder_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof BuilderEnhEnchantment;
    }
}
