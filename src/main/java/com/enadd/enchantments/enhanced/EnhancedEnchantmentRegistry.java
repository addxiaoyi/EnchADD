package com.enadd.enchantments.enhanced;

import java.util.HashMap;
import java.util.Map;


public class EnhancedEnchantmentRegistry {
    private static final Map<String, EnhancedEnchantmentData> DATA = new HashMap<>();

    static {
        // Combat
        register("shadow_strike_enh", "暗影打击 (强化)", "从背后攻击造成巨大伤害", "LEGENDARY", 3, 50, EnhancedCategory.COMBAT);
        register("frost_blade_enh", "霜冻之刃 (强化)", "攻击使目标大幅减速并造成冻结伤害", "EPIC", 5, 40, EnhancedCategory.COMBAT);
        register("thunder_blade_enh", "雷霆之刃 (强化)", "攻击引发连锁闪电", "EPIC", 5, 40, EnhancedCategory.COMBAT);
        register("void_slash_enh", "虚空斩 (强化)", "斩击撕裂空间，造成百分比伤害", "LEGENDARY", 3, 60, EnhancedCategory.COMBAT);
        register("blood_lust_enh", "嗜血 (强化)", "击杀敌人后获得大幅属性提升", "EPIC", 3, 35, EnhancedCategory.COMBAT);
        register("phantom_strike_enh", "幻影打击 (强化)", "攻击时有概率触发多次幻影攻击", "LEGENDARY", 3, 55, EnhancedCategory.COMBAT);
        register("venom_blade_enh", "剧毒之刃 (强化)", "攻击施加致命剧毒", "EPIC", 5, 30, EnhancedCategory.COMBAT);
        register("berserker_rage_enh", "狂战士之怒 (强化)", "生命值越低伤害越高", "EPIC", 3, 45, EnhancedCategory.COMBAT);
        register("critical_strike_enh", "暴击 (强化)", "大幅提升暴击率和暴击伤害", "EPIC", 5, 40, EnhancedCategory.COMBAT);
        register("backstab_enh", "背刺 (强化)", "从背后攻击造成额外伤害", "RARE", 5, 25, EnhancedCategory.COMBAT);
        register("lifesteal_enh", "生命偷取 (强化)", "攻击回复生命值", "EPIC", 5, 50, EnhancedCategory.COMBAT);
        register("explosive_arrow_enh", "爆炸箭 (强化)", "箭矢命中引发剧烈爆炸", "EPIC", 5, 45, EnhancedCategory.COMBAT);

        // Armor
        register("aegis_enh", "神盾 (强化)", "受到伤害时有概率完全抵挡", "LEGENDARY", 3, 60, EnhancedCategory.ARMOR);
        register("dodge_enh", "闪避 (强化)", "大幅提升闪避概率", "EPIC", 5, 40, EnhancedCategory.ARMOR);
        register("frost_walker_enh", "深寒行者 (强化)", "行走时冻结周围地面和敌人", "EPIC", 2, 45, EnhancedCategory.ARMOR);
        register("second_wind_enh", "二重呼吸 (强化)", "濒死时获得爆发性恢复和护盾", "LEGENDARY", 1, 80, EnhancedCategory.ARMOR);
        register("warding_enh", "守护 (强化)", "减少来自所有来源的伤害", "EPIC", 5, 35, EnhancedCategory.ARMOR);

        // Tool
        register("auto_smelt_enh", "自动熔炼 (强化)", "挖掘矿石时自动熔炼并获得额外产出", "EPIC", 3, 40, EnhancedCategory.TOOL);
        register("builder_enh", "建筑师 (强化)", "放置方块时有概率不消耗材料并提升速度", "RARE", 5, 30, EnhancedCategory.TOOL);
        register("lumberjack_enh", "伐木工 (强化)", "一键砍倒整棵树并自动补种", "EPIC", 3, 35, EnhancedCategory.TOOL);
        register("treasure_hunter_enh", "寻宝者 (强化)", "挖掘方块时有概率发现珍稀宝藏", "LEGENDARY", 3, 55, EnhancedCategory.TOOL);
    }

    private static void register(String id, String name, String desc, String rarity, int maxLevel, int baseCost, EnhancedCategory category) {
        DATA.put(id, new EnhancedEnchantmentData(id, name, desc, rarity, maxLevel, baseCost, category));
    }

    public static EnhancedEnchantmentData getEnchantmentData(String id) {
        return DATA.getOrDefault(id, new EnhancedEnchantmentData(id, id, "Enhanced version of " + id, "RARE", 5, 20, EnhancedCategory.COMBAT));
    }

    public static Map<String, EnhancedEnchantmentData> getAllEnchantments() {
        return new HashMap<>(DATA);
    }

    public enum EnhancedCategory {
        COMBAT, ARMOR, TOOL, CURSE
    }
}
