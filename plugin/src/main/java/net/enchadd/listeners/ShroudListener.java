package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.ShroudEnchant;
import net.enchadd.listeners.support.ShroudTargetSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class ShroudListener implements Listener {

    private final Enchantment enchant;
    private final NamespacedKey key;
    private final ShroudEnchant config;
    private final ShroudTargetSupport targetSupport;

    public ShroudListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(ShroudEnchant.KEY),
                PerformanceUtils.namespacedKey(ShroudEnchant.KEY),
                resolveConfig(),
                new ShroudTargetSupport()
        );
    }

    public ShroudListener(@Nullable Enchantment enchant,
                          @NotNull NamespacedKey key,
                          @Nullable ShroudEnchant config,
                          @NotNull ShroudTargetSupport targetSupport) {
        this.enchant = enchant;
        this.key = key;
        this.config = config;
        this.targetSupport = targetSupport;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (enchant == null || config == null) {
            return;
        }

        Player player = targetSupport.resolveEligibleTarget(event);
        if (player == null) {
            return;
        }

        ShroudTargetSupport.ShroudContext context = targetSupport.resolveContext(player, enchant);
        if (context == null) {
            return;
        }
        if (!targetSupport.shouldTrigger(context, key, config)) {
            return;
        }
        targetSupport.apply(event, context, key);
    }

    private static @Nullable ShroudEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(ShroudEnchant.KEY);
        return (enchantObj instanceof ShroudEnchant) ? (ShroudEnchant) enchantObj : null;
    }
}
