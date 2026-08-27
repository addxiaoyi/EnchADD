package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.HomewardEnchant;
import net.enchadd.listeners.support.HomewardEscapeSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

@SuppressWarnings("UnstableApiUsage")
public class HomewardListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(HomewardEnchant.KEY);
    private NamespacedKey cooldownKey = PerformanceUtils.namespacedKeyWithSuffix(HomewardEnchant.KEY, "_cooldown");
    private NamespacedKey windowKey = PerformanceUtils.namespacedKeyWithSuffix(HomewardEnchant.KEY, "_window");
    private NamespacedKey levelKey = PerformanceUtils.namespacedKeyWithSuffix(HomewardEnchant.KEY, "_level");
    private final HomewardEnchant config;
    private final HomewardEscapeSupport escapeSupport;

    public HomewardListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(HomewardEnchant.KEY);
        this.config = (enchantObj instanceof HomewardEnchant) ? (HomewardEnchant) enchantObj : null;
        this.escapeSupport = this.config == null ? null : new HomewardEscapeSupport(config, cooldownKey, windowKey, levelKey);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCombatDamage(EntityDamageByEntityEvent event) {
        if (enchant == null || escapeSupport == null) {
            return;
        }
        escapeSupport.handleCombatDamage(event, enchant);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFallDamage(EntityDamageEvent event) {
        if (enchant == null || escapeSupport == null) {
            return;
        }
        escapeSupport.handleFallDamage(event);
    }
}
