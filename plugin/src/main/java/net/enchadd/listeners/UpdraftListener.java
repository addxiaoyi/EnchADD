package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.UpdraftEnchant;
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
public class UpdraftListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(UpdraftEnchant.KEY);
    private UpdraftEnchant config;

    public UpdraftListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(UpdraftEnchant.KEY);
        this.config = (enchantObj instanceof UpdraftEnchant) ? (UpdraftEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (attacker.isOnGround()) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(attacker);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) return;

        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getSlowFallingSecondsPerLevel(), level);
        if (durationTicks <= 0) return;

        int amplifier = Math.max(0, config.getSlowFallingAmplifier());
        PotionEffect effect = new PotionEffect(PotionEffectType.SLOW_FALLING, durationTicks, amplifier, false, false, true);
        attacker.addPotionEffect(effect);
    }
}
