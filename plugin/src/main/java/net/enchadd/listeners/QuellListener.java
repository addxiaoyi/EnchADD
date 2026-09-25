package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.QuellEnchant;
import net.enchadd.listeners.support.EnchantDamageSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

@SuppressWarnings("UnstableApiUsage")
public class QuellListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(QuellEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(QuellEnchant.KEY);
    private final QuellEnchant config;

    public QuellListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(QuellEnchant.KEY);
        this.config = (enchantObj instanceof QuellEnchant) ? (QuellEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (enchant == null || config == null) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) return;

        ItemStack chestplate = equipment.getChestplate();
        if (chestplate == null) return;
        int level = Math.min(config.getMaxLevel(), PerformanceUtils.getEnchantLevel(chestplate, enchant));
        if (level <= 0) return;

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.MAGIC && cause != EntityDamageEvent.DamageCause.WITHER) return;
        double adjusted = EnchantDamageSupport.quellDamage(event.getDamage(), event.getFinalDamage(),
                level, config.getMaxLevel(), config.getReductionPerLevel());
        if (!(adjusted < event.getDamage())) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) return;
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) return;

        double chance = config.getTriggerChance() * level;
        if (!Double.isFinite(chance)) return;
        chance = Math.min(0.6d, Math.max(0.0d, chance));
        if (!PerformanceUtils.rollChance(chance)) return;

        event.setDamage(adjusted);
        PerformanceUtils.setCooldown(pdc, key);
    }
}
