package net.enchadd.listeners;

import net.enchadd.listeners.support.ThirstEffectSupport;
import net.enchadd.listeners.support.ThirstRules;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.ThirstEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

@SuppressWarnings("UnstableApiUsage")
public class ThirstListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(ThirstEnchant.KEY);
    private final NamespacedKey combatKey = net.enchadd.utils.PerformanceUtils.namespacedKeyWithSuffix(ThirstEnchant.KEY, "_combat");
    private final ThirstEnchant config;
    private ThirstEffectSupport effectSupport;

    public ThirstListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(ThirstEnchant.KEY);
        this.config = (enchantObj instanceof ThirstEnchant) ? (ThirstEnchant) enchantObj : null;
        this.effectSupport = this.config == null ? null : new ThirstEffectSupport(this.enchant, this.config, combatKey);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCombatDamage(EntityDamageByEntityEvent event) {
        if (!ThirstRules.isCombatHit(event.isCancelled(), event.getFinalDamage())) return;
        ThirstEffectSupport effectSupport = resolveEffectSupport();
        if (enchant == null || config == null || effectSupport == null) return;
        Entity victim = event.getEntity();
        Entity damager = event.getDamager();
        if (victim instanceof Player victimPlayer) {
            effectSupport.markCombatIfApplicable(victimPlayer);
        }
        if (damager instanceof Player damagerPlayer) {
            effectSupport.markCombatIfApplicable(damagerPlayer);
        } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            effectSupport.markCombatIfApplicable(shooter);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onRegen(EntityRegainHealthEvent event) {
        ThirstEffectSupport effectSupport = resolveEffectSupport();
        if (!(event.getEntity() instanceof Player player)) return;
        if (enchant == null || config == null || effectSupport == null) return;
        EntityRegainHealthEvent.RegainReason reason = event.getRegainReason();
        if (reason != EntityRegainHealthEvent.RegainReason.REGEN && reason != EntityRegainHealthEvent.RegainReason.SATIATED) {
            return;
        }
        double amount = event.getAmount();
        effectSupport.applyRegenPenalty(player, amount, event::setAmount);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        ThirstEffectSupport effectSupport = resolveEffectSupport();
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        if (enchant == null || config == null || effectSupport == null) return;
        Integer adjusted = effectSupport.adjustedFoodLevel(player, event.getFoodLevel());
        if (adjusted != null) {
            event.setFoodLevel(adjusted);
        }
    }

    private ThirstEffectSupport resolveEffectSupport() {
        if (effectSupport == null && config != null) {
            effectSupport = new ThirstEffectSupport(enchant, config, combatKey);
        }
        return effectSupport;
    }
}
