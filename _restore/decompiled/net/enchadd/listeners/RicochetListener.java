/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.registry.RegistryAccess
 *  io.papermc.paper.registry.RegistryKey
 *  org.bukkit.NamespacedKey
 *  org.bukkit.Registry
 *  org.bukkit.World
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.AbstractArrow
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.inventory.EntityEquipment
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.persistence.PersistentDataContainer
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.projectiles.ProjectileSource
 *  org.bukkit.util.Vector
 */
package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Collection;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.RicochetEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class RicochetListener
implements Listener {
    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = (Enchantment)this.registry.get(RicochetEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(RicochetEnchant.KEY);
    private final RicochetEnchant config;

    public RicochetListener() {
        EnchADDEnchant enchantObj = EnchADDConfig.ENCHANTS.get(RicochetEnchant.KEY);
        this.config = enchantObj instanceof RicochetEnchant ? (RicochetEnchant)enchantObj : null;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onHit(EntityDamageByEntityEvent event) {
        Player p;
        Player shooter;
        Entity entity = event.getDamager();
        if (!(entity instanceof AbstractArrow)) {
            return;
        }
        AbstractArrow arrow = (AbstractArrow)entity;
        if (arrow.getShooter() == null) {
            return;
        }
        ProjectileSource projectileSource = arrow.getShooter();
        Player player = shooter = projectileSource instanceof Player ? (p = (Player)projectileSource) : null;
        if (!PerformanceUtils.isPlayerValid(shooter)) {
            return;
        }
        if (this.enchant == null || this.config == null) {
            return;
        }
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe((LivingEntity)shooter);
        if (equipment == null) {
            return;
        }
        ItemStack main = equipment.getItemInMainHand();
        int level = PerformanceUtils.getEnchantLevel(main, this.enchant);
        if (level <= 0) {
            return;
        }
        PersistentDataContainer arrowPdc = arrow.getPersistentDataContainer();
        if (((Boolean)arrowPdc.getOrDefault(this.key, PersistentDataType.BOOLEAN, (Object)false)).booleanValue()) {
            return;
        }
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe((Entity)shooter);
        if (pdc == null) {
            return;
        }
        if (PerformanceUtils.isOnCooldown(pdc, this.key, this.config.getCooldownTicks())) {
            return;
        }
        double chance = Math.min(0.5, this.config.getTriggerChance() * (double)level);
        if (!PerformanceUtils.rollChance(chance)) {
            return;
        }
        World world = arrow.getWorld();
        double r = Math.max(1.0, this.config.getRadius());
        Collection nearbyEntities = world.getNearbyEntities(arrow.getLocation(), r, r, r);
        LivingEntity target = null;
        double minDistanceSquared = Double.MAX_VALUE;
        for (Entity entity2 : nearbyEntities) {
            double distanceSquared;
            LivingEntity livingEntity;
            if (!(entity2 instanceof LivingEntity) || (livingEntity = (LivingEntity)entity2).equals((Object)shooter) || livingEntity.equals((Object)event.getEntity()) || !((distanceSquared = livingEntity.getLocation().distanceSquared(arrow.getLocation())) < minDistanceSquared)) continue;
            minDistanceSquared = distanceSquared;
            target = livingEntity;
        }
        if (target == null) {
            return;
        }
        Vector dir = target.getEyeLocation().toVector().subtract(arrow.getLocation().toVector()).normalize();
        double baseSpeed = arrow.getVelocity().length() * this.config.getSpeedScale();
        Vector vel = dir.multiply(baseSpeed);
        world.spawn(arrow.getLocation(), arrow.getClass(), spawned -> {
            spawned.setVelocity(vel);
            spawned.setShooter((ProjectileSource)shooter);
            spawned.getPersistentDataContainer().set(this.key, PersistentDataType.BOOLEAN, (Object)true);
        });
        PerformanceUtils.setCooldown(pdc, this.key);
    }
}
