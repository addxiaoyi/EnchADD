package com.enadd.core.enchantment;

import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import org.bukkit.plugin.java.JavaPlugin;


public class ExplosiveArrowEffect implements IEnchantmentEffect {

    public ExplosiveArrowEffect(JavaPlugin plugin) {
        // Constructor accepts plugin parameter for interface compatibility
    }

    @Override
    public void apply(EffectContext context) {
        // Implementation for explosive arrow effect
        // This would make arrows explode on impact
    }

    @Override
    public boolean canApply(EffectContext context) {
        // Check if the effect can be applied
        return true;
    }
}
