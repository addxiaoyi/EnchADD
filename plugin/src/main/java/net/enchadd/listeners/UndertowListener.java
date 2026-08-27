package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.UndertowEnchant;
import net.enchadd.listeners.support.UndertowPullSupport;
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
public class UndertowListener implements Listener {

    private final Enchantment enchant;
    private final NamespacedKey key;
    private final UndertowEnchant config;
    private final UndertowPullSupport pullSupport;

    public UndertowListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(UndertowEnchant.KEY),
                PerformanceUtils.namespacedKey(UndertowEnchant.KEY),
                resolveConfig(),
                new UndertowPullSupport()
        );
    }

    public UndertowListener(@Nullable Enchantment enchant,
                            @NotNull NamespacedKey key,
                            @Nullable UndertowEnchant config,
                            @NotNull UndertowPullSupport pullSupport) {
        this.enchant = enchant;
        this.key = key;
        this.config = config;
        this.pullSupport = pullSupport;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent event) {
        if (enchant == null) {
            return;
        }
        pullSupport.captureLaunchLevel(event, enchant, key);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (config == null) {
            return;
        }
        pullSupport.applyOnHit(event, key, config);
    }

    private static @Nullable UndertowEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(UndertowEnchant.KEY);
        return (enchantObj instanceof UndertowEnchant) ? (UndertowEnchant) enchantObj : null;
    }
}
