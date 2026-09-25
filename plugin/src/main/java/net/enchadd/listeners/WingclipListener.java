package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.WingclipEnchant;
import net.enchadd.utils.PerformanceUtils;
import net.enchadd.listeners.support.EnchantDamageSupport;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;

@SuppressWarnings("UnstableApiUsage")
public class WingclipListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(WingclipEnchant.KEY);
    private WingclipEnchant config;

    public WingclipListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(WingclipEnchant.KEY);
        this.config = (enchantObj instanceof WingclipEnchant) ? (WingclipEnchant) enchantObj : null;
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
        if (target.isOnGround()) return;

        double bonusDamage = EnchantDamageSupport.bonusDamage(
                level, config.getBonusDamagePerLevel(), config.getMaxBonusDamage());
        double damage = event.getDamage();
        double adjusted = EnchantDamageSupport.addBonus(damage, bonusDamage);
        if (Double.isFinite(adjusted) && adjusted > damage) {
            event.setDamage(adjusted);
        }
    }
}
