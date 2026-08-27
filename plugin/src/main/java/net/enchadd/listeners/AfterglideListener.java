package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.AfterglideEnchant;
import net.enchadd.listeners.support.AfterglideGlideSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.potion.PotionEffect;

@SuppressWarnings("UnstableApiUsage")
public class AfterglideListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(AfterglideEnchant.KEY);
    private NamespacedKey key = PerformanceUtils.namespacedKey(AfterglideEnchant.KEY);
    private AfterglideEnchant config;
    private final AfterglideGlideSupport glideSupport = new AfterglideGlideSupport();

    public AfterglideListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(AfterglideEnchant.KEY);
        this.config = (enchantObj instanceof AfterglideEnchant) ? (AfterglideEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (event.isGliding()) return;
        if (enchant == null || config == null) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.isOnGround()) return;

        AfterglideGlideSupport.AfterglideContext context = glideSupport.resolveContext(player, enchant);
        if (context == null) return;
        if (glideSupport.isOnCooldown(context, key, config.getCooldownTicks())) return;

        PotionEffect effect = glideSupport.createSlowFallingEffect(config, context.level());
        if (effect == null) return;

        glideSupport.apply(player, effect, context, key);
    }
}
