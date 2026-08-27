package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.FragilityEnchant;
import net.enchadd.listeners.support.FragilityDamageSupport;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import net.enchadd.utils.PerformanceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class FragilityListener implements Listener {

    private Enchantment enchant;
    private FragilityEnchant config;
    private final FragilityDamageSupport damageSupport;

    public FragilityListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(FragilityEnchant.KEY),
                resolveConfig(),
                new FragilityDamageSupport()
        );
    }

    public FragilityListener(@Nullable Enchantment enchant,
                             @Nullable FragilityEnchant config,
                             @NotNull FragilityDamageSupport damageSupport) {
        this.enchant = enchant;
        this.config = config;
        this.damageSupport = damageSupport;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (enchant == null || config == null) {
            return;
        }
        damageSupport.apply(event, enchant, config);
    }

    private static @Nullable FragilityEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(FragilityEnchant.KEY);
        return (enchantObj instanceof FragilityEnchant) ? (FragilityEnchant) enchantObj : null;
    }
}
