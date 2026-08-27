package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.FreshcatchEnchant;
import net.enchadd.listeners.support.FreshcatchRestoreSupport;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public final class FreshcatchListener implements Listener {

    private final Enchantment enchant;
    private final FreshcatchRestoreSupport restoreSupport;

    public FreshcatchListener() {
        this(resolveEnchant(FreshcatchEnchant.KEY), resolveConfig());
    }

    public FreshcatchListener(@Nullable Enchantment enchant, @Nullable FreshcatchEnchant config) {
        this.enchant = enchant;
        this.restoreSupport = config == null ? null : new FreshcatchRestoreSupport(config);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFish(PlayerFishEvent event) {
        if (enchant == null || restoreSupport == null) {
            return;
        }
        restoreSupport.handleFish(event, enchant);
    }

    private static @Nullable Enchantment resolveEnchant(net.kyori.adventure.key.Key key) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return registry.get(key);
    }

    private static @Nullable FreshcatchEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(FreshcatchEnchant.KEY);
        return enchantObj instanceof FreshcatchEnchant freshcatchEnchant ? freshcatchEnchant : null;
    }
}
