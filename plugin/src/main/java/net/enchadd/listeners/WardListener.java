package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.WardEnchant;
import net.enchadd.listeners.support.WardShieldSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WardListener implements Listener {

    private final Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment ward = registry.get(WardEnchant.KEY);
    private final NamespacedKey wardKey = PerformanceUtils.namespacedKey(WardEnchant.KEY);
    private final WardShieldSupport shieldSupport = new WardShieldSupport();

    private final WardEnchant config;

    public WardListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(WardEnchant.KEY);
        this.config = (enchantObj instanceof WardEnchant) ? (WardEnchant) enchantObj : null;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageWithWard(EntityDamageByEntityEvent event) {
        if (event.isCancelled() || !Double.isFinite(event.getFinalDamage()) || event.getFinalDamage() <= 0) return;
        if (ward == null || config == null) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!PerformanceUtils.isSuccessfulShieldBlock(player, event)) return;

        ItemStack shield = shieldSupport.resolveWardShield(player, ward);
        if (shield == null) return;
        if (shieldSupport.isOnCooldown(player, shield, wardKey, config)) return;
        int durabilityCost = shieldSupport.resolveDurabilityCost(shield, event.getFinalDamage());
        if (durabilityCost <= 0) return;

        shieldSupport.triggerCooldown(player, shield, wardKey, config);
        shieldSupport.applyWardBlock(player, shield, event, config, durabilityCost);
    }
}
