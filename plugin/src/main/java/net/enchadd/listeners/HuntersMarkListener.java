package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.HuntersMarkEnchant;
import net.enchadd.listeners.support.HuntersMarkProjectileSupport;
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
public class HuntersMarkListener implements Listener {

    private Enchantment enchant;
    private NamespacedKey key;
    private HuntersMarkEnchant config;
    private final HuntersMarkProjectileSupport projectileSupport;

    public HuntersMarkListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(HuntersMarkEnchant.KEY),
                PerformanceUtils.namespacedKey(HuntersMarkEnchant.KEY),
                resolveConfig(),
                new HuntersMarkProjectileSupport()
        );
    }

    public HuntersMarkListener(@Nullable Enchantment enchant,
                               @NotNull NamespacedKey key,
                               @Nullable HuntersMarkEnchant config,
                               @NotNull HuntersMarkProjectileSupport projectileSupport) {
        this.enchant = enchant;
        this.key = key;
        this.config = config;
        this.projectileSupport = projectileSupport;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(ProjectileLaunchEvent event) {
        if (enchant == null) {
            return;
        }
        projectileSupport.captureLaunchLevel(event, enchant, key);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (config == null) {
            return;
        }
        projectileSupport.applyOnHit(event, key, config);
    }

    private static @Nullable HuntersMarkEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(HuntersMarkEnchant.KEY);
        return (enchantObj instanceof HuntersMarkEnchant) ? (HuntersMarkEnchant) enchantObj : null;
    }
}
