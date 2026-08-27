package net.enchadd.listeners;

import net.enchadd.listeners.support.VolleySpawnSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.VolleyEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class VolleyListener implements Listener {

    private static final int MAX_EXTRA_ARROW_MULTIPLIER = 32;

    private final Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment volley = registry.get(VolleyEnchant.KEY);
    private final Random random = ThreadLocalRandom.current();
    private final VolleyEnchant config;
    private VolleySpawnSupport spawnSupport;

    public VolleyListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(VolleyEnchant.KEY);
        this.config = (enchantObj instanceof VolleyEnchant) ? (VolleyEnchant) enchantObj : null;
        this.spawnSupport = this.config == null ? null : new VolleySpawnSupport(this.config, MAX_EXTRA_ARROW_MULTIPLIER);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVolley(ProjectileLaunchEvent event) {
        if (volley == null || config == null || spawnSupport == null) return;

        Entity projectileEntity = event.getEntity();
        if (!(projectileEntity instanceof AbstractArrow launchedArrow)) return;
        if (CreatureSpawnEvent.SpawnReason.ENCHANTMENT.equals(projectileEntity.getEntitySpawnReason())) return;
        if (hasVolleyMarker(launchedArrow)) return;
        if (!projectileEntity.isValid()) return;
        if (!(launchedArrow.getShooter() instanceof LivingEntity shooter)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(shooter);
        if (equipment == null) return;

        ItemStack bow = equipment.getItemInMainHand();
        int level = PerformanceUtils.getEnchantLevel(bow, volley);
        if (level <= 0) return;

        if (projectileEntity instanceof Arrow normalArrow) {
            spawnArrowVolley(shooter, normalArrow, level);
            return;
        }
        if (projectileEntity instanceof SpectralArrow spectralArrow) {
            spawnSpectralVolley(shooter, spectralArrow, level);
        }
    }

    private void spawnArrowVolley(LivingEntity shooter, Arrow arrow, int level) {
        if (spawnSupport == null) return;
        Vector velocity = arrow.getVelocity();
        if (velocity == null || velocity.lengthSquared() == 0.0) return;

        int extraArrows = spawnSupport.extraArrowsForNormal(level);
        if (extraArrows <= 0) return;

        spawnSupport.spawnArrowVolley(
                shooter,
                arrow,
                extraArrows,
                velocity,
                random,
                () -> arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY),
                this::markVolleyArrow
        );
    }

    private void spawnSpectralVolley(LivingEntity shooter, SpectralArrow arrow, int level) {
        if (spawnSupport == null) return;
        Vector velocity = arrow.getVelocity();
        if (velocity == null || velocity.lengthSquared() == 0.0) return;

        int extraArrows = spawnSupport.extraArrowsForSpectral(level);
        if (extraArrows <= 0) return;

        spawnSupport.spawnSpectralVolley(
                shooter,
                arrow,
                extraArrows,
                velocity,
                random,
                () -> arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY),
                this::markVolleyArrow
        );
    }

    private boolean hasVolleyMarker(AbstractArrow arrow) {
        return spawnSupport != null && spawnSupport.hasVolleyMarker(arrow, volley.getKey());
    }

    private void markVolleyArrow(AbstractArrow arrow) {
        if (spawnSupport != null) {
            spawnSupport.markVolleyArrow(arrow, volley.getKey());
        }
    }

    private Vector applySpread(Vector velocity, double spread) {
        return spawnSupport == null ? velocity.clone() : spawnSupport.applySpread(velocity, spread, random);
    }
}
