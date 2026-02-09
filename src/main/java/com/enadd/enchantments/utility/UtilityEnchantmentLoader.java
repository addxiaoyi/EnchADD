package com.enadd.enchantments.utility;

import org.bukkit.enchantments.Enchantment;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class UtilityEnchantmentLoader {
    // Bug #304: 使用ConcurrentHashMap保证线程安全
    private static final Map<String, Enchantment> ENCHANTMENTS = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(UtilityEnchantmentLoader.class.getName());
    private static int successCount = 0;
    private static int failureCount = 0;

    public static void registerAll() {
        LOGGER.info("开始注册实用附魔...");
        long startTime = System.currentTimeMillis();

        // Bug #305-330: 为每个附魔创建添加异常处理（26个附魔）
        safeRegister("auto_repair", () -> new AutoRepairEnchantment());
        safeRegister("bad_omen", () -> new BadOmenEnchantment());
        safeRegister("conduit_power", () -> new ConduitPowerEnchantment());
        safeRegister("dolphins_grace", () -> new DolphinsGraceEnchantment());
        safeRegister("double_drop", () -> new DoubleDropEnchantment());
        safeRegister("glowing", () -> new GlowingEnchantment());
        safeRegister("harvest_utility", () -> new HarvestEnchantment());
        safeRegister("hero_of_the_village", () -> new HeroOfTheVillageEnchantment());
        safeRegister("invisibility", () -> new InvisibilityEnchantment());
        safeRegister("levitation", () -> new LevitationEnchantment());
        safeRegister("light_footed", () -> new LightFootedEnchantment());
        safeRegister("light_source", () -> new LightSourceEnchantment());
        safeRegister("luck", () -> new LuckEnchantment());
        safeRegister("night_vision", () -> new NightVisionEnchantment());
        safeRegister("quick_hands", () -> new QuickHandsEnchantment());
        safeRegister("quick_swap", () -> new QuickSwapEnchantment());
        safeRegister("raid_omen", () -> new RaidOmenEnchantment());
        safeRegister("scavenger", () -> new ScavengerEnchantment());
        safeRegister("slow_falling", () -> new SlowFallingEnchantment());
        safeRegister("soft_landing", () -> new SoftLandingEnchantment());
        safeRegister("soul_bound", () -> new SoulBoundEnchantment());
        safeRegister("steady_aim", () -> new SteadyAimEnchantment());
        safeRegister("swift_draw", () -> new SwiftDrawEnchantment());
        safeRegister("tracker", () -> new TrackerEnchantment());
        safeRegister("unluck", () -> new UnluckEnchantment());
        safeRegister("water_walk", () -> new WaterWalkEnchantment());

        long endTime = System.currentTimeMillis();
        LOGGER.info(String.format("实用附魔注册完成: 成功 %d, 失败 %d, 耗时 %dms",
            successCount, failureCount, (endTime - startTime)));
    }

    // Bug #331: 添加安全注册方法，包装异常处理
    private static void safeRegister(String id, EnchantmentSupplier supplier) {
        try {
            // Bug #332: 验证id参数
            if (id == null || id.trim().isEmpty()) {
                LOGGER.warning("附魔ID为null或空，跳过注册");
                failureCount++;
                return;
            }

            // Bug #333: 验证supplier参数
            if (supplier == null) {
                LOGGER.warning("附魔 " + id + " 的supplier为null，跳过注册");
                failureCount++;
                return;
            }

            Enchantment enchantment = supplier.get();

            // Bug #334: 验证创建的附魔对象
            if (enchantment == null) {
                LOGGER.warning("附魔 " + id + " 创建失败（返回null），跳过注册");
                failureCount++;
                return;
            }

            register(id, enchantment);
            successCount++;
        } catch (Exception e) {
            LOGGER.severe("注册附魔 " + id + " 时出错: " + e.getMessage());
            e.printStackTrace();
            failureCount++;
        }
    }

    @FunctionalInterface
    private interface EnchantmentSupplier {
        Enchantment get() throws Exception;
    }

    // Bug #335: 添加参数验证
    private static void register(String id, Enchantment enchantment) {
        if (id == null || id.trim().isEmpty()) {
            LOGGER.warning("register: id为null或空");
            return;
        }

        if (enchantment == null) {
            LOGGER.warning("register: enchantment为null，id=" + id);
            return;
        }

        try {
            ENCHANTMENTS.put(id, enchantment);
        } catch (Exception e) {
            LOGGER.severe("存储附魔时出错，id=" + id + ": " + e.getMessage());
        }
    }

    // Bug #336: 添加异常处理和null检查
    public static Map<String, Enchantment> getAllEnchantments() {
        try {
            if (ENCHANTMENTS == null || ENCHANTMENTS.isEmpty()) {
                LOGGER.warning("ENCHANTMENTS为null或空，返回空Map");
                return new HashMap<>();
            }
            return new HashMap<>(ENCHANTMENTS);
        } catch (Exception e) {
            LOGGER.severe("获取所有附魔时出错: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // Bug #337: 添加获取统计信息的方法
    public static int getSuccessCount() {
        return successCount;
    }

    public static int getFailureCount() {
        return failureCount;
    }

    public static int getTotalCount() {
        return successCount + failureCount;
    }
}
