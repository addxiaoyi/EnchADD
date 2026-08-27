package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.SteadyAimEnchant;
import net.enchadd.listeners.support.SteadyAimProjectileSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class SteadyAimListener implements Listener {

    private Enchantment enchant;
    private NamespacedKey levelKey;
    private NamespacedKey cooldownKey;
    private SteadyAimEnchant config;
    private final SteadyAimProjectileSupport projectileSupport;

    public SteadyAimListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(SteadyAimEnchant.KEY),
                PerformanceUtils.namespacedKey(SteadyAimEnchant.KEY),
                PerformanceUtils.namespacedKeyWithSuffix(SteadyAimEnchant.KEY, "_cooldown"),
                resolveConfig(),
                new SteadyAimProjectileSupport()
        );
    }

    public SteadyAimListener(@Nullable Enchantment enchant,
                             @NotNull NamespacedKey levelKey,
                             @NotNull NamespacedKey cooldownKey,
                             @Nullable SteadyAimEnchant config,
                             @NotNull SteadyAimProjectileSupport projectileSupport) {
        this.enchant = enchant;
        this.levelKey = levelKey;
        this.cooldownKey = cooldownKey;
        this.config = config;
        this.projectileSupport = projectileSupport;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(ProjectileLaunchEvent event) {
        if (enchant == null || config == null) {
            return;
        }
        projectileSupport.captureLaunchLevel(event, enchant, levelKey, cooldownKey, config);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (config == null) {
            return;
        }
        projectileSupport.applyBonusDamage(event, levelKey, config);
    }

    private static @Nullable SteadyAimEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(SteadyAimEnchant.KEY);
        return (enchantObj instanceof SteadyAimEnchant) ? (SteadyAimEnchant) enchantObj : null;
    }
}
