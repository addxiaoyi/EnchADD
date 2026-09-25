package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.LodestarEnchant;
import net.enchadd.listeners.support.EnchantDamageSupport;
import net.enchadd.utils.PerformanceUtils;
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
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("UnstableApiUsage")
public class LodestarListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(LodestarEnchant.KEY);
    private NamespacedKey windowKey = PerformanceUtils.namespacedKeyWithSuffix(LodestarEnchant.KEY, "_window");
    private NamespacedKey targetKey = PerformanceUtils.namespacedKeyWithSuffix(LodestarEnchant.KEY, "_target");
    private LodestarEnchant config;

    public LodestarListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(LodestarEnchant.KEY);
        this.config = (enchantObj instanceof LodestarEnchant) ? (LodestarEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (!Double.isFinite(event.getFinalDamage()) || event.getFinalDamage() <= 0.0) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(attacker);
        if (equipment == null) return;

        int level = Math.min(config.getMaxLevel(),
                PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant));
        if (level <= 0) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(attacker);
        if (pdc == null) return;

        int targetId = target.getEntityId();
        Integer previousTargetId = pdc.get(targetKey, PersistentDataType.INTEGER);
        if (previousTargetId != null
                && previousTargetId == targetId
                && PerformanceUtils.isWindowActive(pdc, windowKey)) {
            double bonusDamage = EnchantDamageSupport.bonusDamage(level,
                    config.getBonusDamagePerLevel(), config.getMaxBonusDamage());
            double adjusted = EnchantDamageSupport.addBonus(event.getDamage(), bonusDamage);
            if (adjusted > event.getDamage()) {
                event.setDamage(adjusted);
            }
        }

        pdc.set(targetKey, PersistentDataType.INTEGER, targetId);
        PerformanceUtils.setWindowUntilTicks(pdc, windowKey, config.getFocusWindowTicks());
    }
}
