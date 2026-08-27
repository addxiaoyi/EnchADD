package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.FlareEnchant;
import net.enchadd.listeners.support.FlareProjectileSupport;
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
public class FlareListener implements Listener {

    private Enchantment enchant;
    private NamespacedKey levelKey;
    private NamespacedKey cooldownKey;
    private FlareEnchant config;
    private final FlareProjectileSupport projectileSupport;

    public FlareListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(FlareEnchant.KEY),
                PerformanceUtils.namespacedKey(FlareEnchant.KEY),
                PerformanceUtils.namespacedKeyWithSuffix(FlareEnchant.KEY, "_cooldown"),
                resolveConfig(),
                new FlareProjectileSupport()
        );
    }

    public FlareListener(@Nullable Enchantment enchant,
                         @NotNull NamespacedKey levelKey,
                         @NotNull NamespacedKey cooldownKey,
                         @Nullable FlareEnchant config,
                         @NotNull FlareProjectileSupport projectileSupport) {
        this.enchant = enchant;
        this.levelKey = levelKey;
        this.cooldownKey = cooldownKey;
        this.config = config;
        this.projectileSupport = projectileSupport;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent event) {
        if (enchant == null) {
            return;
        }
        projectileSupport.captureLaunchLevel(event, enchant, levelKey);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (config == null) {
            return;
        }
        projectileSupport.applyOnDamage(event, levelKey, cooldownKey, config);
    }

    private static @Nullable FlareEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(FlareEnchant.KEY);
        return (enchantObj instanceof FlareEnchant) ? (FlareEnchant) enchantObj : null;
    }
}
