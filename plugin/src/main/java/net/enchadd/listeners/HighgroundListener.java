package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.HighgroundEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;

@SuppressWarnings("UnstableApiUsage")
public class HighgroundListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(HighgroundEnchant.KEY);
    private HighgroundEnchant config;

    public HighgroundListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(HighgroundEnchant.KEY);
        this.config = (enchantObj instanceof HighgroundEnchant) ? (HighgroundEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(attacker);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) return;

        double heightAdvantage = attacker.getLocation().getY() - target.getLocation().getY();
        if (heightAdvantage < config.getRequiredHeightAdvantage()) return;

        double bonusDamage = Math.min(config.getMaxBonusDamage(), level * config.getBonusDamagePerLevel());
        if (bonusDamage <= 0.0) return;

        event.setDamage(event.getDamage() + bonusDamage);
    }
}
