package com.enadd.enchantments.decorative;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理装饰性附魔的冲突和存储
 */
public final class DecorativeEnchantmentConflictManager {

    private final JavaPlugin plugin;
    private final NamespacedKey typeKey;
    private final NamespacedKey levelKey;
    private final Map<ItemStack, DecorativeEnchantment> activeEnchantments;

    public DecorativeEnchantmentConflictManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.typeKey = new NamespacedKey(plugin, "decorative_enchant_type");
        this.levelKey = new NamespacedKey(plugin, "decorative_enchant_level");
        this.activeEnchantments = new ConcurrentHashMap<>();
    }

    public boolean canApplyEnchantment(ItemStack item, DecorativeEnchantmentType newType) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        // 目前每个物品只允许一个装饰性附魔
        if (hasDecorativeEnchantment(item)) {
            DecorativeEnchantment existing = getDecorativeEnchantment(item);
            if (existing != null && existing.getType() == newType) {
                // 如果是同类型的，允许升级/降级（在这里视为允许应用）
                return true;
            }
            return false;
        }

        return true;
    }

    public String getConflictErrorMessage() {
        return "§c该物品已经拥有一个装饰性附魔，请先移除它。";
    }

    public boolean applyEnchantment(ItemStack item, DecorativeEnchantment enchantment) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(typeKey, PersistentDataType.STRING, enchantment.getType().getId());
        container.set(levelKey, PersistentDataType.INTEGER, enchantment.getLevel());

        item.setItemMeta(meta);
        activeEnchantments.put(item, enchantment);
        return true;
    }

    public boolean hasDecorativeEnchantment(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(typeKey, PersistentDataType.STRING);
    }

    public DecorativeEnchantment getDecorativeEnchantment(ItemStack item) {
        if (!hasDecorativeEnchantment(item)) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String typeId = container.get(typeKey, PersistentDataType.STRING);
        Integer level = container.get(levelKey, PersistentDataType.INTEGER);

        if (typeId == null || level == null) return null;

        DecorativeEnchantmentType type = DecorativeEnchantmentType.fromId(typeId);
        if (type == null) return null;

        return new DecorativeEnchantment(type, level);
    }

    public boolean removeDecorativeEnchantment(ItemStack item) {
        if (!hasDecorativeEnchantment(item)) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(typeKey);
        container.remove(levelKey);

        item.setItemMeta(meta);
        activeEnchantments.remove(item);
        return true;
    }
}
