package net.enchadd.listeners.support;

import net.enchadd.enchants.AirbagEnchant;
import net.enchadd.utils.EnchantCache;
import net.enchadd.utils.EnchantStats;
import net.enchadd.utils.ParticleQueue;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class AirbagImpactSupport {

    public boolean isCushionedCause(@NotNull EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.FLY_INTO_WALL || cause == EntityDamageEvent.DamageCause.FALL;
    }

    public int resolveArmorLevel(@NotNull EntityEquipment equipment, @NotNull Enchantment enchantment) {
        int levels = 0;
        for (ItemStack item : equipment.getArmorContents()) {
            levels += EnchantCache.getLevel(item, enchantment);
        }
        return levels;
    }

    public double resolveReduction(@NotNull AirbagEnchant config, int levels) {
        return PerformanceUtils.clamp(levels * config.getDamageReductionPerLevel(), 0.0, 1.0);
    }

    public void applyImpactReduction(@NotNull EntityDamageEvent event,
                                     @NotNull LivingEntity livingEntity,
                                     double damage,
                                     double reduction) {
        event.setDamage(damage * (1.0 - reduction));
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
