package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.ResonanceEnchant;
import net.enchadd.listeners.support.ResonanceComboSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class ResonanceListener implements Listener {

    private Enchantment enchant;
    private NamespacedKey comboKey;
    private ResonanceEnchant config;
    private final ResonanceComboSupport comboSupport;

    public ResonanceListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(ResonanceEnchant.KEY),
                PerformanceUtils.namespacedKeyWithSuffix(ResonanceEnchant.KEY, "_combo"),
                resolveConfig(),
                new ResonanceComboSupport()
        );
    }

    public ResonanceListener(@Nullable Enchantment enchant,
                             @NotNull NamespacedKey comboKey,
                             @Nullable ResonanceEnchant config,
                             @NotNull ResonanceComboSupport comboSupport) {
        this.enchant = enchant;
        this.comboKey = comboKey;
        this.config = config;
        this.comboSupport = comboSupport;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) {
            return;
        }
        comboSupport.apply(event, enchant, comboKey, config);
    }

    private static @Nullable ResonanceEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(ResonanceEnchant.KEY);
        return (enchantObj instanceof ResonanceEnchant) ? (ResonanceEnchant) enchantObj : null;
    }
}
