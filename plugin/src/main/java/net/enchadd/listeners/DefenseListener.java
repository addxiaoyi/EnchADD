package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.DefenseEnchant;
import net.enchadd.listeners.support.DefenseCombatSupport;
import net.enchadd.utils.PerformanceUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;

public final class DefenseListener implements Listener {

    private final Enchantment enchant;
    private final DefenseEnchant config;

    public DefenseListener(Key key) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        this.enchant = registry.get(key);
        Object configured = EnchADDConfig.ENCHANTS.get(key);
        this.config = configured instanceof DefenseEnchant defense ? defense : null;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (enchant == null || config == null || event.isCancelled()
                || !Double.isFinite(event.getFinalDamage()) || event.getFinalDamage() <= 0.0
                || !matches(event.getCause(), config.getMode())) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(entity);
        if (equipment == null) {
            return;
        }
        int level = Math.min(config.getMaxLevel(), PerformanceUtils.getHighestEnchantLevel(equipment, enchant));
        if (level <= 0) {
            return;
        }
        double adjusted = DefenseCombatSupport.reduce(event.getDamage(), level, config.getMaxLevel(),
                config.getReductionPerLevel(), config.getMaxReduction());
        if (adjusted < event.getDamage()) {
            event.setDamage(adjusted);
        }
    }

    private static boolean matches(EntityDamageEvent.DamageCause cause, DefenseEnchant.Mode mode) {
        return switch (mode) {
            case PROJECTILE -> cause == EntityDamageEvent.DamageCause.PROJECTILE;
            case EXPLOSION -> cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                    || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;
            case FIRE -> cause == EntityDamageEvent.DamageCause.FIRE
                    || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                    || cause == EntityDamageEvent.DamageCause.LAVA
                    || cause == EntityDamageEvent.DamageCause.HOT_FLOOR;
            case FALL -> cause == EntityDamageEvent.DamageCause.FALL;
            case FREEZE -> cause == EntityDamageEvent.DamageCause.FREEZE;
        };
    }
}