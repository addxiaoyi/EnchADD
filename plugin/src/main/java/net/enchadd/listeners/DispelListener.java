package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.DispelEnchant;
import net.enchadd.listeners.support.PotionCleanseContextSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class DispelListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(DispelEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(DispelEnchant.KEY);
    private final DispelEnchant config;
    private final PotionCleanseContextSupport cleanseSupport = new PotionCleanseContextSupport();

    private final List<PotionEffectType> positives = List.of(
            PotionEffectType.SPEED,
            PotionEffectType.REGENERATION,
            PotionEffectType.RESISTANCE,
            PotionEffectType.ABSORPTION,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.WATER_BREATHING,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.STRENGTH,
            PotionEffectType.JUMP_BOOST,
            PotionEffectType.HASTE
    );

    public DispelListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(DispelEnchant.KEY);
        this.config = (enchantObj instanceof DispelEnchant) ? (DispelEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) {
            return;
        }

        PotionCleanseContextSupport.DispelContext cleanseContext = cleanseSupport.resolveDispelContext(event, enchant);
        if (cleanseContext == null) {
            return;
        }

        if (PerformanceUtils.isOnCooldown(cleanseContext.pdc(), key, config.getCooldownTicks())) {
            return;
        }

        double chance = Math.min(0.95, config.getTriggerChance() * cleanseContext.level());
        if (!PerformanceUtils.rollChance(chance)) {
            return;
        }

        PotionEffectType removed = cleanseSupport.findFirstEffect(cleanseContext.victim(), positives);
        if (removed == null) {
            return;
        }
        cleanseContext.victim().removePotionEffect(removed);
        PerformanceUtils.setCooldown(cleanseContext.pdc(), key);
    }
}
