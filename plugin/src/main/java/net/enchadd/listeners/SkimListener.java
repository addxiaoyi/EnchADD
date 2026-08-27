package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.SkimEnchant;
import net.enchadd.listeners.support.SkimImpactSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;

@SuppressWarnings("UnstableApiUsage")
public class SkimListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(SkimEnchant.KEY);
    private final SkimEnchant config;
    private final SkimImpactSupport impactSupport;

    public SkimListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(SkimEnchant.KEY);
        this.config = (enchantObj instanceof SkimEnchant) ? (SkimEnchant) enchantObj : null;
        this.impactSupport = this.config == null ? null : new SkimImpactSupport();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onWallImpact(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
            return;
        }
        if (enchant == null || config == null) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return;
        }

        if (impactSupport != null) {
            impactSupport.handleWallImpact(event, equipment, enchant, config);
        }
    }
}
