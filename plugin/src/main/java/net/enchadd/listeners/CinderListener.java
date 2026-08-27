package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.CinderEnchant;
import net.enchadd.listeners.support.CinderHitSupport;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class CinderListener implements Listener {

    private Enchantment enchant;
    private CinderEnchant config;
    private final CinderHitSupport hitSupport;

    public CinderListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(CinderEnchant.KEY),
                resolveConfig(),
                new CinderHitSupport()
        );
    }

    public CinderListener(@Nullable Enchantment enchant,
                          @Nullable CinderEnchant config,
                          @NotNull CinderHitSupport hitSupport) {
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

    private static @Nullable CinderEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(CinderEnchant.KEY);
        return (enchantObj instanceof CinderEnchant) ? (CinderEnchant) enchantObj : null;
    }
}
