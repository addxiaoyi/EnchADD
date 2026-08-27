package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.FleetfootEnchant;
import net.enchadd.listeners.support.FleetfootSprintSupport;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.enchadd.utils.PerformanceUtils;

@SuppressWarnings("UnstableApiUsage")
public class FleetfootListener implements Listener {

    private final Enchantment enchant;
    private final NamespacedKey key;
    private final FleetfootEnchant config;
    private final FleetfootSprintSupport sprintSupport;

    public FleetfootListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(FleetfootEnchant.KEY),
                PerformanceUtils.namespacedKey(FleetfootEnchant.KEY),
                resolveConfig(),
                new FleetfootSprintSupport()
        );
    }

    public FleetfootListener(@Nullable Enchantment enchant,
                             @NotNull NamespacedKey key,
                             @Nullable FleetfootEnchant config,
                             @NotNull FleetfootSprintSupport sprintSupport) {
        this.enchant = enchant;
        this.key = key;
        this.config = config;
        this.sprintSupport = sprintSupport;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSprintToggle(PlayerToggleSprintEvent event) {
        if (!event.isSprinting()) {
            return;
        }
        if (enchant == null || config == null) {
            return;
        }

        Player player = event.getPlayer();
        FleetfootSprintSupport.FleetfootContext context = sprintSupport.resolveContext(player, enchant);
        if (context == null) {
            return;
        }
        if (!sprintSupport.shouldTrigger(context, key, config.getCooldownTicks())) {
            return;
        }
        sprintSupport.apply(event, context, key, config);
    }

    private static @Nullable FleetfootEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(FleetfootEnchant.KEY);
        return (enchantObj instanceof FleetfootEnchant) ? (FleetfootEnchant) enchantObj : null;
    }
}
