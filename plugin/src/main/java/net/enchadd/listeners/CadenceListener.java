package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.CadenceEnchant;
import net.enchadd.utils.PerformanceUtils;
import net.enchadd.listeners.support.EnchantDamageSupport;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;

@SuppressWarnings("UnstableApiUsage")
public class CadenceListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(CadenceEnchant.KEY);
    private NamespacedKey key = PerformanceUtils.namespacedKey(CadenceEnchant.KEY);
    private CadenceEnchant config;

    public CadenceListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(CadenceEnchant.KEY);
        this.config = (enchantObj instanceof CadenceEnchant) ? (CadenceEnchant) enchantObj : null;
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

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(attacker);
        if (pdc == null) return;
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) return;

        double bonus = EnchantDamageSupport.bonusDamage(
                level, config.getBonusDamagePerLevel(), config.getMaxBonusDamage());
        double damage = event.getDamage();
        double adjusted = EnchantDamageSupport.addBonus(damage, bonus);
        if (!Double.isFinite(adjusted) || adjusted <= damage) {
            return;
        }
        event.setDamage(adjusted);
        PerformanceUtils.setCooldown(pdc, key);
    }
}
