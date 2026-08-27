package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.ObscureEnchant;
import net.enchadd.listeners.support.ObscureProjectileSupport;
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
public class ObscureListener implements Listener {

    private Enchantment enchant;
    private NamespacedKey key;
    private ObscureEnchant config;
    private final ObscureProjectileSupport projectileSupport;

    public ObscureListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(ObscureEnchant.KEY),
                PerformanceUtils.namespacedKey(ObscureEnchant.KEY),
                resolveConfig(),
                new ObscureProjectileSupport()
        );
    }

    public ObscureListener(@Nullable Enchantment enchant,
                           @NotNull NamespacedKey key,
                           @Nullable ObscureEnchant config,
                           @NotNull ObscureProjectileSupport projectileSupport) {
        this.enchant = enchant;
        this.key = key;
        this.config = config;
        this.projectileSupport = projectileSupport;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent event) {
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

    private static @Nullable ObscureEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(ObscureEnchant.KEY);
        return (enchantObj instanceof ObscureEnchant) ? (ObscureEnchant) enchantObj : null;
    }
}
