package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.RiposteEnchant;
import net.enchadd.listeners.support.RiposteRetaliationSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;

@SuppressWarnings("UnstableApiUsage")
public class RiposteListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(RiposteEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(RiposteEnchant.KEY);

    // 性能优化: 缓存配置对象
    private final RiposteEnchant config;
    private final RiposteRetaliationSupport retaliationSupport = new RiposteRetaliationSupport();

    public RiposteListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(RiposteEnchant.KEY);
        this.config = (enchantObj instanceof RiposteEnchant) ? (RiposteEnchant) enchantObj : null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlocked(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;

        if (!PerformanceUtils.isSuccessfulShieldBlock(victim, event)) return;

        RiposteRetaliationSupport.RiposteContext context = retaliationSupport.resolveContext(victim, attacker, enchant);
        if (context == null) return;
        if (retaliationSupport.isOnCooldown(context, key, config.getCooldownTicks())) return;
        if (!retaliationSupport.shouldTrigger(config, context.level())) return;

        PotionEffect effect = retaliationSupport.createWeaknessEffect(config, context.level());
        retaliationSupport.apply(context, effect, key);
    }
}
