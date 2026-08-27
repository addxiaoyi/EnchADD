package net.enchadd.listeners.support;

import net.enchadd.enchants.ShroudEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public final class ShroudTargetSupport {

    private static final Set<EntityTargetLivingEntityEvent.TargetReason> REASONS = EnumSet.of(
            EntityTargetLivingEntityEvent.TargetReason.CLOSEST_PLAYER,
            EntityTargetLivingEntityEvent.TargetReason.COLLISION,
            EntityTargetLivingEntityEvent.TargetReason.TARGET_ATTACKED_ENTITY,
            EntityTargetLivingEntityEvent.TargetReason.FORGOT_TARGET
    );

    public @Nullable Player resolveEligibleTarget(@NotNull EntityTargetLivingEntityEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof Player player)) {
            return null;
        }
        if (!player.isSneaking()) {
            return null;
        }
        if (!(event.getEntity() instanceof Mob)) {
            return null;
        }
        if (!REASONS.contains(event.getReason())) {
            return null;
        }
        return player;
    }

    public @Nullable ShroudContext resolveContext(@NotNull Player player, @NotNull Enchantment enchant) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }

        int level = PerformanceUtils.getHighestEnchantLevel(equipment, enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new ShroudContext(level, pdc);
    }

    public boolean shouldTrigger(@NotNull ShroudContext context,
                                 @NotNull NamespacedKey key,
                                 @NotNull ShroudEnchant config) {
        if (PerformanceUtils.isOnCooldown(context.pdc(), key, config.getCooldownTicks())) {
            return false;
        }
        double chance = Math.min(config.getMaxTriggerChance(), config.getTriggerChance() * context.level());
        return PerformanceUtils.rollChance(chance);
    }

    public void apply(@NotNull EntityTargetLivingEntityEvent event,
                      @NotNull ShroudContext context,
                      @NotNull NamespacedKey key) {
        event.setTarget(null);
        PerformanceUtils.setCooldown(context.pdc(), key);
    }

    public record ShroudContext(int level, @NotNull PersistentDataContainer pdc) {
    }
}
