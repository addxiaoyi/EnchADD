package com.enadd.enchantments.armor;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.Bukkit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ArmorEnchantmentLoader {
    private static final Map<String, Enchantment> ENCHANTMENTS = new ConcurrentHashMap<>();
    private static int successCount = 0;
    private static int failureCount = 0;

    public static void registerAll() {
        successCount = 0;
        failureCount = 0;

        // 新增护甲附魔
        safeRegister("adrenaline", AdrenalineEnchantment::new);
        safeRegister("aegis_armor", AegisArmorEnchantment::new);
        safeRegister("celestial_blessing", CelestialBlessingEnchantment::new);
        safeRegister("divine_protection", DivineProtectionEnchantment::new);
        safeRegister("endurance", EnduranceEnchantment::new);
        safeRegister("evasive", EvasiveEnchantment::new);
        safeRegister("fortress", FortressEnchantment::new);
        safeRegister("grounding", GroundingEnchantment::new);
        safeRegister("iron_will", IronWillEnchantment::new);
        safeRegister("magma_walker", MagmaWalkerEnchantment::new);
        safeRegister("recoil", RecoilEnchantment::new);
        safeRegister("second_wind", SecondWindEnchantment::new);
        safeRegister("soul_fire", SoulFireEnchantment::new);
        safeRegister("thermostatic", ThermostaticEnchantment::new);
        safeRegister("warden_bane", WardenBaneEnchantment::new);
        safeRegister("willpower", WillpowerEnchantment::new);

        // 现有护甲附魔
        safeRegister("aqua_affinity", AquaAffinityEnchantment::new);
        safeRegister("barrier", BarrierEnchantment::new);
        safeRegister("blast_protection", BlastProtectionEnchantment::new);
        safeRegister("depth_strider", DepthStriderEnchantment::new);
        safeRegister("feather_falling", FeatherFallingEnchantment::new);
        safeRegister("fire_protection", FireProtectionEnchantment::new);
        safeRegister("frost_walker", FrostWalkerEnchantment::new);
        safeRegister("protection", ProtectionEnchantment::new);
        safeRegister("projectile_protection", ProjectileProtectionEnchantment::new);
        safeRegister("respiration", RespirationEnchantment::new);
        safeRegister("soul_speed", SoulSpeedEnchantment::new);
        safeRegister("stone_skin", StoneSkinEnchantment::new);
        safeRegister("swift_sneak", SwiftSneakEnchantment::new);
        safeRegister("thorns", ThornsEnchantment::new);

        Bukkit.getLogger().info("护甲附魔加载完成: 成功 " + successCount + " 个, 失败 " + failureCount + " 个");
    }

    @FunctionalInterface
    private interface EnchantmentSupplier {
        Enchantment get() throws Exception;
    }

    private static void safeRegister(String id, EnchantmentSupplier supplier) {
        if (id == null || id.trim().isEmpty()) {
            Bukkit.getLogger().warning("尝试注册护甲附魔时ID为空");
            failureCount++;
            return;
        }

        try {
            Enchantment enchantment = supplier.get();
            if (enchantment == null) {
                Bukkit.getLogger().warning("护甲附魔 " + id + " 创建失败: 返回null");
                failureCount++;
                return;
            }

            register(id, enchantment);
            successCount++;
        } catch (Exception e) {
            Bukkit.getLogger().warning("注册护甲附魔 " + id + " 时出错: " + e.getMessage());
            failureCount++;
        }
    }

    private static void register(String id, Enchantment enchantment) {
        if (id == null || id.trim().isEmpty()) {
            Bukkit.getLogger().warning("register: ID为空");
            return;
        }

        if (enchantment == null) {
            Bukkit.getLogger().warning("register: 附魔对象为null (ID: " + id + ")");
            return;
        }

        if (ENCHANTMENTS.containsKey(id)) {
            Bukkit.getLogger().warning("护甲附魔 " + id + " 已存在，将被覆盖");
        }

        ENCHANTMENTS.put(id, enchantment);
    }

    public static Map<String, Enchantment> getAllEnchantments() {
        try {
            if (ENCHANTMENTS.isEmpty()) {
                Bukkit.getLogger().warning("护甲附魔列表为空");
                return new HashMap<>();
            }
            return new HashMap<>(ENCHANTMENTS);
        } catch (Exception e) {
            Bukkit.getLogger().severe("获取护甲附魔列表时出错: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static int getSuccessCount() {
        return successCount;
    }

    public static int getFailureCount() {
        return failureCount;
    }
}
