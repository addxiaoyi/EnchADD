package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.BulwarkEnchant;
import net.enchadd.listeners.support.BulwarkShieldSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;

@SuppressWarnings("UnstableApiUsage")
public class BulwarkListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(BulwarkEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(BulwarkEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final BulwarkEnchant config;
    private final BulwarkShieldSupport shieldSupport = new BulwarkShieldSupport();

    public BulwarkListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(BulwarkEnchant.KEY);
        this.config = (enchantObj instanceof BulwarkEnchant) ? (BulwarkEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onShieldBlock(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!PerformanceUtils.isSuccessfulShieldBlock(player, event)) return;

        BulwarkShieldSupport.BulwarkContext context = shieldSupport.resolveContext(player, enchant);
        if (context == null) return;
        if (shieldSupport.isOnCooldown(context, key, config.getCooldownTicks())) return;

        PotionEffect effect = shieldSupport.createResistanceEffect(config, context.level());
        shieldSupport.apply(player, effect, context, key);
    }
}
