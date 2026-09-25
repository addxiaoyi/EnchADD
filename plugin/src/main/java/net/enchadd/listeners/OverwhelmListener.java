package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.OverwhelmEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("UnstableApiUsage")
public class OverwhelmListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(OverwhelmEnchant.KEY);
    private OverwhelmEnchant config;

    public OverwhelmListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(OverwhelmEnchant.KEY);
        this.config = (enchantObj instanceof OverwhelmEnchant) ? (OverwhelmEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (!target.isOnGround()) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(attacker);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) return;

        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getWeaknessSecondsPerLevel(), level);
        if (durationTicks <= 0) return;

        int amplifier = Math.min(1, Math.max(0, config.getWeaknessAmplifier()));
        PotionEffect effect = new PotionEffect(PotionEffectType.WEAKNESS, durationTicks, amplifier, false, false, true);
        target.addPotionEffect(effect);
    }
}
