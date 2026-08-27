package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.MisfortuneEnchant;
import net.enchadd.listeners.support.MisfortuneExpSupport;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import net.enchadd.utils.PerformanceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class MisfortuneListener implements Listener {

    private Enchantment enchant;
    private MisfortuneEnchant config;
    private final MisfortuneExpSupport expSupport;

    public MisfortuneListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(MisfortuneEnchant.KEY),
                resolveConfig(),
                new MisfortuneExpSupport()
        );
    }

    public MisfortuneListener(@Nullable Enchantment enchant,
                              @Nullable MisfortuneEnchant config,
                              @NotNull MisfortuneExpSupport expSupport) {
        this.enchant = enchant;
        this.config = config;
        this.expSupport = expSupport;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (enchant == null || config == null) {
            return;
        }
        expSupport.apply(event, enchant, config);
    }

    private static @Nullable MisfortuneEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(MisfortuneEnchant.KEY);
        return (enchantObj instanceof MisfortuneEnchant) ? (MisfortuneEnchant) enchantObj : null;
    }
}

