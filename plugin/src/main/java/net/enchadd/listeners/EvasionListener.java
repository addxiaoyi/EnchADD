package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EvasionEnchant;
import net.enchadd.listeners.support.EvasionProjectileSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvasionListener implements Listener {

    private final Enchantment enchant;
    private final EvasionEnchant config;
    private final NamespacedKey cooldownKey;
    private final EvasionProjectileSupport projectileSupport;

    public EvasionListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(EvasionEnchant.KEY),
                resolveConfig(),
                PerformanceUtils.namespacedKeyWithSuffix(EvasionEnchant.KEY, "_cooldown"),
                new EvasionProjectileSupport()
        );
    }

    public EvasionListener(@Nullable Enchantment enchant,
                           @Nullable EvasionEnchant config,
                           @NotNull NamespacedKey cooldownKey,
                           @NotNull EvasionProjectileSupport projectileSupport) {
        this.enchant = enchant;
        this.config = config;
        this.cooldownKey = cooldownKey;
        this.projectileSupport = projectileSupport;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onProjectile(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof AbstractArrow)) return;
        if (enchant == null || config == null) return;

        int level = projectileSupport.resolveLevel(victim, enchant);
        if (level <= 0) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(victim);
        if (pdc == null) return;
        if (PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) return;
        if (!projectileSupport.shouldTrigger(config, level)) return;

        projectileSupport.apply(event, victim);
        PerformanceUtils.setCooldown(pdc, cooldownKey);
    }

    private static @Nullable EvasionEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(EvasionEnchant.KEY);
        return (enchantObj instanceof EvasionEnchant) ? (EvasionEnchant) enchantObj : null;
    }
}
