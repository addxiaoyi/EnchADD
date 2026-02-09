package com.enadd.enchantments.special;

import com.enadd.config.EnchantmentConfig;
import com.enadd.core.api.IEnchantmentConfig;
import com.enadd.enchantments.BaseEnchantment;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import com.enadd.enchantments.Rarity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * 附魔粘合剂 - Enchantment Binder
 * 
 * 允许冲突的附魔共存
 * 
 * 等级效果:
 * - I级: 允许2个冲突附魔共存
 * - II级: 允许4个冲突附魔共存
 * - III级: 允许6个冲突附魔共存
 * 
 * 特性:
 * - 可以作用于原版冲突逻辑
 * - 可以让保护系附魔共存（爆炸保护+摔落保护）
 * - 可以让锋利、亡灵杀手、节肢杀手共存
 * - 可以让时运和精准采集共存
 * - 稀有度: 传说级
 * - 宝藏附魔: 是
 * - 诅咒: 否
 */
@SuppressWarnings("UnstableApiUsage")
public class EnchantmentBinderEnchantment extends BaseEnchantment {
    
    private static final int MAX_LEVEL = 3;
    private static final int CONFLICTS_PER_LEVEL = 2;
    
    @SuppressWarnings("removal")
    public EnchantmentBinderEnchantment() {
        super(
            Rarity.VERY_RARE,
            org.bukkit.enchantments.EnchantmentTarget.ALL,
            new EquipmentSlot[]{
                EquipmentSlot.HAND,
                EquipmentSlot.OFF_HAND,
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
            },
            EnchantmentConfig.BinderConfig.INSTANCE,
            "附魔粘合剂",
            "允许冲突的附魔强行共存，每级可粘合2个冲突附魔"
        );
    }
    
    @Override
    public @NotNull Component description() {
        return Component.text("§d§l✦ 附魔粘合剂 ✦\n" +
            "§7\n" +
            "§7打破附魔冲突的禁忌！\n" +
            "§7让不可能成为可能！\n" +
            "§7\n" +
            "§e✦ 等级效果:\n" +
            "§7  I级: 粘合 §e2个 §7冲突附魔\n" +
            "§7  II级: 粘合 §e4个 §7冲突附魔\n" +
            "§7  III级: 粘合 §e6个 §7冲突附魔\n" +
            "§7\n" +
            "§e✦ 可突破的冲突:\n" +
            "§7  • 保护系附魔共存\n" +
            "§7  • 伤害系附魔共存\n" +
            "§7  • 时运与精准采集共存\n" +
            "§7  • 无限与修补共存\n" +
            "§7  • 所有自定义冲突\n" +
            "§7\n" +
            "§c⚠ 注意:\n" +
            "§7  • 粘合剂本身占用一个附魔槽位\n" +
            "§7  • 超出粘合数量的冲突仍然无效\n" +
            "§7  • 极其稀有的传说级附魔\n" +
            "§7\n" +
            "§5§o「禁忌的力量，打破规则的存在」");
    }
    
    @Override
    public int getMaxLevel() {
        return MAX_LEVEL;
    }
    
    @Override
    public int getStartLevel() {
        return 1;
    }
    
    @Override
    @Deprecated
    @SuppressWarnings("removal")
    public boolean isTreasure() {
        return true;
    }
    
    @Override
    public boolean isTradeable() {
        return false;
    }
    
    @Override
    public boolean isDiscoverable() {
        return false;
    }
    
    @Override
    public int getAnvilCost() {
        return 10;
    }
    
    @Override
    public @NotNull RegistryKeySet<ItemType> getSupportedItems() {
        return RegistrySet.keySet(RegistryKey.ITEM);
    }
    
    @Override
    public @NotNull TypedKey<Enchantment> key() {
        return TypedKey.create(RegistryKey.ENCHANTMENT, this.key);
    }
    
    /**
     * 获取粘合剂允许的冲突数量
     */
    public static int getAllowedConflicts(int level) {
        return Math.min(level * CONFLICTS_PER_LEVEL, MAX_LEVEL * CONFLICTS_PER_LEVEL);
    }
    
    /**
     * 检查物品是否有粘合剂
     */
    public static boolean hasBinder(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        for (Enchantment ench : item.getEnchantments().keySet()) {
            if (ench.getKey().getKey().equals("enchantment_binder")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取粘合剂等级
     */
    public static int getBinderLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        
        for (Enchantment ench : item.getEnchantments().keySet()) {
            if (ench.getKey().getKey().equals("enchantment_binder")) {
                return item.getEnchantmentLevel(ench);
            }
        }
        return 0;
    }
    
    /**
     * 计算物品上的冲突数量
     */
    public static int countConflicts(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        
        Map<Enchantment, Integer> enchants = item.getEnchantments();
        if (enchants.size() <= 1) {
            return 0;
        }

        Set<String> conflictPairs = new HashSet<>();
        List<Enchantment> enchantList = new ArrayList<>(enchants.keySet());
        com.enadd.core.conflict.EnchantmentConflictManager manager = com.enadd.core.conflict.EnchantmentConflictManager.getInstance();

        for (int i = 0; i < enchantList.size(); i++) {
            for (int j = i + 1; j < enchantList.size(); j++) {
                Enchantment e1 = enchantList.get(i);
                Enchantment e2 = enchantList.get(j);
                
                boolean conflicting = false;
                if (manager != null && manager.isInitialized()) {
                    conflicting = manager.areConflicting(e1.getKey().toString(), e2.getKey().toString());
                } else {
                    conflicting = e1.conflictsWith(e2);
                }

                if (conflicting) {
                    String pair = e1.getKey().toString() + ":" + e2.getKey().toString();
                    conflictPairs.add(pair);
                }
            }
        }
        
        return conflictPairs.size();
    }
    
    /**
     * 检查是否可以添加附魔（考虑粘合剂）
     */
    public static boolean canAddEnchantment(ItemStack item, Enchantment newEnchant) {
        if (item == null || newEnchant == null) {
            return false;
        }
        
        int binderLevel = getBinderLevel(item);
        if (binderLevel == 0) {
            // 没有粘合剂，使用正常冲突检查
            return true;
        }
        
        int allowedConflicts = getAllowedConflicts(binderLevel);
        int currentConflicts = countConflicts(item);
        
        // 计算添加新附魔后的冲突数
        int newConflicts = 0;
        for (Enchantment existing : item.getEnchantments().keySet()) {
            if (existing.conflictsWith(newEnchant)) {
                newConflicts++;
            }
        }
        
        return (currentConflicts + newConflicts) <= allowedConflicts;
    }
}
