package com.enadd.enchantments.enhanced.combat;

import org.bukkit.enchantments.Enchantment;
import com.enadd.enchantments.enhanced.EnhancedWeaponEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;








public class LifeStealEnhEnchantment extends EnhancedWeaponEnchantment {

    public LifeStealEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("lifesteal_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof LifeStealEnhEnchantment;
    }
}




