package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.TideRunnerEnchant;
import net.enchadd.listeners.support.TideRunnerEffectSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public final class TideRunnerListener implements Listener {

    private final Enchantment enchant;
    private final TideRunnerEffectSupport effectSupport;

    public TideRunnerListener() {
        this(
                resolveEnchant(TideRunnerEnchant.KEY),
                resolveConfig(),
                PerformanceUtils.namespacedKey(TideRunnerEnchant.KEY)
        );
    }

    public TideRunnerListener(@Nullable Enchantment enchant,
                              @Nullable TideRunnerEnchant config,
                              @NotNull NamespacedKey cooldownKey) {
        this.enchant = enchant;
        this.effectSupport = config == null ? null : new TideRunnerEffectSupport(config, cooldownKey);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        if (enchant == null || effectSupport == null) {
            return;
        }
        if (!event.hasChangedPosition()) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isSwimming()) {
            return;
        }
        effectSupport.handleMove(event, player, enchant);
    }

    private static @Nullable Enchantment resolveEnchant(net.kyori.adventure.key.Key key) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return registry.get(key);
    }

    private static @Nullable TideRunnerEnchant resolveConfig() {
        EnchADDEnchant enchant = EnchADDConfig.ENCHANTS.get(TideRunnerEnchant.KEY);
        return enchant instanceof TideRunnerEnchant tideRunnerEnchant ? tideRunnerEnchant : null;
    }
}
