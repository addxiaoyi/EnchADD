package net.enchadd.listeners.support;

import net.enchadd.enchants.TideRunnerEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
    private static final double MIN_MOVEMENT_DELTA_SQUARED = 0.01;

    private final TideRunnerEnchant config;
    private final NamespacedKey cooldownKey;

    public TideRunnerEffectSupport(@NotNull TideRunnerEnchant config, @NotNull NamespacedKey cooldownKey) {
        this.config = config;
        this.cooldownKey = cooldownKey;
    }

    public void handleMove(@NotNull PlayerMoveEvent event, @NotNull Player player, @NotNull Enchantment enchant) {
        if (!isInWater(player)) {
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
        if (!hasMovementDelta(event.getFrom(), event.getTo())) {
            return;
        }

        int duration = Math.max(40, config.getGraceTicksPerLevel() * level);
        int amplifier = Math.max(0, (int) Math.round(config.getSpeedAmplifierPerLevel() * level));
        PotionEffect effect = new PotionEffect(
                PotionEffectType.DOLPHINS_GRACE,
                duration,
                amplifier,
                false,
                false,
                true
        );
        player.addPotionEffect(effect);
        PerformanceUtils.setCooldown(pdc, cooldownKey);
    }

    public boolean isInWater(@NotNull Player player) {
        Location eye = player.getEyeLocation();
        Block block = eye.getBlock();
        if (block.isLiquid()) {
            return true;
        }
        Block above = block.getRelative(BlockFace.UP);
        return above.isLiquid();
    }

    private static boolean hasMovementDelta(@NotNull Location from, @Nullable Location to) {
        if (to == null) {
            return false;
        }
        Vector delta = to.toVector().subtract(from.toVector());
        return delta.lengthSquared() >= MIN_MOVEMENT_DELTA_SQUARED;
    }
}
