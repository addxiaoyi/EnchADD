package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.BreakguardEnchant;
import net.enchadd.listeners.support.BreakguardDamageSupport;
import net.enchadd.listeners.support.EnchantDamageSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@SuppressWarnings("UnstableApiUsage")
public class BreakguardListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(BreakguardEnchant.KEY);
    private BreakguardEnchant config;
    private final BreakguardDamageSupport damageSupport = new BreakguardDamageSupport();

    public BreakguardListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(BreakguardEnchant.KEY);
        this.config = (enchantObj instanceof BreakguardEnchant) ? (BreakguardEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof Player target)) return;
        if (!PerformanceUtils.isSuccessfulShieldBlock(target, event)) return;

        double bonusDamage = damageSupport.resolveBonusDamage(attacker, enchant, config);
        // Shield blocking may reduce final damage to zero; use the incoming base damage.
        double adjusted = EnchantDamageSupport.addBonus(event.getDamage(), bonusDamage);
        if (!(adjusted > event.getDamage())) return;
        event.setDamage(adjusted);
    }
}
