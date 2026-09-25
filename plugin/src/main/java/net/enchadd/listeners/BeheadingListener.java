package net.enchadd.listeners;

import net.enchadd.listeners.support.BeheadingDropSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.BeheadingEnchant;
import net.enchadd.events.EntityBeheadEvent;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import net.enchadd.utils.PerformanceUtils;


@SuppressWarnings("UnstableApiUsage")
public class BeheadingListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment beheading = registry.get(BeheadingEnchant.KEY);
    private final BeheadingEnchant config;
    private final BeheadingDropSupport dropSupport = new BeheadingDropSupport();

    public BeheadingListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(BeheadingEnchant.KEY);
        this.config = (enchantObj instanceof BeheadingEnchant) ? (BeheadingEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBeheading(EntityDeathEvent event) {
        if (beheading == null || config == null) return;
        if (event.getDamageSource() == null) return;
        if (event.getDamageSource().isIndirect()) return;

        Entity killer = event.getDamageSource().getCausingEntity();
        if (killer == null) return;
        if (!(killer instanceof LivingEntity killerEntity)) return;

        EntityEquipment killerEquipment = PerformanceUtils.getEquipmentSafe(killerEntity);
        if (killerEquipment == null) return;

        int level = PerformanceUtils.getHighestEnchantLevel(killerEquipment, beheading);
        if (level == 0) return;

        double chance = level * config.getChanceToDropHeadPerLevel();
        if (!Double.isFinite(chance)) return;
        chance = Math.min(0.5d, Math.max(0.0d, chance));
        if (!PerformanceUtils.rollChance(chance)) return;

        ItemStack head = dropSupport.getHeadForEntity(event.getEntity(), event.getDrops());
        if (head == null) return;
        EntityBeheadEvent beheadEvent = new EntityBeheadEvent(event.getEntity(), head);
        Bukkit.getPluginManager().callEvent(beheadEvent);
        if (beheadEvent.isCancelled()) return;
        event.getDrops().add(beheadEvent.getHeadToDrop());
    }
}
