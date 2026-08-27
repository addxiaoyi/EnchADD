package net.enchadd.listeners.support;

import net.enchadd.enchants.EvasionEnchant;
import net.enchadd.utils.EnchantStats;
import net.enchadd.utils.ParticleQueue;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;

public final class EvasionProjectileSupport {

    public int resolveLevel(@NotNull Player victim, @NotNull Enchantment enchant) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(victim);
        if (equipment == null) {
            return 0;
        }
        return PerformanceUtils.getEnchantLevel(equipment.getBoots(), enchant);
    }

    public boolean shouldTrigger(@NotNull EvasionEnchant config, int level) {
        return PerformanceUtils.rollChanceWithLevel(config.getTriggerChance(), level, 0.6);
    }

    public void apply(@NotNull EntityDamageByEntityEvent event, @NotNull Player victim) {
        event.setCancelled(true);
        EnchantStats.record(net.enchadd.enchants.EvasionEnchant.KEY);

        Location location = victim.getLocation();
        World world = victim.getWorld();
        ParticleQueue.submit(location.clone().add(0, 1, 0), Particle.WITCH, 30);
        ParticleQueue.submit(location, Particle.GUST, 3);
        world.playSound(location, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.5f);
    }
}
