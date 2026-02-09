package com.enadd.core.enchantment;

import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import com.enadd.core.enchantment.EnchantmentEffectManager.EffectTrigger;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import java.util.Map;



/**
 * 附魔事件处理器 - 监听所有游戏事件并触发附魔效果
 */
public final class EnchantmentEventHandler implements Listener {

    private final EnchantmentEffectManager effectManager;

    public EnchantmentEventHandler(JavaPlugin plugin) {
        this.effectManager = EnchantmentEffectManager.getInstance();
    }

    /**
     * 处理攻击事件
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 近战攻击
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            ItemStack weapon = attacker.getInventory().getItemInMainHand();

            if (weapon != null && weapon.getType() != Material.AIR) {
                processEnchantments(weapon, attacker, event.getEntity(), event, EffectTrigger.ATTACK);
            }
        }

        // 远程攻击
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            ProjectileSource shooter = projectile.getShooter();

            if (shooter instanceof Player) {
                Player attacker = (Player) shooter;
                ItemStack weapon = attacker.getInventory().getItemInMainHand();

                if (weapon != null && weapon.getType() != Material.AIR) {
                    processEnchantments(weapon, attacker, event.getEntity(), event, EffectTrigger.HIT);
                }
            }
        }

        // 防御
        if (event.getEntity() instanceof Player) {
            Player defender = (Player) event.getEntity();

            // 检查护甲附魔
            for (ItemStack armor : defender.getInventory().getArmorContents()) {
                if (armor != null && armor.getType() != Material.AIR) {
                    processEnchantments(armor, defender, event.getDamager(), event, EffectTrigger.DEFEND);
                }
            }
        }
    }

    /**
     * 处理玩家受伤事件
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        // 检查护甲附魔
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) {
                processEnchantments(armor, player, null, event, EffectTrigger.HURT);
            }
        }
    }

    /**
     * 处理方块破坏事件
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (tool != null && tool.getType() != Material.AIR) {
            processEnchantments(tool, player, null, event, EffectTrigger.MINE);
        }
    }

    /**
     * 处理射击事件
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();

        if (bow != null) {
            processEnchantments(bow, player, null, event, EffectTrigger.SHOOT);
        }
    }

    /**
     * 处理击杀事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon != null && weapon.getType() != Material.AIR) {
            processEnchantments(weapon, killer, event.getEntity(), event, EffectTrigger.KILL);
        }
    }

    /**
     * 处理玩家移动事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // 只在实际移动时触发（不是视角转动）
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        // 检查靴子附魔
        ItemStack boots = player.getInventory().getBoots();
        if (boots != null && boots.getType() != Material.AIR) {
            processEnchantments(boots, player, null, event, EffectTrigger.MOVE);
        }
    }

    /**
     * 处理玩家跳跃事件
     */
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJump(PlayerMoveEvent event) {
        // 检测跳跃（Y坐标增加且不在地面）
        if (event.getTo().getY() > event.getFrom().getY() && !event.getPlayer().isOnGround()) {
            Player player = event.getPlayer();

            // 检查靴子附魔
            ItemStack boots = player.getInventory().getBoots();
            if (boots != null && boots.getType() != Material.AIR) {
                processEnchantments(boots, player, null, event, EffectTrigger.JUMP);
            }
        }
    }

    /**
     * 处理玩家潜行事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();

        // 检查护腿附魔
        ItemStack leggings = player.getInventory().getLeggings();
        if (leggings != null && leggings.getType() != Material.AIR) {
            processEnchantments(leggings, player, null, event, EffectTrigger.SNEAK);
        }
    }

    /**
     * 处理玩家交互事件
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() != Material.AIR) {
            processEnchantments(item, player, null, event, EffectTrigger.INTERACT);
        }
    }

    /**
     * 处理物品消耗事件
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null) {
            processEnchantments(item, player, null, event, EffectTrigger.CONSUME);
        }
    }

    /**
     * 处理附魔
     */
    private void processEnchantments(ItemStack item, Player player, Entity target,
                                    org.bukkit.event.Event event, EffectTrigger trigger) {
        if (item == null || !item.hasItemMeta()) return;

        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if (enchantments.isEmpty()) return;

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            String enchantId = getEnchantmentId(entry.getKey());
            int level = entry.getValue();

            EffectContext context = new EffectContext(player, target, item, level, event, trigger);
            effectManager.applyEffect(enchantId, context);
        }
    }

    /**
     * 获取附魔ID
     */
    private String getEnchantmentId(Enchantment enchantment) {
        String key = enchantment.getKey().getKey();
        // 移除命名空间前缀
        if (key.contains(":")) {
            key = key.substring(key.indexOf(":") + 1);
        }
        return key.toLowerCase();
    }
}
