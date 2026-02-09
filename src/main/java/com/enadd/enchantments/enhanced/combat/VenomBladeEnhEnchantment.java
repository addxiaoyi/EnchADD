package com.enadd.enchantments.enhanced.combat;

import org.bukkit.enchantments.Enchantment;
import com.enadd.enchantments.enhanced.EnhancedWeaponEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;








public class VenomBladeEnhEnchantment extends EnhancedWeaponEnchantment {

    public VenomBladeEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("venom_blade_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof VenomBladeEnhEnchantment;
    }
}




