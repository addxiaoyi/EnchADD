package net.enchadd.listeners;

import net.enchadd.listeners.support.FarshotDamageSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.FarshotEnchant;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.enchadd.utils.PerformanceUtils;

@SuppressWarnings("UnstableApiUsage")
public class FarshotListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(FarshotEnchant.KEY);
    private final NamespacedKey levelKey = PerformanceUtils.namespacedKey(FarshotEnchant.KEY);
    private final NamespacedKey cooldownKey = PerformanceUtils.namespacedKeyWithSuffix(FarshotEnchant.KEY, "_cooldown");
    private final FarshotEnchant config;
    private FarshotDamageSupport damageSupport;

    public FarshotListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(FarshotEnchant.KEY);
        this.config = (enchantObj instanceof FarshotEnchant) ? (FarshotEnchant) enchantObj : null;
        this.damageSupport = this.config == null ? null : new FarshotDamageSupport(this.config);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(ProjectileLaunchEvent event) {
        if (enchant == null) return;
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (arrow.getPersistentDataContainer().has(levelKey, PersistentDataType.INTEGER)) return;
        if (!(event.getEntity().getShooter() instanceof LivingEntity shooter)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(shooter);
        if (equipment == null) return;

        ItemStack mainHand = equipment.getItemInMainHand();
        if (mainHand == null) return;

        int level = PerformanceUtils.getEnchantLevel(mainHand, enchant);
        if (level <= 0) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(arrow);
        if (pdc == null) return;

        pdc.set(levelKey, PersistentDataType.INTEGER, level);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (!(event.getDamager() instanceof AbstractArrow arrow)) return;
        if (config == null || damageSupport == null) return;

        PersistentDataContainer arrowPdc = PerformanceUtils.getPDCSafe(arrow);
        if (arrowPdc == null) return;

        Integer level = arrowPdc.get(levelKey, PersistentDataType.INTEGER);
        if (level == null || level <= 0) return;

        Player shooter = arrow.getShooter() instanceof Player p ? p : null;
        if (shooter == null) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(shooter);
        if (pdc == null) return;

        if (PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) return;

        double distance = shooter.getLocation().distance(victim.getLocation());
        if (distance <= config.getMinDistance()) return;

        if (!PerformanceUtils.rollChance(damageSupport.triggerChance())) return;

        double base = event.getDamage();
        double scaledDamage = damageSupport.scaledDamage(base, level, distance);
        if (scaledDamage <= base) return;

        event.setDamage(scaledDamage);
        PerformanceUtils.setCooldown(pdc, cooldownKey);
    }
}

