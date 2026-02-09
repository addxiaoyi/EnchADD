package com.enadd.enchantments.enhanced.combat;

import org.bukkit.enchantments.Enchantment;
import com.enadd.enchantments.enhanced.EnhancedWeaponEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;








public class ExplosiveArrowEnhEnchantment extends EnhancedWeaponEnchantment {

    public ExplosiveArrowEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("explosive_arrow_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof ExplosiveArrowEnhEnchantment;
    }
}




