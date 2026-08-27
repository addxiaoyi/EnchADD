package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.PivotEnchant;
import net.enchadd.listeners.support.PivotMomentumSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;

@SuppressWarnings("UnstableApiUsage")
public class PivotListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(PivotEnchant.KEY);
    private NamespacedKey key = PerformanceUtils.namespacedKey(PivotEnchant.KEY);
    private PivotEnchant config;
    private final PivotMomentumSupport momentumSupport = new PivotMomentumSupport();

    public PivotListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(PivotEnchant.KEY);
        this.config = (enchantObj instanceof PivotEnchant) ? (PivotEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onShieldBlock(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!PerformanceUtils.isSuccessfulShieldBlock(player, event)) return;

        PivotMomentumSupport.PivotContext pivotContext = momentumSupport.resolveContext(player, enchant);
        if (pivotContext == null) return;
        if (momentumSupport.isOnCooldown(pivotContext, key, config.getCooldownTicks())) return;

        PotionEffect effect = momentumSupport.createSpeedEffect(config, pivotContext.level());
        if (effect == null) return;

        momentumSupport.apply(player, effect, pivotContext, key);
    }
}
