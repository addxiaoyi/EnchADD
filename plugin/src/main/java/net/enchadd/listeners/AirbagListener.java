package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.AirbagEnchant;
import net.enchadd.listeners.support.AirbagImpactSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;

public class AirbagListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment airbag = registry.get(AirbagEnchant.KEY);
    private final AirbagEnchant config;
    private final AirbagImpactSupport impactSupport = new AirbagImpactSupport();

    public AirbagListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(AirbagEnchant.KEY);
        this.config = (enchantObj instanceof AirbagEnchant) ? (AirbagEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCushionedImpact(EntityDamageEvent event) {
        if (event.isCancelled() || !Double.isFinite(event.getFinalDamage()) || event.getFinalDamage() <= 0) return;
        if (airbag == null || config == null) return;
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (!impactSupport.isCushionedCause(cause)) return;

        double damage = event.getDamage();
        if (!Double.isFinite(damage) || damage <= 0.0) return;

        EntityEquipment entityEquipment = PerformanceUtils.getEquipmentSafe(livingEntity);
        if (entityEquipment == null) return;

        int levels = impactSupport.resolveArmorLevel(entityEquipment, airbag);
        if (levels <= 0) return;

        double percentageDamageReduction = impactSupport.resolveReduction(config, levels);
        if (percentageDamageReduction <= 0.0) return;

        impactSupport.applyImpactReduction(event, livingEntity, damage, percentageDamageReduction);
    }
}
