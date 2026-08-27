package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.SteadfastEnchant;
import net.enchadd.listeners.support.SteadfastKnockbackSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class SteadfastListener implements Listener {

    private final Enchantment enchant;
    private final NamespacedKey key;
    private final SteadfastEnchant config;
    private final SteadfastKnockbackSupport knockbackSupport;

    public SteadfastListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(SteadfastEnchant.KEY),
                PerformanceUtils.namespacedKey(SteadfastEnchant.KEY),
                resolveConfig(),
                new SteadfastKnockbackSupport()
        );
    }

    public SteadfastListener(@Nullable Enchantment enchant,
                             @NotNull NamespacedKey key,
                             @Nullable SteadfastEnchant config,
                             @NotNull SteadfastKnockbackSupport knockbackSupport) {
        this.enchant = enchant;
        this.key = key;
        this.config = config;
        this.knockbackSupport = knockbackSupport;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onKnockback(EntityKnockbackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (enchant == null || config == null) return;

        SteadfastKnockbackSupport.SteadfastContext context = knockbackSupport.resolveContext(player, enchant);
        if (context == null) return;
        if (knockbackSupport.isOnCooldown(context, key, config.getCooldownTicks())) return;
        if (!knockbackSupport.shouldTrigger(config, context.level())) return;
        if (!knockbackSupport.applyKnockbackReduction(event, config, context.level())) return;

        knockbackSupport.triggerCooldown(context, key);
    }

    private static @Nullable SteadfastEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(SteadfastEnchant.KEY);
        return (enchantObj instanceof SteadfastEnchant) ? (SteadfastEnchant) enchantObj : null;
    }
}
