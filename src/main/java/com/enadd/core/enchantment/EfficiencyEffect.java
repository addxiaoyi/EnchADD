package com.enadd.core.enchantment;

import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import org.bukkit.plugin.java.JavaPlugin;


public class EfficiencyEffect implements IEnchantmentEffect {

    public EfficiencyEffect(JavaPlugin plugin) {
        // Constructor accepts plugin parameter for interface compatibility
    }

    @Override
    public void apply(EffectContext context) {
        // Implementation for efficiency effect
        // This would increase mining speed
    }

    @Override
    public boolean canApply(EffectContext context) {
        // Check if the effect can be applied
        return true;
    }
}
