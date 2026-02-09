package com.enadd.enchantments.tool;

import org.bukkit.enchantments.Enchantment;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class ToolEnchantmentLoader {
    // Bug #191: 使用ConcurrentHashMap保证线程安全
    private static final Map<String, Enchantment> ENCHANTMENTS = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(ToolEnchantmentLoader.class.getName());
    private static int successCount = 0;
    private static int failureCount = 0;

    public static void registerAll() {
        LOGGER.info("开始注册工具附魔...");
        long startTime = System.currentTimeMillis();

        // Bug #192-242: 为每个附魔创建添加异常处理（51个附魔）
        safeRegister("angling_expert", () -> new AnglingExpertEnchantment());
        safeRegister("arbor_master", () -> new ArborMasterEnchantment());
        safeRegister("auto_sort", () -> new AutoSortEnchantment());
        safeRegister("builder", () -> new BuilderEnchantment());
        safeRegister("catapult", () -> new CatapultEnchantment());
        safeRegister("climber", () -> new ClimberEnchantment());
        safeRegister("collector", () -> new CollectorEnchantment());
        safeRegister("combo_breaker", () -> new ComboBreakerEnchantment());
        safeRegister("duplication", () -> new DuplicationEnchantment());
        safeRegister("enhanced_piercing", () -> new EnhancedPiercingEnchantment());
        safeRegister("ethereal_step", () -> new EtherealStepEnchantment());
        safeRegister("fortunes_grace", () -> new FortunesGraceEnchantment());
        safeRegister("frost_arrow", () -> new FrostArrowEnchantment());
        safeRegister("harvest", () -> new HarvestEnchantment());
        safeRegister("heavy_hand", () -> new HeavyHandEnchantment());
        safeRegister("intimidation", () -> new IntimidationEnchantment());
        safeRegister("lightning_speed", () -> new LightningSpeedEnchantment());
        safeRegister("miner", () -> new MinerEnchantment());
        safeRegister("multitool", () -> new MultitoolEnchantment());
        safeRegister("ore_sight", () -> new OreSightEnchantment());
        safeRegister("precision", () -> new PrecisionEnchantment());
        safeRegister("prospecting", () -> new ProspectingEnchantment());
        safeRegister("shadow_veil", () -> new ShadowVeilEnchantment());
        safeRegister("signal_arrow", () -> new SignalArrowEnchantment());
        safeRegister("silence", () -> new SilenceEnchantment());
        safeRegister("smelting_touch", () -> new SmeltingTouchEnchantment());
        safeRegister("sniper", () -> new SniperEnchantment());
        safeRegister("speed_surge", () -> new SpeedSurgeEnchantment());
        safeRegister("strong_draw", () -> new StrongDrawEnchantment());
        safeRegister("titan_strength", () -> new TitanStrengthEnchantment());
        safeRegister("transmutation", () -> new TransmutationEnchantment());
        safeRegister("traveler", () -> new TravelerEnchantment());
        safeRegister("vacuum", () -> new VacuumEnchantment());
        safeRegister("void_reach", () -> new VoidReachEnchantment());

        // Existing ones
        safeRegister("area_mining", () -> new AreaMiningEnchantment());
        safeRegister("auto_smelt", () -> new AutoSmeltEnchantment());
        safeRegister("bane_of_arthropods", () -> new BaneOfArthropodsEnchantment());
        safeRegister("efficiency", () -> new EfficiencyEnchantment());
        safeRegister("explosive_shot", () -> new ExplosiveShotEnchantment());
        safeRegister("fire_aspect", () -> new FireAspectEnchantment());
        safeRegister("flame", () -> new FlameEnchantment());
        safeRegister("fortune", () -> new FortuneEnchantment());
        safeRegister("grappling", () -> new GrapplingEnchantment());
        safeRegister("homing", () -> new HomingEnchantment());
        safeRegister("impaling", () -> new ImpalingEnchantment());
        safeRegister("infinity", () -> new InfinityEnchantment());
        safeRegister("instant_mining", () -> new InstantMiningEnchantment());
        safeRegister("knockback", () -> new KnockbackEnchantment());
        safeRegister("looting", () -> new LootingEnchantment());
        safeRegister("luck_of_the_sea", () -> new LuckOfTheSeaEnchantment());
        safeRegister("lure", () -> new LureEnchantment());

        long endTime = System.currentTimeMillis();
        LOGGER.info(String.format("工具附魔注册完成: 成功 %d, 失败 %d, 耗时 %dms",
            successCount, failureCount, (endTime - startTime)));
    }

    // Bug #243: 添加安全注册方法，包装异常处理
    private static void safeRegister(String id, EnchantmentSupplier supplier) {
        try {
            // Bug #244: 验证id参数
            if (id == null || id.trim().isEmpty()) {
                LOGGER.warning("附魔ID为null或空，跳过注册");
                failureCount++;
                return;
            }

            // Bug #245: 验证supplier参数
            if (supplier == null) {
                LOGGER.warning("附魔 " + id + " 的supplier为null，跳过注册");
                failureCount++;
                return;
            }

            Enchantment enchantment = supplier.get();

            // Bug #246: 验证创建的附魔对象
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

    // Bug #247: 添加参数验证
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

    // Bug #248: 添加异常处理和null检查
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

    // Bug #249: 添加获取统计信息的方法
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
