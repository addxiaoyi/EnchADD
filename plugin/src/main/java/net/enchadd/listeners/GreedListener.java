package net.enchadd.listeners;

import net.enchadd.listeners.support.GreedEffectSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.GreedEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.enchadd.utils.PerformanceUtils;

@SuppressWarnings("UnstableApiUsage")
public class GreedListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(GreedEnchant.KEY);
    private final NamespacedKey untilKey = PerformanceUtils.namespacedKeyWithSuffix(GreedEnchant.KEY, "_vuln_until");
    private final NamespacedKey scaleKey = PerformanceUtils.namespacedKeyWithSuffix(GreedEnchant.KEY, "_vuln_scale");
    private final GreedEnchant config;
    private GreedEffectSupport effectSupport;

    public GreedListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(GreedEnchant.KEY);
        this.config = (enchantObj instanceof GreedEnchant) ? (GreedEnchant) enchantObj : null;
        this.effectSupport = this.config == null ? null : new GreedEffectSupport(this.config, untilKey, scaleKey);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (enchant == null || config == null || effectSupport == null) return;
        if (event.getDamageSource() == null || event.getDamageSource().isIndirect()) return;

        Entity killerEntity = event.getDamageSource().getCausingEntity();
        if (!(killerEntity instanceof Player killer)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(killer);
        if (equipment == null) return;

        ItemStack mainHand = equipment.getItemInMainHand();
        if (mainHand == null) return;

        int level = PerformanceUtils.getEnchantLevel(mainHand, enchant);
        if (level <= 0) return;

        effectSupport.applyXpBonus(event, level);
        effectSupport.applyVulnerability(killer, level);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (enchant == null || config == null || effectSupport == null) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) return;

        int level = PerformanceUtils.getSumOfEnchantLevels(equipment, enchant);
        if (level <= 0) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) return;

        effectSupport.applyActiveDamageScale(event, pdc);
    }
}
