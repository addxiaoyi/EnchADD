package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.RashnessEnchant;
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

@SuppressWarnings("UnstableApiUsage")
public class RashnessListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(RashnessEnchant.KEY);
    private RashnessEnchant config;

    public RashnessListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(RashnessEnchant.KEY);
        this.config = (enchantObj instanceof RashnessEnchant) ? (RashnessEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(attacker);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) return;
        if (attacker.getAttackCooldown() >= config.getRequiredAttackCooldown()) return;

        double selfDamage = Math.min(config.getMaxSelfDamage(), level * config.getSelfDamagePerLevel());
        if (selfDamage <= 0.0) return;

        attacker.damage(selfDamage);
    }
}
