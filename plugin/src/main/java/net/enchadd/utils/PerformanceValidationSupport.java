package net.enchadd.utils;

import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

final class PerformanceValidationSupport {

    private PerformanceValidationSupport() {
    }

    @Nullable
    static EntityEquipment getEquipmentSafe(@Nullable LivingEntity entity) {
        if (entity == null) {
            return null;
        }
        return entity.getEquipment();
    }

    @Nullable
    static PersistentDataContainer getPDCSafe(@Nullable org.bukkit.entity.Entity entity) {
        if (entity == null) {
            return null;
        }
        try {
            return entity.getPersistentDataContainer();
        } catch (Exception e) {
            return null;
        }
    }

    static boolean isPlayerValid(@Nullable Player player) {
        return player != null && player.isOnline() && player.isValid();
    }

    static boolean isEntityValid(@Nullable LivingEntity entity) {
        return entity != null && entity.isValid() && !entity.isDead();
    }

    static boolean shouldTick(@Nullable Entity entity, int modulo) {
        if (entity == null) {
            return false;
        }
        int throttle = Math.max(1, SafetyModeManager.getTickModuloMultiplier())
                * Math.max(1, EnchantExecutionBudgetManager.getCurrentTickModuloMultiplier());
        int m = Math.max(1, modulo) * throttle;
        return entity.getTicksLived() % m == 0;
    }
}
