package net.enchadd.listeners;

import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.HoldfastEnchant;
import net.enchadd.listeners.support.HoldfastShieldSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@SuppressWarnings("UnstableApiUsage")
public class HoldfastListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(HoldfastEnchant.KEY);
    private NamespacedKey key = PerformanceUtils.namespacedKey(HoldfastEnchant.KEY);
    private HoldfastEnchant config;
    private final HoldfastShieldSupport shieldSupport = new HoldfastShieldSupport();

    public HoldfastListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(HoldfastEnchant.KEY);
        this.config = (enchantObj instanceof HoldfastEnchant) ? (HoldfastEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onShieldDisable(PlayerShieldDisableEvent event) {
        if (enchant == null || config == null) return;
        if (!shieldSupport.isAxeAttack(event.getDamager())) return;

        HoldfastShieldSupport.HoldfastContext context = shieldSupport.resolveContext(event.getPlayer(), enchant);
        if (context == null) return;
        if (!shieldSupport.shouldTrigger(context, key, config)) return;

        shieldSupport.apply(event, context, key);
    }
}
