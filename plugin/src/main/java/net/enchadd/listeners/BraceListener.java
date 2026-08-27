package net.enchadd.listeners;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.BraceEnchant;
import net.enchadd.listeners.support.BraceKnockbackSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@SuppressWarnings("UnstableApiUsage")
public class BraceListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(BraceEnchant.KEY);
    private BraceEnchant config;
    private final BraceKnockbackSupport knockbackSupport = new BraceKnockbackSupport();

    public BraceListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(BraceEnchant.KEY);
        this.config = (enchantObj instanceof BraceEnchant) ? (BraceEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onKnockback(EntityKnockbackEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityKnockbackEvent.Cause.SHIELD_BLOCK) return;
        if (!PerformanceUtils.isLikelyShieldFacingBlock(player, event.getKnockback())) return;

        knockbackSupport.applyReduction(event, player, enchant, config);
    }
}
