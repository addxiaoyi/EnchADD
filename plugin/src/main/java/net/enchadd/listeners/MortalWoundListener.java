package net.enchadd.listeners;

import net.enchadd.listeners.support.MortalWoundEffectSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.MortalWoundEnchant;
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
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.enchadd.utils.PerformanceUtils;

@SuppressWarnings("UnstableApiUsage")
public class MortalWoundListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(MortalWoundEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(MortalWoundEnchant.KEY);
    private final MortalWoundEnchant config;
    private MortalWoundEffectSupport effectSupport;

    public MortalWoundListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(MortalWoundEnchant.KEY);
        this.config = (enchantObj instanceof MortalWoundEnchant) ? (MortalWoundEnchant) enchantObj : null;
        this.effectSupport = this.config == null ? null : new MortalWoundEffectSupport(this.config, key);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(ProjectileLaunchEvent event) {
        if (enchant == null) return;
        if (effectSupport == null) return;
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (arrow.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) return;
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
        Player shooter = arrow.getShooter() instanceof Player p ? p : null;
        if (shooter == null) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (enchant == null || config == null || effectSupport == null) return;
        Integer level = effectSupport.readLaunchLevel(arrow);
        if (level == null || level <= 0) return;
        if (effectSupport.isShooterOnCooldown(shooter)) return;
        if (!effectSupport.shouldTrigger(level)) return;

        int durationTicks = effectSupport.antiHealDurationTicks(level);
        effectSupport.applyAntiHealWindow(victim, durationTicks);
        effectSupport.writeShooterCooldown(shooter);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (config == null || effectSupport == null) return;
        effectSupport.applyHealingScale(entity, event.getAmount(), amount -> event.setAmount((float) amount));
    }
}
