package net.enchadd.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.tag.DamageTypeTags;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

final class PerformanceShieldSupport {

    private static final int MIN_SHIELD_RAISE_TICKS = 5;
    private static final double SHIELD_FRONT_DOT_THRESHOLD = 0.15D;

    private PerformanceShieldSupport() {
    }

    static boolean isSuccessfulShieldBlock(@Nullable Player player,
                                           @Nullable EntityDamageByEntityEvent event) {
        if (player == null || event == null) {
            return false;
        }
        if (!isShieldBlockingState(player)) {
            return false;
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

        Vector horizontalKnockback = knockback.clone().setY(0.0);
        if (horizontalKnockback.lengthSquared() <= 1.0E-6) {
            return false;
        }

        Location playerLocation = player.getLocation();
        Vector facing = playerLocation.getDirection().setY(0.0);
        if (facing.lengthSquared() <= 1.0E-6) {
            return false;
        }

        Vector incoming = horizontalKnockback.multiply(-1.0).normalize();
        Vector forward = facing.normalize();
        return forward.dot(incoming) > SHIELD_FRONT_DOT_THRESHOLD;
    }

    private static boolean isShieldBlockingState(Player player) {
        if (!player.isBlocking() || !player.isHandRaised() || player.getHandRaisedTime() < MIN_SHIELD_RAISE_TICKS) {
            return false;
        }
        return isShieldRaised(player);
    }

    private static boolean isShieldRaised(Player player) {
        ItemStack activeItem = player.getActiveItem();
        if (activeItem != null && activeItem.getType() == Material.SHIELD) {
            return true;
        }

        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return false;
        }

        ItemStack offhand = equipment.getItemInOffHand();
        return offhand != null && offhand.getType() == Material.SHIELD;
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
        Location source = damager != null ? damager.getLocation() : null;
        if (source == null && damageSource != null) {
            source = damageSource.getSourceLocation();
        }
        if (source == null) {
            return false;
        }

        Location playerLocation = player.getLocation();
        Vector toSource = source.toVector().subtract(playerLocation.toVector()).setY(0.0);
        if (toSource.lengthSquared() <= 1.0E-6) {
            return false;
        }

        Vector facing = playerLocation.getDirection().setY(0.0);
        if (facing.lengthSquared() <= 1.0E-6) {
            return false;
        }

        return facing.normalize().dot(toSource.normalize()) > SHIELD_FRONT_DOT_THRESHOLD;
    }
}
