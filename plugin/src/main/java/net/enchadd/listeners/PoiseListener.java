package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.PoiseEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.util.Vector;

@SuppressWarnings("UnstableApiUsage")
public class PoiseListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(PoiseEnchant.KEY);
    private PoiseEnchant config;

    public PoiseListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(PoiseEnchant.KEY);
        this.config = (enchantObj instanceof PoiseEnchant) ? (PoiseEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(attacker);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) return;

        if (config.isRequireOnGround() && !attacker.isOnGround()) return;
        if (config.isDisableWhileSprinting() && attacker instanceof Player player && player.isSprinting()) return;

        Vector velocity = attacker.getVelocity();
        if (velocity != null && velocity.lengthSquared() > config.getMovementThresholdSquared()) return;

        double bonusDamage = Math.min(config.getMaxBonusDamage(), level * config.getBonusDamagePerLevel());
        if (bonusDamage <= 0.0) return;

        event.setDamage(event.getDamage() + bonusDamage);
    }
}
