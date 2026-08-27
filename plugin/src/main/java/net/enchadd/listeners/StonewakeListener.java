package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.StonewakeEnchant;
import net.enchadd.listeners.support.StonewakeEffectSupport;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public final class StonewakeListener implements Listener {

    private final Enchantment enchant;
    private final StonewakeEffectSupport effectSupport;

    public StonewakeListener() {
        this(resolveEnchant(StonewakeEnchant.KEY), resolveConfig());
    }

    public StonewakeListener(@Nullable Enchantment enchant, @Nullable StonewakeEnchant config) {
        this.enchant = enchant;
        this.effectSupport = config == null ? null : new StonewakeEffectSupport(config);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (enchant == null || effectSupport == null) {
            return;
        }
        effectSupport.handleBlockBreak(event, enchant);
    }

    private static @Nullable Enchantment resolveEnchant(net.kyori.adventure.key.Key key) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return registry.get(key);
    }

    private static @Nullable StonewakeEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(StonewakeEnchant.KEY);
        return enchantObj instanceof StonewakeEnchant stonewakeEnchant ? stonewakeEnchant : null;
    }
}
