package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.BarrierEnchant;
import net.enchadd.listeners.support.BarrierShieldSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

public class BarrierListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(BarrierEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(BarrierEnchant.KEY);
    private final BarrierEnchant config;
    private final BarrierShieldSupport shieldSupport;

    public BarrierListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(BarrierEnchant.KEY);
        this.config = (enchantObj instanceof BarrierEnchant) ? (BarrierEnchant) enchantObj : null;
        this.shieldSupport = this.config == null ? null : new BarrierShieldSupport(config);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onShieldHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }
        int level = getBarrierLevel(player, event);
        if (level <= 0) {
            return;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return;
        }
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) {
            return;
        }

        if (shieldSupport == null || !shieldSupport.shouldTrigger(level)) {
            return;
        }

        shieldSupport.knockbackNearby(player, level);
        PerformanceUtils.setCooldown(pdc, key);
    }

    private int getBarrierLevel(Player player, EntityDamageByEntityEvent event) {
        if (!PerformanceUtils.isSuccessfulShieldBlock(player, event)) {
            return 0;
        }
        if (player.getHandRaisedTime() <= 0 || player.getHandRaisedTime() > 10) {
            return 0;
        }
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType().isAir()) {
            return 0;
        }
        return PerformanceUtils.getEnchantLevel(offhand, enchant);
    }
}
