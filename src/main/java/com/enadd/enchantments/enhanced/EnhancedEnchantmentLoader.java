package com.enadd.enchantments.enhanced;
import com.enadd.enchantments.enhanced.armor.*;
import com.enadd.enchantments.enhanced.combat.*;
import com.enadd.enchantments.enhanced.tool.*;
import org.bukkit.enchantments.Enchantment;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class EnhancedEnchantmentLoader {

    // Bug #351: 使用ConcurrentHashMap保证线程安全
    private static final Map<String, Enchantment> ENHANCED_ENCHANTMENTS = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(EnhancedEnchantmentLoader.class.getName());
    private static int successCount = 0;
    private static int failureCount = 0;

    public static void registerAll() {
        LOGGER.info("开始注册增强附魔...");
        long startTime = System.currentTimeMillis();

        // Bug #352-372: 为每个附魔创建添加异常处理（21个附魔）
        // Combat
        safeRegister("shadow_strike_enh", () -> new ShadowStrikeEnhEnchantment());
        safeRegister("frost_blade_enh", () -> new FrostBladeEnhEnchantment());
        safeRegister("thunder_blade_enh", () -> new ThunderBladeEnhEnchantment());
        safeRegister("void_slash_enh", () -> new VoidSlashEnhEnchantment());
        safeRegister("blood_lust_enh", () -> new BloodLustEnhEnchantment());
        safeRegister("phantom_strike_enh", () -> new PhantomStrikeEnhEnchantment());
        safeRegister("venom_blade_enh", () -> new VenomBladeEnhEnchantment());
        safeRegister("berserker_rage_enh", () -> new BerserkerRageEnhEnchantment());
        safeRegister("critical_strike_enh", () -> new CriticalStrikeEnhEnchantment());
        safeRegister("backstab_enh", () -> new BackstabEnhEnchantment());
        safeRegister("lifesteal_enh", () -> new LifeStealEnhEnchantment());
        safeRegister("explosive_arrow_enh", () -> new ExplosiveArrowEnhEnchantment());

        // Armor
        safeRegister("aegis_enh", () -> new AegisEnhEnchantment());
        safeRegister("dodge_enh", () -> new DodgeEnhEnchantment());
        safeRegister("frost_walker_enh", () -> new FrostWalkerEnhEnchantment());
        safeRegister("second_wind_enh", () -> new SecondWindEnhEnchantment());
        safeRegister("warding_enh", () -> new WardingEnhEnchantment());

        // Tool
        safeRegister("auto_smelt_enh", () -> new AutoSmeltEnhEnchantment());
        safeRegister("builder_enh", () -> new BuilderEnhEnchantment());
        safeRegister("lumberjack_enh", () -> new LumberjackEnhEnchantment());
        safeRegister("treasure_hunter_enh", () -> new TreasureHunterEnhEnchantment());

        long endTime = System.currentTimeMillis();
        LOGGER.info(String.format("增强附魔注册完成: 成功 %d, 失败 %d, 耗时 %dms",
            successCount, failureCount, (endTime - startTime)));
    }

    // Bug #373: 添加安全注册方法，包装异常处理
    private static void safeRegister(String id, EnchantmentSupplier supplier) {
        try {
            // Bug #374: 验证id参数
            if (id == null || id.trim().isEmpty()) {
                LOGGER.warning("附魔ID为null或空，跳过注册");
                failureCount++;
                return;
            }

            // Bug #375: 验证supplier参数
            if (supplier == null) {
                LOGGER.warning("附魔 " + id + " 的supplier为null，跳过注册");
                failureCount++;
                return;
            }

            Enchantment enchantment = supplier.get();

            // Bug #376: 验证创建的附魔对象
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

    // Bug #377: 添加参数验证
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
            ENHANCED_ENCHANTMENTS.put(id, enchantment);
        } catch (Exception e) {
            LOGGER.severe("存储附魔时出错，id=" + id + ": " + e.getMessage());
        }
    }

    // Bug #378: 添加参数验证和null检查
    public static Enchantment getEnchantment(String id) {
        if (id == null || id.trim().isEmpty()) {
            LOGGER.warning("getEnchantment: id为null或空");
            return null;
        }

        try {
            return ENHANCED_ENCHANTMENTS.get(id);
        } catch (Exception e) {
            LOGGER.severe("获取附魔时出错，id=" + id + ": " + e.getMessage());
            return null;
        }
    }

    // Bug #379: 添加异常处理和null检查
    public static Map<String, Enchantment> getAllEnchantments() {
        try {
            if (ENHANCED_ENCHANTMENTS == null || ENHANCED_ENCHANTMENTS.isEmpty()) {
                LOGGER.warning("ENHANCED_ENCHANTMENTS为null或空，返回空Map");
                return new HashMap<>();
            }
            return new HashMap<>(ENHANCED_ENCHANTMENTS);
        } catch (Exception e) {
            LOGGER.severe("获取所有附魔时出错: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // Bug #380: 添加异常处理
    public static int getCount() {
        try {
            return ENHANCED_ENCHANTMENTS != null ? ENHANCED_ENCHANTMENTS.size() : 0;
        } catch (Exception e) {
            LOGGER.severe("获取附魔数量时出错: " + e.getMessage());
            return 0;
        }
    }

    // Bug #381: 添加获取统计信息的方法
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
