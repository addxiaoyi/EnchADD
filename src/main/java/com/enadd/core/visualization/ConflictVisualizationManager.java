package com.enadd.core.visualization;

import com.enadd.core.conflict.EnchantmentConflictManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;


/**
 * 附魔冲突可视化管理器
 * 用于生成网站所需的数据结构
 */
public class ConflictVisualizationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConflictVisualizationManager.class);

    private static ConflictVisualizationManager instance;
    private final Gson gson;

    private ConflictVisualizationManager() {
        EnchantmentConflictManager.getInstance();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public static ConflictVisualizationManager getInstance() {
        if (instance == null) {
            instance = new ConflictVisualizationManager();
        }
        return instance;
    }

    public ConflictVisualizationManager(EnchantmentConflictManager conflictManager, Gson gson) {
        this.gson = gson;
    }

    /**
     * 生成网站所需的附魔数据JSON文件
     */
    public void generateVisualizationData() {
        try {
            // 生成增强版附魔数据
            generateEnhancedEnchantmentsJson();

            // 生成冲突规则数据
            generateConflictRulesJson();

            // 生成综合冲突列表数据
            generateComprehensiveConflictListJson();

            LOGGER.info("可视化数据生成完成！");
        } catch (Exception e) {
            LOGGER.error("生成可视化数据时出错: {}", e.getMessage(), e);
        }
    }

    /**
     * 生成增强版附魔数据JSON
     */
    private void generateEnhancedEnchantmentsJson() throws IOException {
        Map<String, EnchantmentCategoryData> enhancedEnchantments = new HashMap<>();

        // 这里可以根据实际需求添加增强版附魔数据
        // 为了演示，我们创建一些示例数据
        enhancedEnchantments.put("combat_enhanced", new EnchantmentCategoryData(
            "战斗附魔·增强版",
            "#e74c3c",
            "⚔️",
            Arrays.asList(
                new EnchantmentData("shadow_strike_enh", "影袭", "背后攻击造成额外伤害", "epic", "I-IV", "钻石剑,下界合金剑,黑曜石,末影珍珠", "仅林地府邸宝箱（20%概率）", "从敌人背后攻击时必定触发", "高风险高回报的刺客型附魔，需要玩家掌握走位技巧"),
                new EnchantmentData("thunder_strike_enh", "雷击", "攻击召唤闪电", "epic", "I-III", "下界合金锭,绿宝石,金锭,闪电之冠", "雷暴天气击杀女巫获取", "攻击时有8%/12%/18%概率召唤闪电", "概率性触发，范围伤害不稳定但爆发力强"),
                new EnchantmentData("ice_freeze_enh", "冰封", "攻击使敌人减速", "rare", "I-IV", "冰霜之首,钻石剑,下界合金剑", "仅冰屋宝箱（25%概率）", "攻击使敌人减速15%/25%/35%/45%，持续2/3/4/5秒", "控制型附魔，单挑和风筝战术的核心选择")
            )
        ));

        enhancedEnchantments.put("armor_enhanced", new EnchantmentCategoryData(
            "防具附魔·增强版",
            "#3498db",
            "🛡️",
            Arrays.asList(
                new EnchantmentData("frost_walker_enh", "寒冰行者·增强", "在水上行走并结冰", "rare", "I-II", "冰霜之首,蓝冰,钻石", "极地掠夺者稀有掉落", "在水上行走并在脚下形成蓝冰", "水上建筑和逃脱技能"),
                new EnchantmentData("thorns_enh", "荆棘·增强", "伤害攻击者", "epic", "I-III", "下界合金锭,绿宝石,铁锭", "林地府邸宝箱（15%概率）", "攻击者受到攻击伤害的15%/25%/35%", "高风险高回报的反击型附魔")
            )
        ));

        enhancedEnchantments.put("tool_enhanced", new EnchantmentCategoryData(
            "工具附魔·增强版",
            "#f39c12",
            "⛏️",
            Arrays.asList(
                new EnchantmentData("efficiency_enh", "效率·增强", "提高挖掘速度", "rare", "I-V", "下界合金锭,钻石,红石", "要塞图书馆宝箱", "挖掘速度提升25%/45%/65%/85%/100%", "大幅提升挖掘效率的必备附魔"),
                new EnchantmentData("fortune_enh", "时运·增强", "增加某些方块的掉落", "epic", "I-III", "下界合金锭,绿宝石,青金石", "海底神殿宝箱（10%概率）", "矿物掉落增加1/2/3倍", "矿物收集效率的极致体现")
            )
        ));

        // 写入JSON文件
        String outputPath = Paths.get("docs", "enchantments_enhanced.json").toString();
        try (FileWriter writer = new FileWriter(outputPath)) {
            gson.toJson(enhancedEnchantments, writer);
        }
    }

    /**
     * 生成冲突规则JSON数据
     */
    private void generateConflictRulesJson() throws IOException {
        Map<String, ConflictRule> conflictRules = new HashMap<>();

        // 添加冲突规则
        conflictRules.put("combat", new ConflictRule(
            Arrays.asList("armor", "special"),
            Arrays.asList("tool", "curse", "utility", "defense", "cosmetic"),
            Arrays.asList("defense")
        ));

        conflictRules.put("armor", new ConflictRule(
            Arrays.asList("combat", "special"),
            Arrays.asList("tool", "curse", "utility", "defense", "cosmetic"),
            Arrays.asList("defense")
        ));

        conflictRules.put("tool", new ConflictRule(
            Arrays.asList("curse"),
            Arrays.asList("combat", "armor", "utility", "defense", "cosmetic"),
            Arrays.asList("special")
        ));

        conflictRules.put("curse", new ConflictRule(
            Arrays.asList("combat", "armor", "tool", "utility", "defense", "special"),
            Arrays.asList("cosmetic"),
            Arrays.asList()
        ));

        conflictRules.put("utility", new ConflictRule(
            Arrays.asList("curse", "special"),
            Arrays.asList("combat", "armor", "tool", "defense", "cosmetic"),
            Arrays.asList("special")
        ));

        conflictRules.put("defense", new ConflictRule(
            Arrays.asList("special"),
            Arrays.asList("combat", "armor", "tool", "curse", "utility", "cosmetic"),
            Arrays.asList("combat", "armor")
        ));

        conflictRules.put("special", new ConflictRule(
            Arrays.asList("combat", "armor", "curse", "utility"),
            Arrays.asList("cosmetic"),
            Arrays.asList("tool", "defense")
        ));

        conflictRules.put("cosmetic", new ConflictRule(
            Arrays.asList("curse", "special"),
            Arrays.asList("combat", "armor", "tool", "utility", "defense"),
            Arrays.asList()
        ));

        // 添加原版附魔冲突规则
        conflictRules.put("vanilla_weapon", new ConflictRule(
            Arrays.asList("vanilla_armor", "special"),
            Arrays.asList("vanilla_tool", "vanilla_curse", "vanilla_utility", "defense", "cosmetic"),
            Arrays.asList("defense")
        ));

        conflictRules.put("vanilla_armor", new ConflictRule(
            Arrays.asList("vanilla_weapon", "special"),
            Arrays.asList("vanilla_tool", "vanilla_curse", "vanilla_utility", "defense", "cosmetic"),
            Arrays.asList("defense")
        ));

        // 写入冲突规则JSON文件
        String outputPath = Paths.get("docs", "conflict_rules.json").toString();
        try (FileWriter writer = new FileWriter(outputPath)) {
            gson.toJson(conflictRules, writer);
        }
    }

    /**
     * 生成综合冲突列表JSON数据
     */
    private void generateComprehensiveConflictListJson() throws IOException {
        Map<String, List<String>> comprehensiveConflicts = new HashMap<>();

        // 基于现有的冲突规则构建综合冲突列表
        Map<String, ConflictRule> allRules = new HashMap<>();

        // 添加所有冲突规则
        allRules.put("combat", new ConflictRule(
            Arrays.asList("armor", "special"),
            Arrays.asList("tool", "curse", "utility", "defense", "cosmetic"),
            Arrays.asList("defense")
        ));

        allRules.put("armor", new ConflictRule(
            Arrays.asList("combat", "special"),
            Arrays.asList("tool", "curse", "utility", "defense", "cosmetic"),
            Arrays.asList("defense")
        ));

        allRules.put("tool", new ConflictRule(
            Arrays.asList("curse"),
            Arrays.asList("combat", "armor", "utility", "defense", "cosmetic"),
            Arrays.asList("special")
        ));

        allRules.put("curse", new ConflictRule(
            Arrays.asList("combat", "armor", "tool", "utility", "defense", "special"),
            Arrays.asList("cosmetic"),
            Arrays.asList()
        ));

        allRules.put("utility", new ConflictRule(
            Arrays.asList("curse", "special"),
            Arrays.asList("combat", "armor", "tool", "defense", "cosmetic"),
            Arrays.asList("special")
        ));

        allRules.put("defense", new ConflictRule(
            Arrays.asList("special"),
            Arrays.asList("combat", "armor", "tool", "curse", "utility", "cosmetic"),
            Arrays.asList("combat", "armor")
        ));

        allRules.put("special", new ConflictRule(
            Arrays.asList("combat", "armor", "curse", "utility"),
            Arrays.asList("cosmetic"),
            Arrays.asList("tool", "defense")
        ));

        allRules.put("cosmetic", new ConflictRule(
            Arrays.asList("curse", "special"),
            Arrays.asList("combat", "armor", "tool", "utility", "defense"),
            Arrays.asList()
        ));

        allRules.put("vanilla_weapon", new ConflictRule(
            Arrays.asList("vanilla_armor", "special"),
            Arrays.asList("vanilla_tool", "vanilla_curse", "vanilla_utility", "defense", "cosmetic"),
            Arrays.asList("defense")
        ));

        allRules.put("vanilla_armor", new ConflictRule(
            Arrays.asList("vanilla_weapon", "special"),
            Arrays.asList("vanilla_tool", "vanilla_curse", "vanilla_utility", "defense", "cosmetic"),
            Arrays.asList("defense")
        ));

        // 构建综合冲突列表：每个类别与哪些其他类别存在冲突
        for (Map.Entry<String, ConflictRule> entry : allRules.entrySet()) {
            String category = entry.getKey();
            ConflictRule rule = entry.getValue();

            // 合并冲突和弱冲突类别
            List<String> allConflictingCategories = new ArrayList<>(rule.conflicts);
            allConflictingCategories.addAll(rule.weak);

            comprehensiveConflicts.put(category, allConflictingCategories);
        }

        // 写入综合冲突列表JSON文件
        String outputPath = Paths.get("docs", "comprehensive_conflicts.json").toString();
        try (FileWriter writer = new FileWriter(outputPath)) {
            gson.toJson(comprehensiveConflicts, writer);
        }
    }

    /**
     * 附魔分类数据结构
     */
    public static class EnchantmentCategoryData {
        public String name;
        public String color;
        public String icon;
        public List<EnchantmentData> enchantments;

        public EnchantmentCategoryData(String name, String color, String icon, List<EnchantmentData> enchantments) {
            this.name = name;
            this.color = color;
            this.icon = icon;
            this.enchantments = enchantments;
        }
    }

    /**
     * 附魔数据结构
     */
    public static class EnchantmentData {
        public String id;
        public String name;
        public String description;
        public String rarity;
        public String level;
        public String materials;
        public String obtain;
        public String trigger;
        public String balance;

        public EnchantmentData(String id, String name, String description, String rarity, String level,
                              String materials, String obtain, String trigger, String balance) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.rarity = rarity;
            this.level = level;
            this.materials = materials;
            this.obtain = obtain;
            this.trigger = trigger;
            this.balance = balance;
        }
    }

    /**
     * 冲突规则数据结构
     */
    public static class ConflictRule {
        public List<String> conflicts;
        public List<String> compatible;
        public List<String> weak;

        public ConflictRule(List<String> conflicts, List<String> compatible, List<String> weak) {
            this.conflicts = conflicts;
            this.compatible = compatible;
            this.weak = weak;
        }
    }
}
