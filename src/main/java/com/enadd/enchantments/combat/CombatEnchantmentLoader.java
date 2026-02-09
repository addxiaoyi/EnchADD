package com.enadd.enchantments.combat;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.Bukkit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CombatEnchantmentLoader {
    private static final Map<String, Enchantment> ENCHANTMENTS = new ConcurrentHashMap<>();
    private static int successCount = 0;
    private static int failureCount = 0;

    public static void registerAll() {
        successCount = 0;
        failureCount = 0;

        // 新增战斗附魔
        safeRegister("aegis", AegisEnchantment::new);
        safeRegister("armor_break", ArmorBreakEnchantment::new);
        safeRegister("arrow_rain", ArrowRainEnchantment::new);
        safeRegister("berserker_rage", BerserkerRageEnchantment::new);
        safeRegister("blade_dance", BladeDanceEnchantment::new);
        safeRegister("bloodlust", BloodlustEnchantment::new);
        safeRegister("blood_pact", BloodPactEnchantment::new);
        safeRegister("charge_shot", ChargeShotEnchantment::new);
        safeRegister("death_mark", DeathMarkEnchantment::new);
        safeRegister("dragon_breath", DragonBreathEnchantment::new);
        safeRegister("earth_shatter", EarthShatterEnchantment::new);
        safeRegister("explosive_arrow", ExplosiveArrowEnchantment::new);
        safeRegister("frenzy", FrenzyEnchantment::new);
        safeRegister("grim_harvest", GrimHarvestEnchantment::new);
        safeRegister("ice_prison", IcePrisonEnchantment::new);
        safeRegister("ice_shot", IceShotEnchantment::new);
        safeRegister("lightning_arrow", LightningArrowEnchantment::new);
        safeRegister("mana_burn", ManaBurnEnchantment::new);
        safeRegister("mana_steal", ManaStealEnchantment::new);
        safeRegister("phantom_strike", PhantomStrikeEnchantment::new);
        safeRegister("poison_cloud", PoisonCloudEnchantment::new);
        safeRegister("rapid_fire", RapidFireEnchantment::new);
        safeRegister("relentless", RelentlessEnchantment::new);
        safeRegister("scatter_shot", ScatterShotEnchantment::new);
        safeRegister("shadow_strike", ShadowStrikeEnchantment::new);
        safeRegister("soul_burn", SoulBurnEnchantment::new);
        safeRegister("tactical_strike", TacticalStrikeEnchantment::new);
        safeRegister("thunder_strike", ThunderStrikeEnchantment::new);
        safeRegister("toxic_shot", ToxicShotEnchantment::new);
        safeRegister("vampire_arrow", VampireArrowEnchantment::new);
        safeRegister("void_slash", VoidSlashEnchantment::new);
        safeRegister("wind_slash", WindSlashEnchantment::new);

        // 现有战斗附魔
        safeRegister("armor_pierce", ArmorPierceEnchantment::new);
        safeRegister("bleeding", BleedingEnchantment::new);
        safeRegister("crippling", CripplingEnchantment::new);
        safeRegister("critical_strike", CriticalStrikeEnchantment::new);
        safeRegister("disarm", DisarmEnchantment::new);
        safeRegister("duelist", DuelistEnchantment::new);
        safeRegister("execution", ExecutionEnchantment::new);
        safeRegister("flame_blade", FlameBladeEnchantment::new);
        safeRegister("frost_blade", FrostBladeEnchantment::new);
        safeRegister("hemorrhage", HemorrhageEnchantment::new);
        safeRegister("lifesteal", LifeStealEnchantment::new);
        safeRegister("momentum", MomentumEnchantment::new);
        safeRegister("piercing_shot", PiercingShotEnchantment::new);
        safeRegister("rend", RendEnchantment::new);
        safeRegister("reprisal", ReprisalEnchantment::new);
        safeRegister("savage", SavageEnchantment::new);
        safeRegister("thunder_blade", ThunderBladeEnchantment::new);
        safeRegister("venom_blade", VenomBladeEnchantment::new);
        safeRegister("whirlwind", WhirlwindEnchantment::new);

        Bukkit.getLogger().info("战斗附魔加载完成: 成功 " + successCount + " 个, 失败 " + failureCount + " 个");
    }

    @FunctionalInterface
    private interface EnchantmentSupplier {
        Enchantment get() throws Exception;
    }

    private static void safeRegister(String id, EnchantmentSupplier supplier) {
        if (id == null || id.trim().isEmpty()) {
            Bukkit.getLogger().warning("尝试注册战斗附魔时ID为空");
            failureCount++;
            return;
        }

        try {
            Enchantment enchantment = supplier.get();
            if (enchantment == null) {
                Bukkit.getLogger().warning("战斗附魔 " + id + " 创建失败: 返回null");
                failureCount++;
                return;
            }

            register(id, enchantment);
            successCount++;
        } catch (Exception e) {
            Bukkit.getLogger().warning("注册战斗附魔 " + id + " 时出错: " + e.getMessage());
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
            Bukkit.getLogger().warning("战斗附魔 " + id + " 已存在，将被覆盖");
        }

        ENCHANTMENTS.put(id, enchantment);
    }

    public static Map<String, Enchantment> getAllEnchantments() {
        try {
            if (ENCHANTMENTS.isEmpty()) {
                Bukkit.getLogger().warning("战斗附魔列表为空");
                return new HashMap<>();
            }
            return new HashMap<>(ENCHANTMENTS);
        } catch (Exception e) {
            Bukkit.getLogger().severe("获取战斗附魔列表时出错: " + e.getMessage());
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
