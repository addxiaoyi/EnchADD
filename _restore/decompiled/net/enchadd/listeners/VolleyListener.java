/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.registry.RegistryAccess
 *  io.papermc.paper.registry.RegistryKey
 *  org.bukkit.Registry
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.AbstractArrow
 *  org.bukkit.entity.AbstractArrow$PickupStatus
 *  org.bukkit.entity.Arrow
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Projectile
 *  org.bukkit.entity.SpectralArrow
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason
 *  org.bukkit.event.entity.ProjectileLaunchEvent
 *  org.bukkit.inventory.EntityEquipment
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.projectiles.ProjectileSource
 *  org.bukkit.util.Vector
 *  org.jetbrains.annotations.NotNull
 */
package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.VolleyEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class VolleyListener
implements Listener {
    private static final int MAX_EXTRA_ARROW_MULTIPLIER = 32;
    private final Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment volley = (Enchantment)this.registry.get(VolleyEnchant.KEY);
    private final Random random = ThreadLocalRandom.current();
    private final VolleyEnchant config;

    public VolleyListener() {
        EnchADDEnchant enchantObj = EnchADDConfig.ENCHANTS.get(VolleyEnchant.KEY);
        this.config = enchantObj instanceof VolleyEnchant ? (VolleyEnchant)enchantObj : null;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onVolley(ProjectileLaunchEvent event) {
        if (this.volley == null || this.config == null) {
            return;
        }
        Projectile projectileEntity = event.getEntity();
        if (!(projectileEntity instanceof AbstractArrow)) {
            return;
        }
        AbstractArrow launchedArrow = (AbstractArrow)projectileEntity;
        if (CreatureSpawnEvent.SpawnReason.ENCHANTMENT.equals((Object)projectileEntity.getEntitySpawnReason())) {
            return;
        }
        if (this.hasVolleyMarker(launchedArrow)) {
            return;
        }
        if (!projectileEntity.isValid()) {
            return;
        }
        ProjectileSource projectileSource = launchedArrow.getShooter();
        if (!(projectileSource instanceof LivingEntity)) {
            return;
        }
        LivingEntity shooter = (LivingEntity)projectileSource;
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(shooter);
        if (equipment == null) {
            return;
        }
        ItemStack bow = equipment.getItemInMainHand();
        int level = PerformanceUtils.getEnchantLevel(bow, this.volley);
        if (level <= 0) {
            return;
        }
        if (projectileEntity instanceof Arrow) {
            Arrow normalArrow = (Arrow)projectileEntity;
            this.spawnArrowVolley(shooter, normalArrow, level);
            return;
        }
        if (projectileEntity instanceof SpectralArrow) {
            SpectralArrow spectralArrow = (SpectralArrow)projectileEntity;
            this.spawnSpectralVolley(shooter, spectralArrow, level);
        }
    }

    private void spawnArrowVolley(LivingEntity shooter, Arrow arrow, int level) {
        Vector velocity = arrow.getVelocity();
        if (velocity == null || velocity.lengthSquared() == 0.0) {
            return;
        }
        int extraArrows = Math.max(0, Math.min(32, level * this.config.getAdditionalArrowsPerLevel()));
        if (extraArrows <= 0) {
            return;
        }
        arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
        for (int i = 0; i < extraArrows; ++i) {
            Vector newVelocity = this.applySpread(velocity, this.config.getSpread());
            shooter.getWorld().spawn(arrow.getLocation(), Arrow.class, spawnedArrow -> {
                spawnedArrow.setVelocity(newVelocity);
                this.markVolleyArrow((AbstractArrow)spawnedArrow);
                spawnedArrow.setCritical(arrow.isCritical());
                spawnedArrow.setShooter(arrow.getShooter());
                spawnedArrow.setHasLeftShooter(arrow.hasLeftShooter());
                spawnedArrow.setBasePotionType(arrow.getBasePotionType());
                spawnedArrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                arrow.getCustomEffects().forEach(effect -> spawnedArrow.addCustomEffect(effect, false));
            }, CreatureSpawnEvent.SpawnReason.ENCHANTMENT);
        }
    }

    private void spawnSpectralVolley(LivingEntity shooter, SpectralArrow arrow, int level) {
        Vector velocity = arrow.getVelocity();
        if (velocity == null || velocity.lengthSquared() == 0.0) {
            return;
        }
        int extraArrows = Math.max(0, Math.min(32, level));
        if (extraArrows <= 0) {
            return;
        }
        arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
        for (int i = 0; i < extraArrows; ++i) {
            Vector newVelocity = this.applySpread(velocity, this.config.getSpread());
            shooter.getWorld().spawn(arrow.getLocation(), SpectralArrow.class, spawnedArrow -> {
                spawnedArrow.setVelocity(newVelocity);
                this.markVolleyArrow((AbstractArrow)spawnedArrow);
                spawnedArrow.setCritical(arrow.isCritical());
                spawnedArrow.setShooter(arrow.getShooter());
                spawnedArrow.setHasLeftShooter(arrow.hasLeftShooter());
                spawnedArrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
            }, CreatureSpawnEvent.SpawnReason.ENCHANTMENT);
        }
    }

    private boolean hasVolleyMarker(AbstractArrow arrow) {
        return (Boolean)arrow.getPersistentDataContainer().getOrDefault(this.volley.getKey(), PersistentDataType.BOOLEAN, (Object)false);
    }

    private void markVolleyArrow(AbstractArrow arrow) {
        arrow.getPersistentDataContainer().set(this.volley.getKey(), PersistentDataType.BOOLEAN, (Object)true);
    }

    private Vector applySpread(Vector velocity, double spread) {
        double spreadX = (this.random.nextDouble() - 0.5) * spread;
        double spreadY = (this.random.nextDouble() - 0.5) * spread;
        double spreadZ = (this.random.nextDouble() - 0.5) * spread;
        return velocity.clone().add(new Vector(spreadX, spreadY, spreadZ));
    }
}
