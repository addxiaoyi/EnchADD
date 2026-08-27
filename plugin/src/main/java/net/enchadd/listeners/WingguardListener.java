package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.WingguardEnchant;
import net.enchadd.listeners.support.WingguardRescueSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class WingguardListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(WingguardEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(WingguardEnchant.KEY);
    private final WingguardEnchant config;
    private final WingguardRescueSupport rescueSupport = new WingguardRescueSupport();

    public WingguardListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(WingguardEnchant.KEY);
        this.config = (enchantObj instanceof WingguardEnchant) ? (WingguardEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFallDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.FALL && cause != EntityDamageEvent.DamageCause.FLY_INTO_WALL) return;
        if (!(entity instanceof Player player)) return;
        if (enchant == null || config == null) return;

        WingguardRescueSupport.WingguardContext context = rescueSupport.resolveContext(player, enchant);
        if (context == null) return;
        if (!rescueSupport.isLethal(player, event.getFinalDamage())) return;
        if (!rescueSupport.shouldTrigger(context, key, config)) return;

        rescueSupport.applyRescue(player, context, key, config);
        event.setCancelled(true);
    }
}
