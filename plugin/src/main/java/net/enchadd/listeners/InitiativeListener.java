package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.InitiativeEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;

@SuppressWarnings("UnstableApiUsage")
public class InitiativeListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(InitiativeEnchant.KEY);
    private InitiativeEnchant config;

    public InitiativeListener() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(InitiativeEnchant.KEY);
        this.config = (enchantObj instanceof InitiativeEnchant) ? (InitiativeEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(attacker);
        if (equipment == null) return;

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) return;

        AttributeInstance maxHealthAttribute = target.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute == null) return;

        double healthFraction = PerformanceUtils.safeDivide(target.getHealth(), maxHealthAttribute.getValue(), 0.0);
        if (healthFraction < config.getRequiredTargetHealthFraction()) return;

        double bonusDamage = Math.min(config.getMaxBonusDamage(), level * config.getBonusDamagePerLevel());
        if (bonusDamage <= 0.0) return;

        event.setDamage(event.getDamage() + bonusDamage);
    }
}
