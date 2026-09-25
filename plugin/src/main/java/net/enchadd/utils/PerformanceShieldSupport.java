package net.enchadd.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.tag.DamageTypeTags;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

final class PerformanceShieldSupport {

    private static final int MIN_SHIELD_RAISE_TICKS = 5;
    private static final double SHIELD_FRONT_DOT_THRESHOLD = 0.15D;
    private static final double MIN_HORIZONTAL_LENGTH_SQUARED = 1.0E-6;

    private PerformanceShieldSupport() {
    }

    static int getActiveOffhandShieldLevel(@Nullable Player player, Enchantment enchant) {
        if (player == null || !player.isHandRaised()) return 0;
        ItemStack activeItem = player.getActiveItem();
        if (activeItem == null
                || !isActiveOffhandShield(player.getActiveItemHand(), activeItem.getType())) return 0;
        return PerformanceUtils.getEnchantLevel(activeItem, enchant);
    }

    static boolean isActiveOffhandShield(EquipmentSlot hand, Material material) {
        return hand == EquipmentSlot.OFF_HAND && material == Material.SHIELD;
    }

    static boolean isSuccessfulShieldBlock(@Nullable Player player,
                                           @Nullable EntityDamageByEntityEvent event) {
        if (player == null || event == null
                || !hasIncomingDamage(event.isCancelled(), event.getDamage(), event.getFinalDamage())) {
            return false;
        }
        if (!isShieldBlockingState(player)) {
            return false;
        }

        // The event records actual blocking, including attacks that pierce a raised shield.
        if (event.isApplicable(DamageModifier.BLOCKING)) {
            return hasBlockedDamage(event.getDamage(DamageModifier.BLOCKING));
        }

        DamageSource damageSource = event.getDamageSource();
        if (damageSource != null) {
            DamageType damageType = damageSource.getDamageType();
            if (damageType != null && isBypassesShieldDamageType(damageType)) {
                return false;
            }
        }

        return isIncomingFromFront(player, damageSource, event.getDamager());
    }

    static boolean isLikelyShieldFacingBlock(@Nullable Player player,
                                             @Nullable Vector knockback) {
        if (player == null || knockback == null) {
            return false;
        }
        if (!isShieldBlockingState(player)) {
            return false;
        }

        return facesIncoming(player.getLocation().getDirection(), knockback.clone().multiply(-1.0));
    }

    private static boolean isShieldBlockingState(Player player) {
        if (!player.isBlocking() || !player.isHandRaised() || player.getHandRaisedTime() < MIN_SHIELD_RAISE_TICKS) {
            return false;
        }
        return isShieldRaised(player);
    }

    private static boolean isShieldRaised(Player player) {
        ItemStack activeItem = player.getActiveItem();
        return activeItem != null && activeItem.getType() == Material.SHIELD;
    }

    private static boolean isBypassesShieldDamageType(DamageType damageType) {
        try {
            return DamageTypeTags.BYPASSES_SHIELD.isTagged(damageType);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean isIncomingFromFront(Player player,
                                               DamageSource damageSource,
                                               Entity damager) {
        Location source = damageSource != null ? damageSource.getSourceLocation() : null;
        if (source == null && damager != null) {
            source = damager.getLocation();
        }
        if (source == null) {
            return false;
        }

        Location playerLocation = player.getLocation();
        if (source.getWorld() == null || !source.getWorld().equals(playerLocation.getWorld())) {
            return false;
        }
        return facesIncoming(playerLocation.getDirection(), source.toVector().subtract(playerLocation.toVector()));
    }

    static boolean hasIncomingDamage(boolean cancelled, double baseDamage, double finalDamage) {
        return !cancelled && Double.isFinite(baseDamage) && baseDamage > 0
                && Double.isFinite(finalDamage) && finalDamage >= 0;
    }

    static boolean hasBlockedDamage(double modifier) {
        return Double.isFinite(modifier) && modifier < 0;
    }

    static boolean facesIncoming(Vector facing, Vector incoming) {
        if (!isFinite(facing) || !isFinite(incoming)) return false;
        Vector forward = facing.clone().setY(0);
        Vector horizontal = incoming.clone().setY(0);
        double facingLength = forward.lengthSquared();
        double incomingLength = horizontal.lengthSquared();
        if (!Double.isFinite(facingLength) || !Double.isFinite(incomingLength)
                || facingLength <= MIN_HORIZONTAL_LENGTH_SQUARED
                || incomingLength <= MIN_HORIZONTAL_LENGTH_SQUARED) return false;
        return forward.normalize().dot(horizontal.normalize()) > SHIELD_FRONT_DOT_THRESHOLD;
    }

    private static boolean isFinite(Vector vector) {
        return vector != null && Double.isFinite(vector.getX())
                && Double.isFinite(vector.getY()) && Double.isFinite(vector.getZ());
    }
}
