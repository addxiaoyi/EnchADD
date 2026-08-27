package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.BackfireEnchant;
import net.enchadd.listeners.support.BackfireDamageSupport;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import net.enchadd.utils.PerformanceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class BackfireListener implements Listener {

    private Enchantment enchant;
    private NamespacedKey key;
    private BackfireEnchant config;
    private final BackfireDamageSupport damageSupport;

    public BackfireListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(BackfireEnchant.KEY),
                PerformanceUtils.namespacedKey(BackfireEnchant.KEY),
                resolveConfig(),
                new BackfireDamageSupport()
        );
    }

    public BackfireListener(@Nullable Enchantment enchant,
                            @NotNull NamespacedKey key,
                            @Nullable BackfireEnchant config,
                            @NotNull BackfireDamageSupport damageSupport) {
        this.enchant = enchant;
        this.key = key;
        this.config = config;
        this.damageSupport = damageSupport;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) {
            return;
        }
        damageSupport.apply(event, enchant, key, config);
    }

    private static @Nullable BackfireEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(BackfireEnchant.KEY);
        return (enchantObj instanceof BackfireEnchant) ? (BackfireEnchant) enchantObj : null;
    }
}
