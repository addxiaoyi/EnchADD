/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.registry.RegistryAccess
 *  io.papermc.paper.registry.RegistryKey
 *  org.bukkit.Location
 *  org.bukkit.NamespacedKey
 *  org.bukkit.Registry
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockFace
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.inventory.EntityEquipment
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.persistence.PersistentDataContainer
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.util.Vector
 */
package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.TideRunnerEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class TideRunnerListener
implements Listener {
    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = (Enchantment)this.registry.get(TideRunnerEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(TideRunnerEnchant.KEY);
    private final TideRunnerEnchant config;

    public TideRunnerListener() {
        EnchADDEnchant enchantObj = EnchADDConfig.ENCHANTS.get(TideRunnerEnchant.KEY);
        this.config = enchantObj instanceof TideRunnerEnchant ? (TideRunnerEnchant)enchantObj : null;
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        if (this.enchant == null || this.config == null) {
            return;
        }
        if (!event.hasChangedPosition()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSwimming()) {
            return;
        }
        if (!this.isInWater(player)) {
            return;
        }
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe((LivingEntity)player);
        if (equipment == null) {
            return;
        }
        ItemStack boots = equipment.getBoots();
        if (boots == null) {
            return;
        }
        int level = PerformanceUtils.getEnchantLevel(boots, this.enchant);
        if (level <= 0) {
            return;
        }
        if (!PerformanceUtils.shouldTick((Entity)player, 5)) {
            return;
        }
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe((Entity)player);
        if (pdc == null) {
            return;
        }
        if (PerformanceUtils.isOnCooldown(pdc, this.key, this.config.getCooldownTicks())) {
            return;
        }
        Vector delta = event.getTo().toVector().subtract(event.getFrom().toVector());
        if (delta.lengthSquared() < 0.01) {
            return;
        }
        int duration = Math.max(40, this.config.getGraceTicksPerLevel() * level);
        int amplifier = Math.max(0, (int)Math.round(this.config.getSpeedAmplifierPerLevel() * (double)level));
        PotionEffect effect = new PotionEffect(PotionEffectType.DOLPHINS_GRACE, duration, amplifier, false, false, true);
        player.addPotionEffect(effect);
        PerformanceUtils.setCooldown(pdc, this.key);
    }

    private boolean isInWater(Player player) {
        Location eye = player.getEyeLocation();
        Block block = eye.getBlock();
        if (block.isLiquid()) {
            return true;
        }
        Block above = block.getRelative(BlockFace.UP);
        return above.isLiquid();
    }
}
