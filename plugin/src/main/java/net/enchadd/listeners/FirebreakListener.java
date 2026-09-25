package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.FirebreakEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;

import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class FirebreakListener implements Listener {

    private static final Set<EntityDamageEvent.DamageCause> FIRE_CAUSES = Set.of(
            EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.HOT_FLOOR,
            EntityDamageEvent.DamageCause.LAVA
    );

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(FirebreakEnchant.KEY);
    private FirebreakEnchant config;

    public FirebreakListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(FirebreakEnchant.KEY);
        this.config = (enchantObj instanceof FirebreakEnchant) ? (FirebreakEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCombust(EntityCombustEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(entity);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getChestplate(), enchant);
        if (level <= 0) return;

        float originalDuration = event.getDuration();
        if (originalDuration <= 0) return;

        double reduction = Math.max(0.0, Math.min(0.90, Math.min(
                config.getMaxCombustionReduction(),
                level * config.getCombustionReductionPerLevel()
        )));
        if (reduction <= 0.0) return;

        float adjustedDuration = Math.max(0.0f, (float) (originalDuration * (1.0 - reduction)));
        if (adjustedDuration >= originalDuration) return;

        event.setDuration(adjustedDuration);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFireDamage(EntityDamageEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!FIRE_CAUSES.contains(event.getCause())) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(entity);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getChestplate(), enchant);
        if (level <= 0) return;

        double reduction = Math.max(0.0, Math.min(0.90, Math.min(
                config.getMaxDirectDamageReduction(),
                level * config.getDirectDamageReductionPerLevel()
        )));
        if (reduction <= 0.0) return;

        event.setDamage(event.getDamage() * (1.0 - reduction));
    }
}
