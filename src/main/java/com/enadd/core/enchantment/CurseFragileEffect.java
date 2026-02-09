package com.enadd.core.enchantment;

import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import org.bukkit.plugin.java.JavaPlugin;


public class CurseFragileEffect implements IEnchantmentEffect {

    public CurseFragileEffect(JavaPlugin plugin) {
        // Constructor accepts plugin parameter for interface compatibility
    }

    @Override
    public void apply(EffectContext context) {
        // Implementation for curse fragile effect
        // This would make equipment degrade faster
    }

    @Override
    public boolean canApply(EffectContext context) {
        // Check if the effect can be applied
        return true;
    }
}
