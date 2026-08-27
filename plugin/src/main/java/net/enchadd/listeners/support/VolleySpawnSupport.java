package net.enchadd.listeners.support;

import net.enchadd.enchants.VolleyEnchant;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public final class VolleySpawnSupport {

    private final VolleyEnchant config;
    private final int maxExtraArrowMultiplier;

    public VolleySpawnSupport(@NotNull VolleyEnchant config, int maxExtraArrowMultiplier) {
        this.config = config;
        this.maxExtraArrowMultiplier = maxExtraArrowMultiplier;
    }

    public int extraArrowsForNormal(int level) {
        return Math.max(0, Math.min(maxExtraArrowMultiplier, level * config.getAdditionalArrowsPerLevel()));
    }

    public int extraArrowsForSpectral(int level) {
        return Math.max(0, Math.min(maxExtraArrowMultiplier, level));
    }

    public void spawnArrowVolley(@NotNull LivingEntity shooter,
                          @NotNull Arrow arrow,
                          int extraArrows,
                          @NotNull Vector velocity,
                          @NotNull Random random,
                          @NotNull Runnable pickupGuard,
                          @NotNull java.util.function.Consumer<AbstractArrow> marker) {
        pickupGuard.run();
        for (int i = 0; i < extraArrows; i++) {
            Vector newVelocity = applySpread(velocity, config.getSpread(), random);
            shooter.getWorld().spawn(arrow.getLocation(), Arrow.class, spawnedArrow -> {
                spawnedArrow.setVelocity(newVelocity);
                marker.accept(spawnedArrow);
                spawnedArrow.setCritical(arrow.isCritical());
                spawnedArrow.setShooter(arrow.getShooter());
                spawnedArrow.setHasLeftShooter(arrow.hasLeftShooter());
                spawnedArrow.setBasePotionType(arrow.getBasePotionType());
                spawnedArrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                arrow.getCustomEffects().forEach(effect -> spawnedArrow.addCustomEffect(effect, false));
            }, CreatureSpawnEvent.SpawnReason.ENCHANTMENT);
        }
    }

    public void spawnSpectralVolley(@NotNull LivingEntity shooter,
                             @NotNull SpectralArrow arrow,
                             int extraArrows,
                             @NotNull Vector velocity,
                             @NotNull Random random,
                             @NotNull Runnable pickupGuard,
                             @NotNull java.util.function.Consumer<AbstractArrow> marker) {
        pickupGuard.run();
        for (int i = 0; i < extraArrows; i++) {
            Vector newVelocity = applySpread(velocity, config.getSpread(), random);
            shooter.getWorld().spawn(arrow.getLocation(), SpectralArrow.class, spawnedArrow -> {
                spawnedArrow.setVelocity(newVelocity);
                marker.accept(spawnedArrow);
                spawnedArrow.setCritical(arrow.isCritical());
                spawnedArrow.setShooter(arrow.getShooter());
                spawnedArrow.setHasLeftShooter(arrow.hasLeftShooter());
                spawnedArrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
            }, CreatureSpawnEvent.SpawnReason.ENCHANTMENT);
        }
    }

    public boolean hasVolleyMarker(@NotNull AbstractArrow arrow, @NotNull org.bukkit.NamespacedKey key) {
        return arrow.getPersistentDataContainer().getOrDefault(key, PersistentDataType.BOOLEAN, false);
    }

    public void markVolleyArrow(@NotNull AbstractArrow arrow, @NotNull org.bukkit.NamespacedKey key) {
        arrow.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
    }

    public Vector applySpread(@NotNull Vector velocity, double spread, @NotNull Random random) {
        double spreadX = (random.nextDouble() - 0.5) * spread;
        double spreadY = (random.nextDouble() - 0.5) * spread;
        double spreadZ = (random.nextDouble() - 0.5) * spread;
        return velocity.clone().add(new Vector(spreadX, spreadY, spreadZ));
    }
}
