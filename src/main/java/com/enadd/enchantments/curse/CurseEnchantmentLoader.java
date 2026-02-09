package com.enadd.enchantments.curse;

import org.bukkit.enchantments.Enchantment;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class CurseEnchantmentLoader {
    // Bug #250: 使用ConcurrentHashMap保证线程安全
    private static final Map<String, Enchantment> ENCHANTMENTS = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(CurseEnchantmentLoader.class.getName());
    private static int successCount = 0;
    private static int failureCount = 0;

    public static void registerAll() {
        LOGGER.info("开始注册诅咒附魔...");
        long startTime = System.currentTimeMillis();

        // Bug #251-268: 为每个附魔创建添加异常处理（18个附魔）
        safeRegister("binding_curse", () -> new BindingCurseEnchantment());
        safeRegister("decay_curse", () -> new DecayCurseEnchantment());
        safeRegister("fragility_curse", () -> new FragilityCurseEnchantment());
        safeRegister("losing_curse", () -> new LosingCurseEnchantment());
        safeRegister("shadow_curse", () -> new ShadowCurseEnchantment());
        safeRegister("vanishing_curse", () -> new VanishingCurseEnchantment());

        // Existing ones
        safeRegister("curse_binding_plus", () -> new CurseBindingPlusEnchantment());
        safeRegister("curse_blindness", () -> new CurseBlindnessEnchantment());
        safeRegister("curse_confusion", () -> new CurseConfusionEnchantment());
        safeRegister("curse_decay", () -> new CurseDecayEnchantment());
        safeRegister("curse_drain", () -> new CurseDrainEnchantment());
        safeRegister("curse_echo", () -> new CurseEchoEnchantment());
        safeRegister("curse_fragile", () -> new CurseFragileEnchantment());
        safeRegister("curse_hunger", () -> new CurseHungerEnchantment());
        safeRegister("curse_noise", () -> new CurseNoiseEnchantment());
        safeRegister("curse_sluggish", () -> new CurseSluggishEnchantment());
        safeRegister("curse_vengeance", () -> new CurseVengeanceEnchantment());
        safeRegister("curse_weakness", () -> new CurseWeaknessEnchantment());

        long endTime = System.currentTimeMillis();
        LOGGER.info(String.format("诅咒附魔注册完成: 成功 %d, 失败 %d, 耗时 %dms",
            successCount, failureCount, (endTime - startTime)));
    }

    // Bug #269: 添加安全注册方法，包装异常处理
    private static void safeRegister(String id, EnchantmentSupplier supplier) {
        try {
            // Bug #270: 验证id参数
            if (id == null || id.trim().isEmpty()) {
                LOGGER.warning("附魔ID为null或空，跳过注册");
                failureCount++;
                return;
            }

            // Bug #271: 验证supplier参数
            if (supplier == null) {
                LOGGER.warning("附魔 " + id + " 的supplier为null，跳过注册");
                failureCount++;
                return;
            }

            Enchantment enchantment = supplier.get();

            // Bug #272: 验证创建的附魔对象
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

    // Bug #273: 添加参数验证
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

    // Bug #274: 添加异常处理和null检查
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

    // Bug #275: 添加获取统计信息的方法
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
