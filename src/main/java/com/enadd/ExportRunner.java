package com.enadd;

import com.enadd.core.dataexport.EnchantmentDataExporter;


/**
 * Standalone runner for exporting enchantment data
 */
public class ExportRunner {
    public static void main(String[] args) {
        System.out.println("开始导出附魔数据...");
        try {
            EnchantmentDataExporter exporter = new EnchantmentDataExporter();
            exporter.exportAllEnchantmentData();
            System.out.println("附魔数据导出完成！");
        } catch (Exception e) {
            System.err.println("导出过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
