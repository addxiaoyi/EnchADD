package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.ParryEnchant;
import net.enchadd.listeners.support.ParryRetaliationSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;

@SuppressWarnings("UnstableApiUsage")
public class ParryListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(ParryEnchant.KEY);
    private NamespacedKey cooldownKey = PerformanceUtils.namespacedKeyWithSuffix(ParryEnchant.KEY, "_cooldown");
    private NamespacedKey windowKey = PerformanceUtils.namespacedKeyWithSuffix(ParryEnchant.KEY, "_window");
    private NamespacedKey targetKey = PerformanceUtils.namespacedKeyWithSuffix(ParryEnchant.KEY, "_target");
    private NamespacedKey levelKey = PerformanceUtils.namespacedKeyWithSuffix(ParryEnchant.KEY, "_level");
    private ParryEnchant config;
    private final ParryRetaliationSupport retaliationSupport = new ParryRetaliationSupport();

    public ParryListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(ParryEnchant.KEY);
        this.config = (enchantObj instanceof ParryEnchant) ? (ParryEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlocked(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getEntity() instanceof Player defender)) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!PerformanceUtils.isSuccessfulShieldBlock(defender, event)) return;

        ParryRetaliationSupport.ArmContext armContext =
                retaliationSupport.resolveArmContext(defender, attacker, enchant);
        if (armContext == null) return;
        retaliationSupport.armRetaliation(armContext, config, cooldownKey, windowKey, targetKey, levelKey);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCounterattack(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getDamager() instanceof Player defender)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        PersistentDataContainer pdc = retaliationSupport.resolvePdc(defender);
        if (pdc == null) return;
        retaliationSupport.applyCounterattack(
                pdc,
                target,
                config,
                windowKey,
                targetKey,
                levelKey,
                event.getDamage(),
                event::setDamage
        );
    }
}
