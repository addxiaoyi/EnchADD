package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.PurifyEnchant;
import net.enchadd.listeners.support.PotionCleanseContextSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class PurifyListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(PurifyEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(PurifyEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final PurifyEnchant config;
    private final PotionCleanseContextSupport cleanseSupport = new PotionCleanseContextSupport();
    private final List<PotionEffectType> negatives = List.of(
            PotionEffectType.POISON,
            PotionEffectType.WITHER,
            PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.BLINDNESS,
            PotionEffectType.HUNGER,
            PotionEffectType.WEAKNESS,
            PotionEffectType.NAUSEA
    );

    public PurifyListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(PurifyEnchant.KEY);
        this.config = (enchantObj instanceof PurifyEnchant) ? (PurifyEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (enchant == null || config == null) {
            return;
        }
        PotionCleanseContextSupport.PurifyContext cleanseContext = cleanseSupport.resolvePurifyContext(event, enchant);
        if (cleanseContext == null) {
            return;
        }
        Player player = cleanseContext.player();

        if (PerformanceUtils.isOnCooldown(cleanseContext.pdc(), key, config.getCooldownTicks())) {
            return;
        }
        if (!PerformanceUtils.rollChance(config.getTriggerChance())) {
            return;
        }

        int level = cleanseContext.level();
        int removed = 0;
        for (PotionEffectType type : negatives) {
            if (player.hasPotionEffect(type)) {
                player.removePotionEffect(type);
                removed++;
                if (removed >= level) {
                    break;
                }
            }
        }
        if (removed > 0) {
            PerformanceUtils.setCooldown(cleanseContext.pdc(), key);
        }
    }
}
