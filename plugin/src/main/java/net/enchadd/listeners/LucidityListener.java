package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.LucidityEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("UnstableApiUsage")
public class LucidityListener implements Listener {

    private static final Set<PotionEffectType> TARGETS = Set.of(
            PotionEffectType.BLINDNESS,
            PotionEffectType.DARKNESS,
            PotionEffectType.NAUSEA
    );

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(LucidityEnchant.KEY);
    private LucidityEnchant config;
    private final Set<UUID> suppress = ConcurrentHashMap.newKeySet();

    public LucidityListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(LucidityEnchant.KEY);
        this.config = (enchantObj instanceof LucidityEnchant) ? (LucidityEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (enchant == null || config == null) return;

        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        UUID uuid = entity.getUniqueId();
        if (suppress.remove(uuid)) return;

        PotionEffectType modified = event.getModifiedType();
        PotionEffect effect = event.getNewEffect();
        if (modified == null || effect == null || effect.isInfinite()) return;
        if (!TARGETS.contains(modified)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(entity);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getHelmet(), enchant);
        if (level <= 0) return;

        double reduction = Math.max(0.0, Math.min(0.90,
                Math.min(config.getMaxDurationReduction(), level * config.getDurationReductionPerLevel())));
        if (reduction <= 0.0) return;

        int originalDuration = effect.getDuration();
        if (originalDuration <= 1) return;

        int adjustedDuration = Math.max(1, (int) Math.floor(originalDuration * (1.0 - reduction)));
        if (adjustedDuration >= originalDuration) return;

        suppress.add(uuid);
        event.setCancelled(true);
        entity.addPotionEffect(effect.withDuration(adjustedDuration), true);
    }
}
