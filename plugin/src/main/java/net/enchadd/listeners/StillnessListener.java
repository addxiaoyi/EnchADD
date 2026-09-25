package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.StillnessEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import net.enchadd.utils.PerformanceUtils;

@SuppressWarnings("UnstableApiUsage")
public class StillnessListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(StillnessEnchant.KEY);
    private final NamespacedKey levelKey = PerformanceUtils.namespacedKey(StillnessEnchant.KEY);
    private final NamespacedKey cooldownKey = PerformanceUtils.namespacedKeyWithSuffix(StillnessEnchant.KEY, "_cooldown");

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(ProjectileLaunchEvent event) {
        if (enchant == null) return;
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (!(event.getEntity().getShooter() instanceof Player player)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) return;

        if (!(EnchADDConfig.ENCHANTS.get(StillnessEnchant.KEY) instanceof StillnessEnchant ench)) return;

        double speed = player.getVelocity().length();
        if (speed > ench.getMovementThreshold()) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) return;
        if (PerformanceUtils.isOnCooldown(pdc, cooldownKey, ench.getCooldownTicks())) return;

        double chance = Math.min(ench.getMaxTriggerChance(), ench.getTriggerChance() * level);
        if (!PerformanceUtils.rollChance(chance)) return;

        PerformanceUtils.setCooldown(pdc, cooldownKey);
        PersistentDataContainer apdc = arrow.getPersistentDataContainer();
        apdc.set(levelKey, PersistentDataType.INTEGER, level);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof AbstractArrow arrow)) return;
        Integer level = arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
        if (level == null || level <= 0) return;
        if (!(EnchADDConfig.ENCHANTS.get(StillnessEnchant.KEY) instanceof @NotNull StillnessEnchant ench)) return;
        double base = event.getDamage();
        double rawBonus = ench.getBonusDamagePerLevel() * level;
        if (!Double.isFinite(base) || base <= 0.0d || !Double.isFinite(rawBonus)) return;
        double bonus = Math.min(0.75, Math.max(0.0, rawBonus));
        double scaled = base * (1.0d + bonus);
        if (Double.isFinite(scaled)) event.setDamage(scaled);
    }
}

