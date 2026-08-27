package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.BindEnchant;
import net.enchadd.listeners.support.BindProjectileSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class BindListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(BindEnchant.KEY);
    private NamespacedKey key = PerformanceUtils.namespacedKey(BindEnchant.KEY);
    private BindEnchant config;

    public BindListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(BindEnchant.KEY);
        this.config = (enchantObj instanceof BindEnchant) ? (BindEnchant) enchantObj : null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(ProjectileLaunchEvent event) {
        BindProjectileSupport support = resolveSupport();
        if (enchant == null || support == null) {
            return;
        }
        support.handleShoot(event, enchant);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        BindProjectileSupport support = resolveSupport();
        if (enchant == null || support == null) {
            return;
        }
        support.handleHit(event);
    }

    private @Nullable BindProjectileSupport resolveSupport() {
        if (config == null) {
            return null;
        }
        return new BindProjectileSupport(config, key);
    }
}
