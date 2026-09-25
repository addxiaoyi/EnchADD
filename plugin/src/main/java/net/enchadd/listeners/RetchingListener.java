package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.RetchingEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("UnstableApiUsage")
public class RetchingListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(RetchingEnchant.KEY);
    private RetchingEnchant config;

    public RetchingListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(RetchingEnchant.KEY);
        this.config = (enchantObj instanceof RetchingEnchant) ? (RetchingEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getEntity() instanceof Player player)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getHelmet(), enchant);
        if (level <= 0) return;

        int currentFood = player.getFoodLevel();
        int newFood = event.getFoodLevel();
        int gainedFood = newFood - currentFood;
        if (gainedFood <= 0) return;

        int reduction = Math.min(config.getMaxFoodGainReduction(), config.getFoodGainReductionPerLevel() * level);
        if (reduction > 0) {
            int adjustedFood = currentFood + Math.max(0, gainedFood - reduction);
            if (adjustedFood < newFood) {
                event.setFoodLevel(adjustedFood);
            }
        }

        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getNauseaSecondsPerLevel(), level);
        if (durationTicks <= 0) return;

        int amplifier = Math.min(1, Math.max(0, config.getNauseaAmplifier()));
        PotionEffect effect = new PotionEffect(PotionEffectType.NAUSEA, durationTicks, amplifier, false, false, true);
        player.addPotionEffect(effect);
    }
}
