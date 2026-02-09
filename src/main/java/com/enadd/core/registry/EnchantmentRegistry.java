package com.enadd.core.registry;

import com.enadd.core.enchantment.EnchantmentData;
import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.armor.ArmorEnchantmentLoader;
import com.enadd.enchantments.combat.CombatEnchantmentLoader;
import com.enadd.enchantments.curse.CurseEnchantmentLoader;
import com.enadd.enchantments.defense.DefenseEnchantmentLoader;
import com.enadd.enchantments.tool.ToolEnchantmentLoader;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentLoader;
import com.enadd.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import com.enadd.core.conflict.EnchantmentConflictManager;


/**
 * 附魔注册中心 - 管理所有自定义附魔的注册和数据
 */
public final class EnchantmentRegistry {

    private static final EnchantmentRegistry INSTANCE = new EnchantmentRegistry();
    private final Map<String, EnchantmentData> enchantments = new ConcurrentHashMap<>();
    private final Map<String, Enchantment> registeredEnchantments = new ConcurrentHashMap<>();

    private EnchantmentRegistry() {}

    public static EnchantmentRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * 注册所有附魔
     * 包含普通扩展附魔和增强版附魔
     */
    public static void registerAll() {
        EnchantmentRegistry registry = getInstance();

        if (registry == null) {
            Bukkit.getLogger().severe("无法获取EnchantmentRegistry实例！");
            return;
        }

        try {
            // 1. 注册增强版附魔
            EnhancedEnchantmentLoader.registerAll();
            Map<String, Enchantment> enhanced = EnhancedEnchantmentLoader.getAllEnchantments();
            if (enhanced != null && !enhanced.isEmpty()) {
                for (Map.Entry<String, Enchantment> entry : enhanced.entrySet()) {
                    if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                        registry.register(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("注册增强版附魔时出错: " + e.getMessage());
        }

        // 2. 注册各类扩展附魔

        // Armor
        try {
            ArmorEnchantmentLoader.registerAll();
            Map<String, Enchantment> armorEnchants = ArmorEnchantmentLoader.getAllEnchantments();
            if (armorEnchants != null && !armorEnchants.isEmpty()) {
                for (Map.Entry<String, Enchantment> entry : armorEnchants.entrySet()) {
                    if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                        registry.register(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("注册护甲附魔时出错: " + e.getMessage());
        }

        // Combat
        try {
            CombatEnchantmentLoader.registerAll();
            Map<String, Enchantment> combatEnchants = CombatEnchantmentLoader.getAllEnchantments();
            if (combatEnchants != null && !combatEnchants.isEmpty()) {
                for (Map.Entry<String, Enchantment> entry : combatEnchants.entrySet()) {
                    if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                        registry.register(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("注册战斗附魔时出错: " + e.getMessage());
        }

        // Curse
        try {
            CurseEnchantmentLoader.registerAll();
            Map<String, Enchantment> curseEnchants = CurseEnchantmentLoader.getAllEnchantments();
            if (curseEnchants != null && !curseEnchants.isEmpty()) {
                for (Map.Entry<String, Enchantment> entry : curseEnchants.entrySet()) {
                    if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                        registry.register(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("注册诅咒附魔时出错: " + e.getMessage());
        }

        // Defense
        try {
            DefenseEnchantmentLoader.registerAll();
            Map<String, Enchantment> defenseEnchants = DefenseEnchantmentLoader.getAllEnchantments();
            if (defenseEnchants != null && !defenseEnchants.isEmpty()) {
                for (Map.Entry<String, Enchantment> entry : defenseEnchants.entrySet()) {
                    if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                        registry.register(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("注册防御附魔时出错: " + e.getMessage());
        }

        // Tool
        try {
            ToolEnchantmentLoader.registerAll();
            Map<String, Enchantment> toolEnchants = ToolEnchantmentLoader.getAllEnchantments();
            if (toolEnchants != null && !toolEnchants.isEmpty()) {
                for (Map.Entry<String, Enchantment> entry : toolEnchants.entrySet()) {
                    if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                        registry.register(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("注册工具附魔时出错: " + e.getMessage());
        }

        // Special
        try {
            com.enadd.enchantments.special.SpecialEnchantmentLoader.registerAll();
            Map<String, Enchantment> specialEnchants = com.enadd.enchantments.special.SpecialEnchantmentLoader.getAllEnchantments();
            if (specialEnchants != null && !specialEnchants.isEmpty()) {
                for (Map.Entry<String, Enchantment> entry : specialEnchants.entrySet()) {
                    if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                        registry.register(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("注册特殊附魔时出错: " + e.getMessage());
        }

        // Utility
        try {
            com.enadd.enchantments.utility.UtilityEnchantmentLoader.registerAll();
            Map<String, Enchantment> utilityEnchants = com.enadd.enchantments.utility.UtilityEnchantmentLoader.getAllEnchantments();
            if (utilityEnchants != null && !utilityEnchants.isEmpty()) {
                for (Map.Entry<String, Enchantment> entry : utilityEnchants.entrySet()) {
                    if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                        registry.register(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("注册实用附魔时出错: " + e.getMessage());
        }

        int totalRegistered = registry.registeredEnchantments.size();
        if (totalRegistered > 0) {
            Bukkit.getLogger().info("已成功注册 " + totalRegistered + " 个自定义附魔");
        } else {
            Bukkit.getLogger().warning("警告：没有注册任何附魔！");
        }
    }

    /**
     * 注册单个附魔
     */
    public void register(String id, Enchantment enchantment) {
        // 参数验证
        if (id == null || id.trim().isEmpty()) {
            Bukkit.getLogger().warning("尝试注册附魔时ID为空，已跳过");
            return;
        }

        if (enchantment == null) {
            Bukkit.getLogger().warning("尝试注册附魔 " + id + " 时enchantment对象为null，已跳过");
            return;
        }

        // 检查是否被禁用
        try {
            if (ConfigManager.isEnchantmentDisabled(id)) {
                Bukkit.getLogger().info("§e跳过已禁用的附魔: " + id);
                return;
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("检查附魔 " + id + " 是否禁用时出错: " + e.getMessage());
            // 继续注册，假设未禁用
        }

        // 检查是否已注册
        if (registeredEnchantments.containsKey(id)) {
            Bukkit.getLogger().warning("附魔 " + id + " 已经注册过，将被覆盖");
        }

        registeredEnchantments.put(id, enchantment);

        // 如果是 BaseEnchantment，提取其数据
        if (enchantment instanceof BaseEnchantment) {
            try {
                BaseEnchantment base = (BaseEnchantment) enchantment;

                // 验证BaseEnchantment的数据
                String chineseName = base.getChineseName();
                String chineseDesc = base.getChineseDescription();

                if (chineseName == null || chineseName.trim().isEmpty()) {
                    chineseName = id; // 使用ID作为后备
                }

                if (chineseDesc == null || chineseDesc.trim().isEmpty()) {
                    chineseDesc = "无描述"; // 使用默认描述
                }

                String rarityName = "COMMON";
                try {
                    if (base.getLocalRarity() != null) {
                        rarityName = base.getLocalRarity().name();
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("获取附魔 " + id + " 的稀有度时出错: " + e.getMessage());
                }

                int maxLevel = 1;
                try {
                    maxLevel = base.getMaxLevel();
                    if (maxLevel < 1) {
                        maxLevel = 1;
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("获取附魔 " + id + " 的最大等级时出错: " + e.getMessage());
                }

                EnchantmentData data = new EnchantmentData(
                    id,
                    chineseName,
                    chineseDesc,
                    "CUSTOM",
                    rarityName,
                    maxLevel
                );
                enchantments.put(id, data);
            } catch (Exception e) {
                Bukkit.getLogger().warning("提取附魔 " + id + " 的数据时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 获取所有已注册的附魔（用于Bootstrap）
     */
    public Map<String, Enchantment> getAllRegisteredEnchantments() {
        return Collections.unmodifiableMap(registeredEnchantments);
    }

    public static Enchantment getEnchantment(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        EnchantmentRegistry instance = getInstance();
        if (instance == null || instance.registeredEnchantments == null) {
            return null;
        }

        return instance.registeredEnchantments.get(id);
    }

    public static int getCount() {
        EnchantmentRegistry instance = getInstance();
        if (instance == null || instance.registeredEnchantments == null) {
            return 0;
        }
        return instance.registeredEnchantments.size();
    }

    public static void cleanup() {
        try {
            EnchantmentRegistry instance = getInstance();
            if (instance != null) {
                if (instance.enchantments != null) {
                    instance.enchantments.clear();
                }
                if (instance.registeredEnchantments != null) {
                    instance.registeredEnchantments.clear();
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("清理EnchantmentRegistry时出错: " + e.getMessage());
        }
    }

    /**
     * 获取指定附魔的冲突列表
     */
    public static List<String> getEnchantmentConflicts(String id) {
        if (id == null || id.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Set<String> conflicts = EnchantmentConflictManager.getInstance().getConflicts(id);
            return conflicts != null ? new ArrayList<>(conflicts) : new ArrayList<>();
        } catch (Exception e) {
            Bukkit.getLogger().warning("获取附魔 " + id + " 的冲突列表时出错: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取所有附魔信息（用于 GUI 适配）
     */
    public static Map<String, EnchantmentInfo> getAllEnchantments() {
        Map<String, EnchantmentInfo> infoMap = new HashMap<>();

        try {
            EnchantmentRegistry registry = getInstance();
            if (registry == null || registry.enchantments == null) {
                return infoMap;
            }

            for (Map.Entry<String, EnchantmentData> entry : registry.enchantments.entrySet()) {
                if (entry == null || entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }

                try {
                    EnchantmentData data = entry.getValue();
                    EnchantmentInfo info = new EnchantmentInfo(
                        data.getName(),
                        data.getDescription(),
                        data.getRarity(),
                        data.getMaxLevel(),
                        data.getType()
                    );
                    infoMap.put(entry.getKey(), info);
                } catch (Exception e) {
                    Bukkit.getLogger().warning("处理附魔 " + entry.getKey() + " 的信息时出错: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("获取所有附魔信息时出错: " + e.getMessage());
        }

        return infoMap;
    }

    /**
     * 附魔信息传输类
     */
    public static class EnchantmentInfo {
        private final String name;
        private final String description;
        private final String rarity;
        private final int maxLevel;
        private final String type;

        public EnchantmentInfo(String name, String description, String rarity, int maxLevel, String type) {
            // 参数验证和默认值
            this.name = (name != null && !name.trim().isEmpty()) ? name : "未知附魔";
            this.description = (description != null && !description.trim().isEmpty()) ? description : "无描述";
            this.rarity = (rarity != null && !rarity.trim().isEmpty()) ? rarity : "COMMON";
            this.maxLevel = (maxLevel > 0) ? maxLevel : 1;
            this.type = (type != null && !type.trim().isEmpty()) ? type : "CUSTOM";
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getRarity() { return rarity; }
        public int getMaxLevel() { return maxLevel; }
        public String getType() { return type; }
    }
}
