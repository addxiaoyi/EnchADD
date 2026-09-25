package net.enchadd.listeners.support;

import net.enchadd.enchants.TideRunnerEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TideRunnerEffectSupport {

    private static final int TICK_MODULO = 5;

    private final TideRunnerEnchant config;
    private final NamespacedKey cooldownKey;

    public TideRunnerEffectSupport(@NotNull TideRunnerEnchant config, @NotNull NamespacedKey cooldownKey) {
        this.config = config;
        this.cooldownKey = cooldownKey;
    }

    public void handleMove(@NotNull PlayerMoveEvent event, @NotNull Player player, @NotNull Enchantment enchant) {
        if (event.isCancelled() || !hasMovementDelta(event.getFrom(), event.getTo()) || !isInWater(player)) {
            return;
        }

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return;
        }
        ItemStack boots = equipment.getBoots();
        if (boots == null) {
            return;
        }

        int level = PerformanceUtils.getEnchantLevel(boots, enchant);
        if (level <= 0) {
            return;
        }
        if (!PerformanceUtils.shouldTick(player, TICK_MODULO)) {
            return;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return;
        }
        if (PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) {
            return;
        }
        int duration = TideRunnerRules.duration(level, config.getMaxLevel(), config.getGraceTicksPerLevel());
        if (duration <= 0) return;
        int amplifier = TideRunnerRules.amplifier(level, config.getMaxLevel(), config.getSpeedAmplifierPerLevel());
        PotionEffect current = player.getPotionEffect(PotionEffectType.DOLPHINS_GRACE);
        if (current != null && !ActiveBuffSupport.canUpgrade(current.getAmplifier(), current.getDuration(),
                amplifier, duration)) return;
        PotionEffect effect = new PotionEffect(
                PotionEffectType.DOLPHINS_GRACE,
                duration,
                amplifier,
                false,
                false,
                true
        );
        if (player.addPotionEffect(effect)) PerformanceUtils.setCooldown(pdc, cooldownKey);
    }

    public boolean isInWater(@NotNull Player player) {
        return containsWater(player.getLocation().getBlock()) || containsWater(player.getEyeLocation().getBlock());
    }

    private static boolean containsWater(Block block) {
        boolean waterlogged = block.getBlockData() instanceof Waterlogged water && water.isWaterlogged();
        return TideshellRules.containsWater(block.getType(), waterlogged);
    }

    private static boolean hasMovementDelta(@NotNull Location from, @Nullable Location to) {
        if (to == null || from.getWorld() == null || !from.getWorld().equals(to.getWorld())) {
            return false;
        }
        Vector delta = to.toVector().subtract(from.toVector());
        return TideRunnerRules.hasMovement(delta);
    }
}
