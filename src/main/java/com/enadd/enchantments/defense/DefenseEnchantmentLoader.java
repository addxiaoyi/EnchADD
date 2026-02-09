package com.enadd.enchantments.defense;

import org.bukkit.enchantments.Enchantment;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class DefenseEnchantmentLoader {
    // Bug #276: 使用ConcurrentHashMap保证线程安全
    private static final Map<String, Enchantment> ENCHANTMENTS = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(DefenseEnchantmentLoader.class.getName());
    private static int successCount = 0;
    private static int failureCount = 0;

    public static void registerAll() {
        LOGGER.info("开始注册防御附魔...");
        long startTime = System.currentTimeMillis();

        // Bug #277-296: 为每个附魔创建添加异常处理（20个附魔）
        safeRegister("absorption", () -> new AbsorptionEnchantment());
        safeRegister("adaptation", () -> new AdaptationEnchantment());
        safeRegister("counter_attack", () -> new CounterAttackEnchantment());
        safeRegister("damage_absorption", () -> new DamageAbsorptionEnchantment());
        safeRegister("elemental_resist", () -> new ElementalResistEnchantment());
        safeRegister("energy_shield", () -> new EnergyShieldEnchantment());
        safeRegister("fire_resistance", () -> new FireResistanceEnchantment());
        safeRegister("haste", () -> new HasteEnchantment());
        safeRegister("health_boost", () -> new HealthBoostEnchantment());
        safeRegister("immunity", () -> new ImmunityEnchantment());
        safeRegister("invisibility_aura", () -> new InvisibilityAuraEnchantment());
        safeRegister("jump_boost", () -> new JumpBoostEnchantment());
        safeRegister("magic_barrier", () -> new MagicBarrierEnchantment());
        safeRegister("physical_barrier", () -> new PhysicalBarrierEnchantment());
        safeRegister("reflect", () -> new ReflectEnchantment());
        safeRegister("regeneration", () -> new RegenerationEnchantment());
        safeRegister("resistance", () -> new ResistanceEnchantment());
        safeRegister("saturation", () -> new SaturationEnchantment());
        safeRegister("strength", () -> new StrengthEnchantment());
        safeRegister("water_breathing", () -> new WaterBreathingEnchantment());

        long endTime = System.currentTimeMillis();
        LOGGER.info(String.format("防御附魔注册完成: 成功 %d, 失败 %d, 耗时 %dms",
            successCount, failureCount, (endTime - startTime)));
    }

    // Bug #297: 添加安全注册方法，包装异常处理
    private static void safeRegister(String id, EnchantmentSupplier supplier) {
        try {
            // Bug #298: 验证id参数
            if (id == null || id.trim().isEmpty()) {
                LOGGER.warning("附魔ID为null或空，跳过注册");
                failureCount++;
                return;
            }

            // Bug #299: 验证supplier参数
            if (supplier == null) {
                LOGGER.warning("附魔 " + id + " 的supplier为null，跳过注册");
                failureCount++;
                return;
            }

            Enchantment enchantment = supplier.get();

            // Bug #300: 验证创建的附魔对象
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

    // Bug #301: 添加参数验证
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

    // Bug #302: 添加异常处理和null检查
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

    // Bug #303: 添加获取统计信息的方法
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
