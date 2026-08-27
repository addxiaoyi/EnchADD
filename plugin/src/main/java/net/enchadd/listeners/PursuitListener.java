package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.PursuitEnchant;
import net.enchadd.listeners.support.PursuitHitSupport;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class PursuitListener implements Listener {

    private Enchantment enchant;
    private PursuitEnchant config;
    private final PursuitHitSupport hitSupport;

    public PursuitListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(PursuitEnchant.KEY),
                resolveConfig(),
                new PursuitHitSupport()
        );
    }

    public PursuitListener(@Nullable Enchantment enchant,
                           @Nullable PursuitEnchant config,
                           @NotNull PursuitHitSupport hitSupport) {
        this.enchant = enchant;
        this.config = config;
        this.hitSupport = hitSupport;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) {
            return;
        }
        hitSupport.apply(event, enchant, config);
    }

    private static @Nullable PursuitEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(PursuitEnchant.KEY);
        return (enchantObj instanceof PursuitEnchant) ? (PursuitEnchant) enchantObj : null;
    }
}
