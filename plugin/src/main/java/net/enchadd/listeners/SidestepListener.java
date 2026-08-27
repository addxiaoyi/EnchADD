package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.SidestepEnchant;
import net.enchadd.listeners.support.SidestepDodgeSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SidestepListener implements Listener {

    private final Enchantment enchant;
    private final NamespacedKey key;
    private final SidestepEnchant config;
    private final SidestepDodgeSupport dodgeSupport;

    public SidestepListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(SidestepEnchant.KEY),
                PerformanceUtils.namespacedKey(SidestepEnchant.KEY),
                resolveConfig(),
                new SidestepDodgeSupport()
        );
    }

    public SidestepListener(@Nullable Enchantment enchant,
                            @NotNull NamespacedKey key,
                            @Nullable SidestepEnchant config,
                            @NotNull SidestepDodgeSupport dodgeSupport) {
        this.enchant = enchant;
        this.key = key;
        this.config = config;
        this.dodgeSupport = dodgeSupport;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onProjectileHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!(event.getDamager() instanceof Projectile)) {
            return;
        }

        SidestepDodgeSupport.SidestepContext context = dodgeSupport.resolveContext(player, enchant);
        if (context == null) {
            return;
        }
        if (!dodgeSupport.shouldTrigger(context, key, config)) {
            return;
        }
        dodgeSupport.apply(event, player, context, key, config);
    }

    private static @Nullable SidestepEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(SidestepEnchant.KEY);
        return (enchantObj instanceof SidestepEnchant) ? (SidestepEnchant) enchantObj : null;
    }
}
