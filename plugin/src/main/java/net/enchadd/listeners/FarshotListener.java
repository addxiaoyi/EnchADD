package net.enchadd.listeners;

import net.enchadd.listeners.support.FarshotDamageSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.FarshotEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.enchadd.utils.PerformanceUtils;

@SuppressWarnings("UnstableApiUsage")
public class FarshotListener implements Listener {

    private final Enchantment enchant;
    private final NamespacedKey levelKey;
    private final NamespacedKey cooldownKey;
    private final NamespacedKey launchWorldKey;
    private final NamespacedKey launchXKey;
    private final NamespacedKey launchYKey;
    private final NamespacedKey launchZKey;
    private final FarshotEnchant config;
    private final FarshotDamageSupport damageSupport;

    public FarshotListener() {
        this(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(FarshotEnchant.KEY),
                PerformanceUtils.namespacedKey(FarshotEnchant.KEY),
                PerformanceUtils.namespacedKeyWithSuffix(FarshotEnchant.KEY, "_cooldown"),
                resolveConfig()
        );
    }

    public FarshotListener(@Nullable Enchantment enchant,
                           @NotNull NamespacedKey levelKey,
                           @NotNull NamespacedKey cooldownKey,
                           @Nullable FarshotEnchant config) {
        this.enchant = enchant;
        this.levelKey = levelKey;
        this.cooldownKey = cooldownKey;
        this.launchWorldKey = siblingKey(levelKey, "_launch_world");
        this.launchXKey = siblingKey(levelKey, "_launch_x");
        this.launchYKey = siblingKey(levelKey, "_launch_y");
        this.launchZKey = siblingKey(levelKey, "_launch_z");
        this.config = config;
        this.damageSupport = config == null ? null : new FarshotDamageSupport(config);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(ProjectileLaunchEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (arrow.getPersistentDataContainer().has(levelKey, PersistentDataType.INTEGER)) return;
        if (!(event.getEntity().getShooter() instanceof LivingEntity shooter)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(shooter);
        if (equipment == null) return;

        ItemStack mainHand = equipment.getItemInMainHand();
        if (mainHand == null) return;

        int level = Math.min(config.getMaxLevel(), PerformanceUtils.getEnchantLevel(mainHand, enchant));
        if (level <= 0) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(arrow);
        if (pdc == null) return;

        Location launch = arrow.getLocation();
        World world = launch.getWorld();
        if (world == null || !hasFiniteCoordinates(launch)) return;

        pdc.set(levelKey, PersistentDataType.INTEGER, level);
        pdc.set(launchWorldKey, PersistentDataType.STRING, world.getUID().toString());
        pdc.set(launchXKey, PersistentDataType.DOUBLE, launch.getX());
        pdc.set(launchYKey, PersistentDataType.DOUBLE, launch.getY());
        pdc.set(launchZKey, PersistentDataType.DOUBLE, launch.getZ());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled() || !Double.isFinite(event.getFinalDamage()) || event.getFinalDamage() <= 0) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (!(event.getDamager() instanceof AbstractArrow arrow)) return;
        if (config == null || damageSupport == null) return;

        PersistentDataContainer arrowPdc = PerformanceUtils.getPDCSafe(arrow);
        if (arrowPdc == null) return;

        Integer level = arrowPdc.get(levelKey, PersistentDataType.INTEGER);
        if (level == null || level <= 0) return;

        Player shooter = arrow.getShooter() instanceof Player p ? p : null;
        if (shooter == null) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(shooter);
        if (pdc == null) return;

        if (PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) return;

        double distance = launchDistance(arrowPdc, victim);
        if (!Double.isFinite(distance)) return;
        if (distance <= config.getMinDistance()) return;

        if (!PerformanceUtils.rollChance(damageSupport.triggerChance())) return;

        double base = event.getDamage();
        double scaledDamage = damageSupport.scaledDamage(base, level, distance);
        if (scaledDamage <= base) return;

        event.setDamage(scaledDamage);
        PerformanceUtils.setCooldown(pdc, cooldownKey);
    }

    private double launchDistance(@NotNull PersistentDataContainer pdc, @NotNull LivingEntity victim) {
        String worldId = pdc.get(launchWorldKey, PersistentDataType.STRING);
        Double x = pdc.get(launchXKey, PersistentDataType.DOUBLE);
        Double y = pdc.get(launchYKey, PersistentDataType.DOUBLE);
        Double z = pdc.get(launchZKey, PersistentDataType.DOUBLE);
        if (worldId == null || x == null || y == null || z == null
                || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || !worldId.equals(victim.getWorld().getUID().toString())) {
            return Double.NaN;
        }

        Location target = victim.getLocation();
        if (!hasFiniteCoordinates(target)) return Double.NaN;
        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double dz = target.getZ() - z;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        return Double.isFinite(distanceSquared) ? Math.sqrt(distanceSquared) : Double.NaN;
    }

    private static boolean hasFiniteCoordinates(@NotNull Location location) {
        return Double.isFinite(location.getX())
                && Double.isFinite(location.getY())
                && Double.isFinite(location.getZ());
    }

    private static @NotNull NamespacedKey siblingKey(@NotNull NamespacedKey key, @NotNull String suffix) {
        return new NamespacedKey(key.getNamespace(), key.getKey() + suffix);
    }

    private static @Nullable FarshotEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(FarshotEnchant.KEY);
        return enchantObj instanceof FarshotEnchant farshot ? farshot : null;
    }
}

