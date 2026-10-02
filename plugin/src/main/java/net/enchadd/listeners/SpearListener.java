package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.SpearEnchant;
import net.enchadd.listeners.support.SpearCombatSupport;
import net.enchadd.listeners.support.SpearItemSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class SpearListener implements Listener {

    private final Enchantment enchant;
    private final SpearEnchant config;
    private final NamespacedKey cooldownKey;

    public SpearListener(@NotNull net.kyori.adventure.key.Key key) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        this.enchant = registry.get(key);
        Object configured = EnchADDConfig.ENCHANTS.get(key);
        this.config = configured instanceof SpearEnchant spear ? spear : null;
        this.cooldownKey = PerformanceUtils.namespacedKeyWithSuffix(key, "_cooldown");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null || event.isCancelled()) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        Entity damager = event.getDamager();
        if (!(damager instanceof LivingEntity attacker) || !isDirectMelee(event, damager)) {
            return;
        }

        if (config.getMode() == SpearEnchant.Mode.COUNTERTHRUST) {
            applyCounterthrust(event, target);
            return;
        }

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(attacker);
        if (equipment == null || !SpearItemSupport.isSpear(equipment.getItemInMainHand())) {
            return;
        }
        int level = Math.min(config.getMaxLevel(), PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant));
        if (level <= 0) {
            return;
        }
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(attacker);
        if (pdc == null || PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) {
            return;
        }

        switch (config.getMode()) {
            case LANCER -> applyLancer(event, attacker, pdc, level);
            case REACH -> applyReach(event, attacker, target, pdc, level);
            case SKEWER -> applySkewer(target, attacker, pdc, level);
            default -> {
            }
        }
    }

    private void applyLancer(EntityDamageByEntityEvent event, LivingEntity attacker,
                             PersistentDataContainer pdc, int level) {
        if (!(attacker instanceof Player player) || !player.isSprinting()) {
            return;
        }
        double adjusted = SpearCombatSupport.damageBonus(event.getDamage(), level,
                config.getPowerPerLevel(), config.getMaxPower(), 1.0);
        if (adjusted <= event.getDamage()) {
            return;
        }
        event.setDamage(adjusted);
        PerformanceUtils.setCooldown(pdc, cooldownKey);
    }

    private void applyReach(EntityDamageByEntityEvent event, LivingEntity attacker, LivingEntity target,
                            PersistentDataContainer pdc, int level) {
        if (!attacker.getWorld().equals(target.getWorld())) {
            return;
        }
        double distance = attacker.getLocation().distance(target.getLocation());
        double factor = SpearCombatSupport.reachFactor(distance, config.getMinDistance(), config.getMaxDistance());
        double adjusted = SpearCombatSupport.damageBonus(event.getDamage(), level,
                config.getPowerPerLevel(), config.getMaxPower(), factor);
        if (adjusted <= event.getDamage()) {
            return;
        }
        event.setDamage(adjusted);
        PerformanceUtils.setCooldown(pdc, cooldownKey);
    }

    private void applySkewer(LivingEntity target, LivingEntity attacker,
                             PersistentDataContainer pdc, int level) {
        double chance = Math.min(0.75, Math.min(config.getMaxPower(), config.getTriggerChance() * level));
        if (!PerformanceUtils.rollChance(chance)) {
            return;
        }
        int duration = SpearCombatSupport.durationTicks(level, config.getMaxLevel(),
                config.getDurationTicksPerLevel(), (int) config.getMaxPower());
        if (duration <= 0 || !target.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS, duration, Math.min(1, Math.max(0, level - 1)), false, false, true))) {
            return;
        }
        PerformanceUtils.setCooldown(pdc, cooldownKey);
    }

    private void applyCounterthrust(EntityDamageByEntityEvent event, LivingEntity defender) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(defender);
        if (equipment == null || !defender.isSneaking() || !SpearItemSupport.isSpear(equipment.getItemInMainHand())) {
            return;
        }
        int level = Math.min(config.getMaxLevel(), PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant));
        if (level <= 0) {
            return;
        }
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(defender);
        if (pdc == null || PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) {
            return;
        }
        double adjusted = SpearCombatSupport.reduceDamage(event.getDamage(), level,
                config.getPowerPerLevel(), config.getMaxPower());
        if (adjusted >= event.getDamage()) {
            return;
        }
        event.setDamage(adjusted);
        PerformanceUtils.setCooldown(pdc, cooldownKey);
    }

    private static boolean isDirectMelee(EntityDamageByEntityEvent event, Entity damager) {
        if (event.getDamageSource() == null) {
            return true;
        }
        Entity direct = event.getDamageSource().getDirectEntity();
        Entity causing = event.getDamageSource().getCausingEntity();
        return !event.getDamageSource().isIndirect()
                && (direct == null || direct.equals(damager))
                && (causing == null || causing.equals(damager));
    }
}