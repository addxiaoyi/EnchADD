package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.MeteorEnchant;
import net.enchadd.listeners.support.MeteorHitSupport;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class MeteorListener implements Listener {

    private Enchantment enchant;
    private MeteorEnchant config;
    private final MeteorHitSupport hitSupport;

    public MeteorListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(MeteorEnchant.KEY),
                resolveConfig(),
                new MeteorHitSupport()
        );
    }

    public MeteorListener(@Nullable Enchantment enchant,
                          @Nullable MeteorEnchant config,
                          @NotNull MeteorHitSupport hitSupport) {
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

    private static @Nullable MeteorEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(MeteorEnchant.KEY);
        return (enchantObj instanceof MeteorEnchant) ? (MeteorEnchant) enchantObj : null;
    }
}
