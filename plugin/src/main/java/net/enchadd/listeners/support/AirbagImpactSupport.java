package net.enchadd.listeners.support;

import net.enchadd.enchants.AirbagEnchant;
import net.enchadd.utils.EnchantCache;
import net.enchadd.utils.EnchantStats;
import net.enchadd.utils.ParticleQueue;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class AirbagImpactSupport {

    private static final double MAX_IMPACT_REDUCTION = 0.90;

    public boolean isCushionedCause(@NotNull EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.FLY_INTO_WALL || cause == EntityDamageEvent.DamageCause.FALL;
    }

    public int resolveArmorLevel(@NotNull EntityEquipment equipment, @NotNull Enchantment enchantment) {
        int levels = 0;
        for (ItemStack item : equipment.getArmorContents()) {
            levels = addArmorLevel(levels, EnchantCache.getLevel(item, enchantment));
        }
        return levels;
    }

    public double resolveReduction(@NotNull AirbagEnchant config, int levels) {
        return reduction(levels, config.getMaxLevel(), config.getDamageReductionPerLevel());
    }

    static int addArmorLevel(int total, int level) {
        long combined = (long) Math.max(0, total) + Math.max(0, level);
        return (int) Math.min(Integer.MAX_VALUE, combined);
    }

    static double reduction(int levels, int maxLevel, double perLevel) {
        if (levels <= 0 || maxLevel <= 0 || !Double.isFinite(perLevel) || perLevel <= 0) return 0;
        return Math.min(MAX_IMPACT_REDUCTION, perLevel * Math.min(levels, maxLevel));
    }

    static double reducedDamage(double damage, double finalDamage, double reduction) {
        if (!Double.isFinite(damage) || damage <= 0 || !Double.isFinite(finalDamage) || finalDamage <= 0
                || !Double.isFinite(reduction) || reduction <= 0) return damage;
        return damage * (1.0 - Math.min(MAX_IMPACT_REDUCTION, reduction));
    }

    public void applyImpactReduction(@NotNull EntityDamageEvent event,
                                     @NotNull LivingEntity livingEntity,
                                     double damage,
                                     double reduction) {
        if (event.isCancelled() || !isCushionedCause(event.getCause())) return;
        double adjusted = reducedDamage(damage, event.getFinalDamage(), reduction);
        if (Double.compare(adjusted, damage) == 0) return;
        event.setDamage(adjusted);
        EnchantStats.record(net.enchadd.enchants.AirbagEnchant.KEY);

        ParticleQueue.submit(
                livingEntity.getWorld(),
                livingEntity.getLocation().add(0, 1, 0),
                Particle.CLOUD,
                40,
                0.5,
                0.5,
                0.5,
                0.1
        );
        ParticleQueue.submit(
                livingEntity.getWorld(),
                livingEntity.getLocation(),
                Particle.CAMPFIRE_COSY_SMOKE,
                10,
                0.2,
                0.2,
                0.2,
                0.0
        );
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.BLOCK_WOOL_BREAK, 1f, 0.5f);
    }
}
