package com.enadd.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



/**
 * ItemMeta辅助类 - 使用现代Adventure API替代弃用方法
 */
public class ItemMetaHelper {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();

    /**
     * 设置物品显示名称（使用Adventure API）
     */
    public static void setDisplayName(ItemMeta meta, String legacyText) {
        // Bug #508: 添加参数验证
        if (meta == null) {
            return;
        }

        // Bug #509: 处理null和空字符串
        if (legacyText == null || legacyText.isEmpty()) {
            meta.displayName(null); // 清除显示名称
            return;
        }

        try {
            // Bug #510: 添加异常处理
            Component component = SERIALIZER.deserialize(legacyText);
            if (component != null) {
                meta.displayName(component);
            }
        } catch (Exception e) {
            // Bug #511: 序列化失败时的后备处理
            System.err.println("设置显示名称失败: " + e.getMessage());
        }
    }

    /**
     * 设置物品Lore（使用Adventure API）
     */
    public static void setLore(ItemMeta meta, List<String> legacyLore) {
        // Bug #512: 添加参数验证
        if (meta == null) {
            return;
        }

        // Bug #513: 处理null列表
        if (legacyLore == null) {
            meta.lore(null); // 清除lore
            return;
        }

        // Bug #514: 处理空列表
        if (legacyLore.isEmpty()) {
            meta.lore(new ArrayList<>());
            return;
        }

        try {
            // Bug #515: 过滤null元素并添加异常处理
            List<Component> componentLore = legacyLore.stream()
                .filter(line -> line != null) // 过滤null行
                .map(line -> {
                    try {
                        return SERIALIZER.deserialize(line);
                    } catch (Exception e) {
                        // Bug #516: 单行序列化失败时返回空Component
                        return Component.text(line);
                    }
                })
                .filter(component -> component != null) // 过滤null组件
                .collect(Collectors.toList());

            meta.lore(componentLore);
        } catch (Exception e) {
            // Bug #517: 整体失败时的后备处理
            System.err.println("设置Lore失败: " + e.getMessage());
        }
    }

    /**
     * 获取物品显示名称（使用Adventure API）
     */
    public static String getDisplayName(ItemMeta meta) {
        // Bug #518: 添加参数验证
        if (meta == null) {
            return null;
        }

        try {
            Component component = meta.displayName();
            // Bug #519: 检查component是否为null
            if (component == null) {
                return null;
            }

            // Bug #520: 添加异常处理
            return SERIALIZER.serialize(component);
        } catch (Exception e) {
            // Bug #521: 序列化失败时返回null
            System.err.println("获取显示名称失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取物品显示名称为字符串（使用Adventure API）
     */
    public static String getDisplayNameAsString(ItemMeta meta) {
        // Bug #522: 直接调用getDisplayName，保持一致性
        return getDisplayName(meta);
    }

    /**
     * 获取物品Lore（使用Adventure API）
     */
    public static List<String> getLore(ItemMeta meta) {
        // Bug #523: 添加参数验证
        if (meta == null) {
            return null;
        }

        try {
            List<Component> componentLore = meta.lore();

            // Bug #524: 检查componentLore是否为null
            if (componentLore == null) {
                return null;
            }

            // Bug #525: 处理空列表
            if (componentLore.isEmpty()) {
                return new ArrayList<>();
            }

            // Bug #526: 过滤null元素并添加异常处理
            return componentLore.stream()
                .filter(component -> component != null) // 过滤null组件
                .map(component -> {
                    try {
                        return SERIALIZER.serialize(component);
                    } catch (Exception e) {
                        // Bug #527: 单行序列化失败时返回空字符串
                        return "";
                    }
                })
                .filter(line -> line != null && !line.isEmpty()) // 过滤空行
                .collect(Collectors.toList());
        } catch (Exception e) {
            // Bug #528: 整体失败时返回null
            System.err.println("获取Lore失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取物品Lore为字符串列表（使用Adventure API）
     */
    public static List<String> getLoreAsStrings(ItemMeta meta) {
        // Bug #529: 直接调用getLore，保持一致性
        return getLore(meta);
    }

    /**
     * 检查物品是否有Lore
     */
    public static boolean hasLore(ItemMeta meta) {
        // Bug #530: 添加参数验证
        if (meta == null) {
            return false;
        }

        try {
            List<Component> lore = meta.lore();
            // Bug #531: 更严格的检查
            return lore != null && !lore.isEmpty();
        } catch (Exception e) {
            // Bug #532: 异常时返回false
            return false;
        }
    }

    // Bug #533: 添加检查是否有显示名称的方法
    public static boolean hasDisplayName(ItemMeta meta) {
        if (meta == null) {
            return false;
        }

        try {
            Component displayName = meta.displayName();
            return displayName != null;
        } catch (Exception e) {
            return false;
        }
    }

    // Bug #534: 添加清除显示名称的方法
    public static void clearDisplayName(ItemMeta meta) {
        if (meta == null) {
            return;
        }

        try {
            meta.displayName(null);
        } catch (Exception e) {
            System.err.println("清除显示名称失败: " + e.getMessage());
        }
    }

    // Bug #535: 添加清除Lore的方法
    public static void clearLore(ItemMeta meta) {
        if (meta == null) {
            return;
        }

        try {
            meta.lore(null);
        } catch (Exception e) {
            System.err.println("清除Lore失败: " + e.getMessage());
        }
    }

    // Bug #536: 添加添加Lore行的方法
    public static void addLoreLine(ItemMeta meta, String line) {
        if (meta == null || line == null) {
            return;
        }

        try {
            List<String> currentLore = getLore(meta);
            if (currentLore == null) {
                currentLore = new ArrayList<>();
            }
            currentLore.add(line);
            setLore(meta, currentLore);
        } catch (Exception e) {
            System.err.println("添加Lore行失败: " + e.getMessage());
        }
    }

    // Bug #537: 添加安全的序列化方法
    public static Component deserializeSafely(String legacyText) {
        if (legacyText == null || legacyText.isEmpty()) {
            return Component.empty();
        }

        try {
            Component component = SERIALIZER.deserialize(legacyText);
            return component != null ? component : Component.empty();
        } catch (Exception e) {
            // 失败时返回纯文本Component
            return Component.text(legacyText);
        }
    }

    // Bug #538: 添加安全的反序列化方法
    public static String serializeSafely(Component component) {
        if (component == null) {
            return "";
        }

        try {
            String result = SERIALIZER.serialize(component);
            return result != null ? result : "";
        } catch (Exception e) {
            return "";
        }
    }
}
