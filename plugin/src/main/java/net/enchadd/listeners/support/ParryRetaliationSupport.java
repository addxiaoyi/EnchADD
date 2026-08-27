package net.enchadd.listeners.support;

import net.enchadd.enchants.ParryEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParryRetaliationSupport {

    public @Nullable ArmContext resolveArmContext(@NotNull Player defender,
                                                  @NotNull LivingEntity attacker,
                                                  @NotNull Enchantment enchant) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(defender);
        if (equipment == null) {
            return null;
        }

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInOffHand(), enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(defender);
        if (pdc == null) {
            return null;
        }
        return new ArmContext(attacker.getEntityId(), level, pdc);
    }

    public boolean armRetaliation(@NotNull ArmContext context,
                                  @NotNull ParryEnchant config,
                                  @NotNull NamespacedKey cooldownKey,
                                  @NotNull NamespacedKey windowKey,
                                  @NotNull NamespacedKey targetKey,
                                  @NotNull NamespacedKey levelKey) {
        PersistentDataContainer pdc = context.pdc();
        if (PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) {
            return false;
        }

        PerformanceUtils.setCooldown(pdc, cooldownKey);
        PerformanceUtils.setWindowUntilTicks(pdc, windowKey, config.getRetaliationWindowTicks());
        pdc.set(targetKey, PersistentDataType.INTEGER, context.attackerId());
        pdc.set(levelKey, PersistentDataType.INTEGER, context.level());
        return true;
    }

    public @Nullable PersistentDataContainer resolvePdc(@NotNull Player player) {
        return PerformanceUtils.getPDCSafe(player);
    }

    public void applyCounterattack(@NotNull PersistentDataContainer pdc,
                                   @NotNull LivingEntity target,
                                   @NotNull ParryEnchant config,
                                   @NotNull NamespacedKey windowKey,
                                   @NotNull NamespacedKey targetKey,
                                   @NotNull NamespacedKey levelKey,
                                   double baseDamage,
                                   @NotNull DamageWriter writer) {
        if (!PerformanceUtils.isWindowActive(pdc, windowKey)) {
            return;
        }

        Integer targetId = pdc.get(targetKey, PersistentDataType.INTEGER);
        if (targetId == null || targetId != target.getEntityId()) {
            return;
        }

        Integer level = pdc.get(levelKey, PersistentDataType.INTEGER);
        if (level == null || level <= 0) {
            return;
        }

        double bonusDamage = Math.min(config.getMaxBonusDamage(), level * config.getBonusDamagePerLevel());
        if (bonusDamage > 0.0) {
            writer.write(baseDamage + bonusDamage);
        }

        pdc.remove(windowKey);
        pdc.remove(targetKey);
        pdc.remove(levelKey);
    }

    @FunctionalInterface
    public interface DamageWriter {
        void write(double damage);
    }

    public record ArmContext(int attackerId, int level, @NotNull PersistentDataContainer pdc) {
    }
}
