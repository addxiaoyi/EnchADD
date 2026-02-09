package com.enadd.listeners;

import com.enadd.enchantments.special.EnchantmentBinderEnchantment;
import com.enadd.core.conflict.EnchantmentConflictManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;

/**
 * 附魔粘合剂监听器
 * 
 * 功能：
 * - 阻止未经粘合剂允许的冲突附魔
 * - 检查粘合剂槽位限制
 * - 防止刷附魔漏洞
 * 
 * 修复漏洞：
 * - 粘合剂无限制刷附魔漏洞
 * - 冲突检查绕过漏洞
 * - 铁砧刷附魔漏洞
 */
public final class EnchantmentBinderListener implements Listener {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    
    public EnchantmentBinderListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * 监听铁砧合成
     * 这是最主要的漏洞点 - 玩家可以通过铁砧添加冲突附魔
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        try {
            ItemStack result = event.getResult();
            // BUG FIX #5: 更早检查null
            if (result == null) {
                return;
            }
            
            if (!result.hasItemMeta()) {
                return;
            }
            
            // 获取所有附魔
            Map<Enchantment, Integer> enchants = result.getEnchantments();
            // BUG FIX #6: 添加防御性检查
            if (enchants == null || enchants.size() <= 1) {
                return; // 只有一个附魔，不需要检查
            }
            
            // BUG FIX #4: 使用统一的冲突计数
            int conflictCount = EnchantmentBinderEnchantment.countConflicts(result);
            
            if (conflictCount == 0) {
                return; // 没有冲突，允许
            }
            
            // 有冲突，检查粘合剂
            // BUG FIX #1: 使用实际存在的方法
            int binderLevel = getBinderLevel(result);
            if (binderLevel == 0) {
                // 没有粘合剂，阻止合成
                event.setResult(null);
                // BUG FIX #7: 改为fine级别
                logger.fine("Blocked anvil combination: No binder enchantment for conflicting enchants");
                // BUG FIX #9: 通知玩家
                if (event.getView().getPlayer() instanceof Player) {
                    Player player = (Player) event.getView().getPlayer();
                    player.sendMessage("§c无法合成：冲突的附魔需要粘合剂！");
                }
                return;
            }
            
            // 检查粘合剂槽位是否足够
            int allowedConflicts = getAllowedConflicts(binderLevel);
            
            if (conflictCount > allowedConflicts) {
                // 超出粘合剂限制，阻止合成
                event.setResult(null);
                // BUG FIX #7: 改为fine级别
                logger.fine(String.format("Blocked anvil combination: %d conflicts exceed binder level %d limit (%d allowed)",
                    conflictCount, binderLevel, allowedConflicts));
                // BUG FIX #9: 通知玩家
                if (event.getView().getPlayer() instanceof Player) {
                    Player player = (Player) event.getView().getPlayer();
                    player.sendMessage(String.format("§c粘合剂等级不足：需要 %d 级粘合剂才能容纳 %d 个冲突！", 
                        (conflictCount + 1) / 2, conflictCount));
                }
            }
            
        } catch (Exception e) {
            // BUG FIX #8: 捕获具体异常
            logger.severe("Error in EnchantmentBinderListener.onPrepareAnvil: " + e.getMessage());
            e.printStackTrace();
            // 出错时阻止合成，防止漏洞利用
            event.setResult(null);
        }
    }
    
    /**
     * BUG FIX #1: 实现缺失的方法
     * 获取物品上的粘合剂等级
     */
    private int getBinderLevel(ItemStack item) {
        return EnchantmentBinderEnchantment.getBinderLevel(item);
    }
    
    /**
     * BUG FIX #1: 实现缺失的方法
     * 根据粘合剂等级计算允许的冲突数量
     */
    private int getAllowedConflicts(int binderLevel) {
        return EnchantmentBinderEnchantment.getAllowedConflicts(binderLevel);
    }
    
    /**
     * 监听附魔台附魔
     * 防止附魔台给出冲突附魔
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        try {
            ItemStack item = event.getItem();
            Map<Enchantment, Integer> toAdd = event.getEnchantsToAdd();
            
            if (toAdd == null || toAdd.isEmpty()) {
                return;
            }
            
            // BUG FIX #11: 添加粘合剂检查
            // 检查物品当前的粘合剂等级
            int currentBinderLevel = getBinderLevel(item);
            
            // 检查新附魔是否与现有附魔冲突
            for (Enchantment newEnch : toAdd.keySet()) {
                for (Enchantment existing : item.getEnchantments().keySet()) {
                    if (conflictsWith(newEnch, existing)) {
                        // 如果有冲突但没有粘合剂，阻止
                        if (currentBinderLevel == 0) {
                            event.setCancelled(true);
                            // BUG FIX #7: 改为fine级别
                            logger.fine(String.format("Blocked enchanting table: %s conflicts with %s (no binder)",
                                newEnch.getKey().getKey(), existing.getKey().getKey()));
                            // BUG FIX #9: 通知玩家
                            event.getEnchanter().sendMessage("§c附魔台无法添加冲突的附魔！请使用铁砧和粘合剂。");
                            return;
                        }
                    }
                }
            }
            
            // 检查新附魔之间是否冲突
            Enchantment[] newEnchs = toAdd.keySet().toArray(new Enchantment[0]);
            for (int i = 0; i < newEnchs.length; i++) {
                for (int j = i + 1; j < newEnchs.length; j++) {
                    if (conflictsWith(newEnchs[i], newEnchs[j])) {
                        event.setCancelled(true);
                        // BUG FIX #7: 改为fine级别
                        logger.fine(String.format("Blocked enchanting table: %s conflicts with %s",
                            newEnchs[i].getKey().getKey(), newEnchs[j].getKey().getKey()));
                        // BUG FIX #9: 通知玩家
                        event.getEnchanter().sendMessage("§c附魔台无法同时添加冲突的附魔！");
                        return;
                    }
                }
            }
            
        } catch (Exception e) {
            // BUG FIX #8: 捕获具体异常
            logger.severe("Error in EnchantmentBinderListener.onEnchantItem: " + e.getMessage());
            e.printStackTrace();
            // 出错时阻止附魔，防止漏洞利用
            event.setCancelled(true);
        }
    }
    
    /**
     * 监听附魔台准备
     * 在附魔选项显示前检查
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
        try {
            ItemStack item = event.getItem();
            if (item == null || !item.hasItemMeta()) {
                return;
            }
            
            // 如果物品已经有冲突附魔但没有足够的粘合剂
            // 不允许继续附魔
            Map<Enchantment, Integer> existing = item.getEnchantments();
            if (existing.size() <= 1) {
                return;
            }
            
            int conflictCount = 0;
            for (Enchantment e1 : existing.keySet()) {
                for (Enchantment e2 : existing.keySet()) {
                    if (e1 != e2 && conflictsWith(e1, e2)) {
                        conflictCount++;
                    }
                }
            }
            conflictCount = conflictCount / 2;
            
            if (conflictCount > 0) {
                int binderLevel = EnchantmentBinderEnchantment.getBinderLevel(item);
                int allowedConflicts = EnchantmentBinderEnchantment.getAllowedConflicts(binderLevel);
                
                if (conflictCount > allowedConflicts) {
                    // 物品有非法的冲突附魔，不允许继续附魔
                    event.setCancelled(true);
                    logger.warning("Blocked enchanting: Item has illegal conflicting enchantments");
                }
            }
            
        } catch (Exception e) {
            logger.severe("Error in EnchantmentBinderListener.onPrepareItemEnchant: " + e.getMessage());
            e.printStackTrace();
            // 出错时阻止附魔，防止漏洞利用
            event.setCancelled(true);
        }
    }
    
    /**
     * 检查两个附魔是否冲突
     * 优先使用自定义冲突管理器，回退到原版检查
     */
    private boolean conflictsWith(Enchantment e1, Enchantment e2) {
        if (e1 == null || e2 == null || e1.equals(e2)) {
            return false;
        }
        
        try {
            // BUG FIX #2, #3: 修复方法调用
            EnchantmentConflictManager manager = EnchantmentConflictManager.getInstance();
            if (manager != null && manager.isInitialized()) {
                // BUG FIX #3: 转换Enchantment到String
                String id1 = e1.getKey().toString();
                String id2 = e2.getKey().toString();
                return manager.areConflicting(id1, id2);
            }
        } catch (Exception ex) {
            logger.warning("Failed to use EnchantmentConflictManager, falling back to vanilla: " + ex.getMessage());
        }
        
        // 回退到原版冲突检查
        return e1.conflictsWith(e2);
    }
    
    /**
     * 获取监听器实例（用于注册）
     */
    public static EnchantmentBinderListener create(JavaPlugin plugin) {
        return new EnchantmentBinderListener(plugin);
    }
}
