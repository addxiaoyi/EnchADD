package com.enadd.enchantments.enhanced.combat;

import com.enadd.enchantments.enhanced.EnhancedWeaponEnchantment;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;


public class VoidSlashEnhEnchantment extends EnhancedWeaponEnchantment {
    public VoidSlashEnhEnchantment() {
        super(EnhancedEnchantmentRegistry.getEnchantmentData("void_slash_enh"));
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other instanceof VoidSlashEnhEnchantment;
    }
}
