package net.enchadd.listeners;

import net.enchadd.listeners.support.RicochetEffectSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.RicochetEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("UnstableApiUsage")
public class RicochetListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(RicochetEnchant.KEY);
    private final NamespacedKey levelKey = PerformanceUtils.namespacedKey(RicochetEnchant.KEY);
    private final NamespacedKey cooldownKey = PerformanceUtils.namespacedKeyWithSuffix(RicochetEnchant.KEY, "_cooldown");
    private final NamespacedKey bouncedKey = PerformanceUtils.namespacedKeyWithSuffix(RicochetEnchant.KEY, "_bounced");
    private final RicochetEnchant config;
    private RicochetEffectSupport effectSupport;

    public RicochetListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(RicochetEnchant.KEY);
        this.config = (enchantObj instanceof RicochetEnchant) ? (RicochetEnchant) enchantObj : null;
        this.effectSupport = this.config == null ? null : new RicochetEffectSupport(this.config, levelKey, bouncedKey);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(ProjectileLaunchEvent event) {
        if (enchant == null) return;
        if (effectSupport == null) return;
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (arrow.getPersistentDataContainer().has(levelKey, PersistentDataType.INTEGER)) return;
        if (!(arrow.getShooter() instanceof LivingEntity shooter)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(shooter);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) return;

        effectSupport.tagLaunchLevel(arrow, level);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof AbstractArrow arrow)) return;
        if (arrow.getShooter() == null) return;
        Player shooter = arrow.getShooter() instanceof Player p ? p : null;
        if (!PerformanceUtils.isPlayerValid(shooter)) return;
        if (enchant == null || config == null || effectSupport == null) return;

        PersistentDataContainer arrowPdc = arrow.getPersistentDataContainer();
        Integer level = effectSupport.readLaunchLevel(arrow);
        if (level == null || level <= 0) return;
        if (effectSupport.hasBounced(arrow)) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(shooter);
        if (pdc == null) return;

        if (PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) return;
        if (!effectSupport.shouldTrigger(level)) return;

        LivingEntity target = effectSupport.findRicochetTarget(arrow, shooter, event.getEntity());
        if (target == null) return;

        if (!effectSupport.spawnRicochet(arrow, () -> shooter, target, level)) return;

        PerformanceUtils.setCooldown(pdc, cooldownKey);
    }
}
