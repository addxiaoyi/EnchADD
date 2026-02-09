package com.enadd.core.dataexport;

import com.enadd.core.registry.EnchantmentRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 数据导出工具类
 * 从插件注册表中提取附魔数据并生成网站可用的JSON格式
 */
public class EnchantmentDataExporter {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * 导出所有附魔数据到JSON文件
     */
    public void exportAllEnchantmentData() {
        try {
            // 从注册表获取所有附魔数据
            Map<String, EnchantmentRegistry.EnchantmentInfo> allEnchantments = EnchantmentRegistry.getAllEnchantments();

            // 按类别组织数据
            Map<String, JsonObject> categorizedEnchantments = categorizeEnchantments(allEnchantments);

            // 生成主要的enchantments.json文件
            generateMainEnchantmentsJson(categorizedEnchantments);

            // 生成冲突规则JSON
            generateConflictRulesJson();

            System.out.println("附魔数据导出完成！");
            System.out.println("生成了 enchantments.json 和 conflict_rules.json 文件");

        } catch (IOException | RuntimeException e) {
            System.err.println("导出附魔数据时出错: " + e.getMessage());
            // Use custom logger if available, otherwise just log message
        }
    }

    /**
     * 按类别组织附魔数据
     */
    private Map<String, JsonObject> categorizeEnchantments(Map<String, EnchantmentRegistry.EnchantmentInfo> enchantments) {
        Map<String, JsonObject> categories = new HashMap<>();

        for (Map.Entry<String, EnchantmentRegistry.EnchantmentInfo> entry : enchantments.entrySet()) {
            String id = entry.getKey();
            EnchantmentRegistry.EnchantmentInfo info = entry.getValue();
            String type = info.getType();

            JsonObject category = categories.computeIfAbsent(type, k -> new JsonObject());

            JsonObject enchantmentData = new JsonObject();
            enchantmentData.addProperty("name", info.getName());
            enchantmentData.addProperty("description", info.getDescription());
            enchantmentData.addProperty("rarity", info.getRarity());
            enchantmentData.addProperty("maxLevel", info.getMaxLevel());

            category.add(id, enchantmentData);
        }

        return categories;
    }

    /**
     * 生成主要的enchantments.json文件
     */
    private void generateMainEnchantmentsJson(Map<String, JsonObject> categorizedEnchantments) throws IOException {
        JsonObject root = new JsonObject();

        for (Map.Entry<String, JsonObject> entry : categorizedEnchantments.entrySet()) {
            root.add(entry.getKey(), entry.getValue());
        }

        try (FileWriter writer = new FileWriter(Paths.get("docs", "enchantments.json").toFile())) {
            gson.toJson(root, writer);
        }
    }

    /**
     * 生成冲突规则JSON
     */
    private void generateConflictRulesJson() throws IOException {
        JsonObject root = new JsonObject();

        // 获取所有附魔
        Map<String, EnchantmentRegistry.EnchantmentInfo> allEnchantments = EnchantmentRegistry.getAllEnchantments();

        for (String enchantId : allEnchantments.keySet()) {
            List<String> conflicts = EnchantmentRegistry.getEnchantmentConflicts(enchantId);
            if (!conflicts.isEmpty()) {
                JsonArray conflictArray = new JsonArray();
                for (String conflict : conflicts) {
                    conflictArray.add(conflict);
                }
                root.add(enchantId, conflictArray);
            }
        }

        try (FileWriter writer = new FileWriter(Paths.get("docs", "conflict_rules.json").toFile())) {
            gson.toJson(root, writer);
        }
    }
}
