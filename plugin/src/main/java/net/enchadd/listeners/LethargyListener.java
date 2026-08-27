package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.LethargyEnchant;
import net.enchadd.listeners.support.LethargySprintSupport;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import net.enchadd.utils.PerformanceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class LethargyListener implements Listener {

    private Enchantment enchant;
    private NamespacedKey key;
    private LethargyEnchant config;
    private final LethargySprintSupport sprintSupport;

    public LethargyListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(LethargyEnchant.KEY),
                PerformanceUtils.namespacedKey(LethargyEnchant.KEY),
                resolveConfig(),
                new LethargySprintSupport()
        );
    }

    public LethargyListener(@Nullable Enchantment enchant,
                            @NotNull NamespacedKey key,
                            @Nullable LethargyEnchant config,
                            @NotNull LethargySprintSupport sprintSupport) {
        this.enchant = enchant;
        this.key = key;
        this.config = config;
        this.sprintSupport = sprintSupport;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSprintToggle(PlayerToggleSprintEvent event) {
        if (enchant == null || config == null) {
            return;
        }
        sprintSupport.apply(event, enchant, key, config);
    }

    private static @Nullable LethargyEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(LethargyEnchant.KEY);
        return (enchantObj instanceof LethargyEnchant) ? (LethargyEnchant) enchantObj : null;
    }
}

