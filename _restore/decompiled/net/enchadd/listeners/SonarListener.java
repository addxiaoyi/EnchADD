/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.registry.RegistryAccess
 *  io.papermc.paper.registry.RegistryKey
 *  org.bukkit.Location
 *  org.bukkit.NamespacedKey
 *  org.bukkit.Registry
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockFace
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.inventory.EntityEquipment
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.persistence.PersistentDataContainer
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 */
package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.SonarEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SonarListener
implements Listener {
    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = (Enchantment)this.registry.get(SonarEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(SonarEnchant.KEY);
    private final SonarEnchant config;

    public SonarListener() {
        EnchADDEnchant enchantObj = EnchADDConfig.ENCHANTS.get(SonarEnchant.KEY);
        this.config = enchantObj instanceof SonarEnchant ? (SonarEnchant)enchantObj : null;
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        PersistentDataContainer pdc;
        if (this.enchant == null || this.config == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!event.hasChangedPosition()) {
            return;
        }
        if (!PerformanceUtils.shouldTick((Entity)player, 5)) {
            return;
        }
        if (!this.isInWater((LivingEntity)player)) {
            return;
        }
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe((LivingEntity)player);
        if (equipment == null) {
            return;
        }
        ItemStack helmet = equipment.getHelmet();
        if (helmet == null) {
            return;
        }
        int level = PerformanceUtils.getEnchantLevel(helmet, this.enchant);
        if (level <= 0) {
            return;
        }
        if (!this.shouldRunPulse(player)) {
            return;
        }
        Location loc = player.getLocation();
        List<LivingEntity> nearby = this.getNearbyTargets(player, loc, level);
        if (nearby.isEmpty()) {
            return;
        }
        int duration = Math.max(20, this.config.getGlowTicksPerLevel() * level);
        int count = this.applyGlowEffect(nearby, duration);
        if (count > 0 && (pdc = PerformanceUtils.getPDCSafe((Entity)player)) != null) {
            PerformanceUtils.setCooldown(pdc, this.key);
        }
    }

    private boolean shouldRunPulse(Player player) {
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe((Entity)player);
        if (pdc == null) {
            return false;
        }
        return !PerformanceUtils.isOnCooldown(pdc, this.key, this.config.getCooldownTicks());
    }

    private List<LivingEntity> getNearbyTargets(Player player, Location loc, int level) {
        double radius = Math.max(2.0, this.config.getRadiusPerLevel() * (double)level);
        Collection nearbyEntities = player.getWorld().getNearbyEntities(loc, radius, radius, radius);
        ArrayList<LivingEntity> targets = PerformanceUtils.newArrayListWithCapacity(nearbyEntities.size());
        for (Entity e : nearbyEntities) {
            LivingEntity livingEntity;
            if (!(e instanceof LivingEntity) || (livingEntity = (LivingEntity)e).equals((Object)player)) continue;
            targets.add(livingEntity);
        }
        return targets;
    }

    private int applyGlowEffect(List<LivingEntity> nearby, int duration) {
        int applied = 0;
        for (LivingEntity target : nearby) {
            if (!this.isInWater(target)) continue;
            PotionEffect effect = new PotionEffect(PotionEffectType.GLOWING, duration, 0, false, false, true);
            target.addPotionEffect(effect);
            if (++applied < 10) continue;
            break;
        }
        return applied;
    }

    private boolean isInWater(LivingEntity entity) {
        Location eye = entity.getEyeLocation();
        Block block = eye.getBlock();
        if (block.isLiquid()) {
            return true;
        }
        Block above = block.getRelative(BlockFace.UP);
        return above.isLiquid();
    }
}
