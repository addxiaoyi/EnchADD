package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.RallyEnchant;
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
public class RallyListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(RallyEnchant.KEY);
    private NamespacedKey windowKey = PerformanceUtils.namespacedKeyWithSuffix(RallyEnchant.KEY, "_window");
    private NamespacedKey levelKey = PerformanceUtils.namespacedKeyWithSuffix(RallyEnchant.KEY, "_level");
    private RallyEnchant config;

    public RallyListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(RallyEnchant.KEY);
        this.config = (enchantObj instanceof RallyEnchant) ? (RallyEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDamaged(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (!(event.getDamager() instanceof LivingEntity)) return;
        if (event.getFinalDamage() <= 0.0) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(victim);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getChestplate(), enchant);
        if (level <= 0) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(victim);
        if (pdc == null) return;

        PerformanceUtils.setWindowUntilTicks(pdc, windowKey, config.getRetaliationWindowTicks());
        pdc.set(levelKey, PersistentDataType.INTEGER, level);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCounterattack(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(attacker);
        if (pdc == null) return;
        if (!PerformanceUtils.isWindowActive(pdc, windowKey)) return;
        Integer level = pdc.get(levelKey, PersistentDataType.INTEGER);
        if (level == null || level <= 0) return;

        double bonusDamage = Math.min(config.getMaxBonusDamage(), level * config.getBonusDamagePerLevel());
        if (bonusDamage <= 0.0) return;

        event.setDamage(event.getDamage() + bonusDamage);
        pdc.remove(windowKey);
        pdc.remove(levelKey);
    }
}
