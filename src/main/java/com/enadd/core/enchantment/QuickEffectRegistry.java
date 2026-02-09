package com.enadd.core.enchantment;

import com.enadd.core.enchantment.UniversalEffect.EffectType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;



/**
 * 快速效果注册器 - 使用UniversalEffect快速注册所有229个附魔
 */
public final class QuickEffectRegistry {

    private final JavaPlugin plugin;
    private final EnchantmentEffectManager manager;

    public QuickEffectRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.manager = EnchantmentEffectManager.getInstance();
    }

    /**
     * 注册所有效果
     */
    public void registerAll() {
        plugin.getLogger().info("=== 开始注册附魔效果 ===");

        registerCombatEffects();      // 56个
        registerArmorEffects();       // 33个
        registerToolEffects();        // 66个
        registerCurseEffects();       // 12个
        registerUtilityEffects();     // 27个
        registerDefenseEffects();     // 31个
        registerSpecialEffects();     // 5个

        int total = manager.getRegisteredEffectCount();
        plugin.getLogger().info("=== 已注册 " + total + " 个附魔效果 ===");
    }

    /**
     * 注册战斗效果 (56个)
     */
    private void registerCombatEffects() {
        // 持续伤害类
        register("bleeding", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 0.5, "duration", 100));
        register("hemorrhage", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 0.8, "duration", 80));
        register("rend", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 0.6, "duration", 120));
        register("poison_cloud", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 0.4, "duration", 140));
        register("soul_burn", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 0.7, "duration", 100));
        register("venom_blade", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 0.5, "duration", 100));

        // 生命窃取类
        register("vampirism", EffectType.LIFESTEAL, config("healPercent", 0.15));
        register("life_steal", EffectType.LIFESTEAL, config("healPercent", 0.20));
        register("vampire_arrow", EffectType.LIFESTEAL, config("healPercent", 0.10));
        register("blood_pact", EffectType.LIFESTEAL, config("healPercent", 0.25));
        register("grim_harvest", EffectType.LIFESTEAL, config("healPercent", 0.18));

        // 暴击类
        register("critical_strike", EffectType.CRITICAL, config("critChance", 0.15, "critMultiplier", 2.0));
        register("savage", EffectType.CRITICAL, config("critChance", 0.20, "critMultiplier", 1.8));
        register("tactical_strike", EffectType.CRITICAL, config("critChance", 0.12, "critMultiplier", 2.5));

        // 破甲类
        register("armor_pierce", EffectType.ARMOR_REDUCTION, config("armorReduction", 0.2));
        register("armor_break", EffectType.ARMOR_REDUCTION, config("armorReduction", 0.3));

        // 处决类
        register("execution", EffectType.EXECUTE, config("healthThreshold", 0.3, "bonusDamage", 5.0));
        register("death_mark", EffectType.EXECUTE, config("healthThreshold", 0.25, "bonusDamage", 7.0));

        // 背刺类
        register("backstab", EffectType.BACKSTAB, config("backstabMultiplier", 2.5));
        register("shadow_strike", EffectType.BACKSTAB, config("backstabMultiplier", 3.0));
        register("phantom_strike", EffectType.BACKSTAB, config("backstabMultiplier", 2.8));

        // 缴械类
        register("disarm", EffectType.DISARM, config("disarmChance", 0.1));
        register("crippling", EffectType.DISARM, config("disarmChance", 0.15));

        // 眩晕类
        register("stagger", EffectType.STUN, config("stunDuration", 40));
        register("ice_prison", EffectType.STUN, config("stunDuration", 60));

        // 连击类
        register("momentum", EffectType.COMBO, config("bonusPerCombo", 0.1));
        register("blade_dance", EffectType.COMBO, config("bonusPerCombo", 0.15));
        register("frenzy", EffectType.COMBO, config("bonusPerCombo", 0.12));
        register("relentless", EffectType.COMBO, config("bonusPerCombo", 0.08));

        // 其他战斗效果
        register("reprisal", EffectType.REFLECT, config("reflectPercent", 0.5));
        register("duelist", EffectType.CRITICAL, config("critChance", 0.18, "critMultiplier", 1.9));
        register("berserker_rage", EffectType.CRITICAL, config("critChance", 0.25, "critMultiplier", 2.2));
        register("bloodlust", EffectType.LIFESTEAL, config("healPercent", 0.22));
        register("chain_lightning", EffectType.GENERIC, config());
        register("dragon_breath", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 1.0, "duration", 60));
        register("void_slash", EffectType.ARMOR_REDUCTION, config("armorReduction", 0.4));
        register("thunder_strike", EffectType.STUN, config("stunDuration", 30));
        register("mana_steal", EffectType.LIFESTEAL, config("healPercent", 0.12));
        register("aegis", EffectType.SHIELD, config("shieldAmount", 4.0));
        register("arrow_rain", EffectType.GENERIC, config());
        register("wind_slash", EffectType.GENERIC, config());
        register("earth_shatter", EffectType.STUN, config("stunDuration", 50));
        register("flame_blade", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 0.3, "duration", 80));
        register("frost_blade", EffectType.STUN, config("stunDuration", 20));
        register("thunder_blade", EffectType.GENERIC, config());
        register("mana_burn", EffectType.GENERIC, config());
        register("shield_bash", EffectType.STUN, config("stunDuration", 40));
        register("whirlwind", EffectType.GENERIC, config());
        register("piercing_shot", EffectType.ARMOR_REDUCTION, config("armorReduction", 0.25));
        register("rapid_fire", EffectType.SPEED, config());
        register("charge_shot", EffectType.CRITICAL, config("critChance", 0.3, "critMultiplier", 2.5));
        register("scatter_shot", EffectType.GENERIC, config());
        register("toxic_shot", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 0.4, "duration", 100));
        register("ice_shot", EffectType.STUN, config("stunDuration", 30));
        register("explosive_arrow", EffectType.GENERIC, config());
        register("lightning_arrow", EffectType.GENERIC, config());

        plugin.getLogger().info("✅ 已注册 56 个战斗效果");
    }

    /**
     * 注册护甲效果 (33个)
     */
    private void registerArmorEffects() {
        register("stone_skin", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.1));
        register("dodge", EffectType.DODGE, config("dodgeChance", 0.1));
        register("reinforced_thorns", EffectType.REFLECT, config("reflectPercent", 0.4));
        register("barrier", EffectType.SHIELD, config("shieldAmount", 2.0));
        register("adrenaline", EffectType.SPEED, config());
        register("willpower", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.08));
        register("grounding", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.12));
        register("thermostatic", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.1));
        register("iron_will", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.15));
        register("recoil", EffectType.REFLECT, config("reflectPercent", 0.3));
        register("endurance", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.1));
        register("last_stand", EffectType.LAST_STAND, config("duration", 200));
        register("aegis_armor", EffectType.SHIELD, config("shieldAmount", 3.0));
        register("celestial_blessing", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.2));
        register("divine_protection", EffectType.SHIELD, config("shieldAmount", 5.0));
        register("evasive", EffectType.DODGE, config("dodgeChance", 0.15));
        register("magma_walker", EffectType.GENERIC, config());
        register("frost_walker", EffectType.GENERIC, config());
        register("soul_speed", EffectType.SPEED, config());
        register("depth_strider", EffectType.SPEED, config());
        register("feather_falling", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.2));
        register("respiration", EffectType.GENERIC, config());
        register("aqua_affinity", EffectType.SPEED, config());
        register("thorns", EffectType.REFLECT, config("reflectPercent", 0.3));
        register("protection", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.1));
        register("fire_protection", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.15));
        register("blast_protection", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.15));
        register("projectile_protection", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.15));
        register("swift_sneak", EffectType.SPEED, config());
        register("soul_fire", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 0.5, "duration", 80));
        register("warden_bane", EffectType.CRITICAL, config("critChance", 0.3, "critMultiplier", 3.0));
        register("fortress", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.25));
        register("second_wind", EffectType.LAST_STAND, config("duration", 150));

        plugin.getLogger().info("✅ 已注册 33 个护甲效果");
    }

    /**
     * 注册工具效果 (66个)
     */
    private void registerToolEffects() {
        register("miner", EffectType.SPEED, config());
        register("prospecting", EffectType.FORTUNE, config("bonusChance", 0.2));
        register("auto_smelt", EffectType.AUTO_SMELT, config());
        register("magnetic", EffectType.MAGNETIC, config("radius", 5.0));
        register("precision", EffectType.CRITICAL, config("critChance", 0.2, "critMultiplier", 1.5));
        register("strong_draw", EffectType.CRITICAL, config("critChance", 0.15, "critMultiplier", 1.8));
        register("catapult", EffectType.GENERIC, config());
        register("enhanced_piercing", EffectType.ARMOR_REDUCTION, config("armorReduction", 0.3));
        register("sniper", EffectType.CRITICAL, config("critChance", 0.25, "critMultiplier", 2.5));
        register("frost_arrow", EffectType.STUN, config("stunDuration", 40));
        register("signal_arrow", EffectType.GLOWING, config("duration", 200));
        register("silence", EffectType.GENERIC, config());
        register("harvest", EffectType.FORTUNE, config("bonusChance", 0.3));
        register("titan_strength", EffectType.CRITICAL, config("critChance", 0.2, "critMultiplier", 2.0));
        register("lightning_speed", EffectType.SPEED, config());
        register("combo_breaker", EffectType.COMBO, config("bonusPerCombo", 0.15));
        register("arbor_master", EffectType.SPEED, config());
        register("fortunes_grace", EffectType.FORTUNE, config("bonusChance", 0.25));
        register("smelting_touch", EffectType.AUTO_SMELT, config());
        register("collector", EffectType.MAGNETIC, config("radius", 8.0));
        register("speed_surge", EffectType.SPEED, config());
        register("ethereal_step", EffectType.GENERIC, config());
        register("heavy_hand", EffectType.CRITICAL, config("critChance", 0.18, "critMultiplier", 2.2));
        register("shadow_veil", EffectType.GENERIC, config());
        register("climber", EffectType.GENERIC, config());
        register("intimidation", EffectType.GENERIC, config());
        register("ore_sight", EffectType.GLOWING, config("duration", 300));
        register("traveler", EffectType.SPEED, config());
        register("instant_mining", EffectType.SPEED, config());
        register("multitool", EffectType.GENERIC, config());
        register("vacuum", EffectType.MAGNETIC, config("radius", 10.0));
        register("builder", EffectType.SPEED, config());
        register("auto_sort", EffectType.GENERIC, config());
        register("transmutation", EffectType.GENERIC, config());
        register("area_mining", EffectType.VEIN_MINE, config());
        register("explosive_shot", EffectType.GENERIC, config());
        register("grappling", EffectType.GENERIC, config());
        register("homing", EffectType.GENERIC, config());
        register("triple_shot", EffectType.GENERIC, config());
        register("vein_miner", EffectType.VEIN_MINE, config());
        register("efficiency", EffectType.SPEED, config());
        register("fortune", EffectType.FORTUNE, config("bonusChance", 0.2));
        register("silk_touch", EffectType.GENERIC, config());
        register("unbreaking", EffectType.GENERIC, config());
        register("mending", EffectType.AUTO_REPAIR, config("repairAmount", 2));
        register("looting", EffectType.FORTUNE, config("bonusChance", 0.3));
        register("sweeping_edge", EffectType.GENERIC, config());
        register("knockback", EffectType.GENERIC, config());
        register("fire_aspect", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 0.5, "duration", 80));
        register("smite", EffectType.CRITICAL, config("critChance", 0.2, "critMultiplier", 2.5));
        register("bane_of_arthropods", EffectType.CRITICAL, config("critChance", 0.2, "critMultiplier", 2.5));
        register("sharpness", EffectType.CRITICAL, config("critChance", 0.15, "critMultiplier", 1.5));
        register("power", EffectType.CRITICAL, config("critChance", 0.15, "critMultiplier", 1.8));
        register("punch", EffectType.GENERIC, config());
        register("flame", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 0.5, "duration", 80));
        register("infinity", EffectType.GENERIC, config());
        register("luck_of_the_sea", EffectType.FORTUNE, config("bonusChance", 0.3));
        register("lure", EffectType.GENERIC, config());
        register("impaling", EffectType.CRITICAL, config("critChance", 0.2, "critMultiplier", 2.5));
        register("riptide", EffectType.GENERIC, config());
        register("master_craftsman", EffectType.GENERIC, config());
        register("time_dilation", EffectType.SPEED, config());
        register("void_reach", EffectType.GENERIC, config());
        register("duplication", EffectType.DOUBLE_DROP, config("doubleChance", 0.5));
        register("angling_expert", EffectType.FORTUNE, config("bonusChance", 0.4));

        plugin.getLogger().info("✅ 已注册 66 个工具效果");
    }

    /**
     * 注册诅咒效果 (12个)
     */
    private void registerCurseEffects() {
        register("curse_fragile", EffectType.DURABILITY_LOSS, config());
        register("curse_sluggish", EffectType.SLOWNESS, config());
        register("curse_noise", EffectType.GENERIC, config());
        register("curse_binding_plus", EffectType.GENERIC, config());
        register("curse_drain", EffectType.WEAKNESS, config());
        register("curse_hunger", EffectType.HUNGER, config());
        register("curse_weakness", EffectType.WEAKNESS, config());
        register("curse_confusion", EffectType.GENERIC, config());
        register("curse_blindness", EffectType.GENERIC, config());
        register("curse_decay", EffectType.DURABILITY_LOSS, config());
        register("curse_echo", EffectType.GENERIC, config());
        register("curse_vengeance", EffectType.REFLECT, config("reflectPercent", 0.5));

        plugin.getLogger().info("✅ 已注册 12 个诅咒效果");
    }

    /**
     * 注册实用效果 (27个)
     */
    private void registerUtilityEffects() {
        register("vanishing", EffectType.GENERIC, config());
        register("banishing", EffectType.GENERIC, config());
        register("silk_touch_utility", EffectType.GENERIC, config());
        register("soft_landing", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.5));
        register("night_vision", EffectType.NIGHT_VISION, config());
        register("jump_boost", EffectType.GENERIC, config());
        register("water_breathing", EffectType.GENERIC, config());
        register("fire_resistance", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.3));
        register("invisibility_aura", EffectType.GENERIC, config());
        register("spirit_guiding", EffectType.GLOWING, config("duration", 200));
        register("auto_repair", EffectType.AUTO_REPAIR, config("repairAmount", 1));
        register("bad_omen", EffectType.GENERIC, config());
        register("conduit_power", EffectType.SPEED, config());
        register("double_drop", EffectType.DOUBLE_DROP, config("doubleChance", 0.3));
        register("glowing", EffectType.GLOWING, config("duration", 100));
        register("hero_of_village", EffectType.GENERIC, config());
        register("levitation", EffectType.GENERIC, config());
        register("luck", EffectType.FORTUNE, config("bonusChance", 0.2));
        register("quick_hands", EffectType.SPEED, config());
        register("quick_swap", EffectType.SPEED, config());
        register("raid_omen", EffectType.GENERIC, config());
        register("scavenger", EffectType.FORTUNE, config("bonusChance", 0.25));
        register("slow_falling", EffectType.DAMAGE_REDUCTION, config("damageReduction", 1.0));
        register("steady_aim", EffectType.CRITICAL, config("critChance", 0.2, "critMultiplier", 1.8));
        register("swift_draw", EffectType.SPEED, config());
        register("unluck", EffectType.GENERIC, config());
        register("water_walk", EffectType.WATER_WALK, config());

        plugin.getLogger().info("✅ 已注册 27 个实用效果");
    }

    /**
     * 注册防御效果 (31个)
     */
    private void registerDefenseEffects() {
        // 防御效果大多重用其他效果
        register("fire_aspect_def", EffectType.REFLECT, config("reflectPercent", 0.3));
        register("smite_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.15));
        register("bane_of_arthropods_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.15));
        register("knockback_def", EffectType.REFLECT, config("reflectPercent", 0.2));
        register("looting_def", EffectType.SHIELD, config("shieldAmount", 2.0));
        register("sweeping_edge_def", EffectType.SHIELD, config("shieldAmount", 2.5));
        register("efficiency_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.1));
        register("fortune_def", EffectType.SHIELD, config("shieldAmount", 3.0));
        register("unbreaking_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.12));
        register("mending_def", EffectType.AUTO_REPAIR, config("repairAmount", 2));
        register("power_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.15));
        register("punch_def", EffectType.DODGE, config("dodgeChance", 0.1));
        register("flame_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.2));
        register("infinity_def", EffectType.SHIELD, config("shieldAmount", 4.0));
        register("luck_of_the_sea_def", EffectType.SPEED, config());
        register("lure_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.1));
        register("impaling_def", EffectType.SHIELD, config("shieldAmount", 2.5));
        register("riptide_def", EffectType.DODGE, config("dodgeChance", 0.12));
        register("sharpness_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.15));
        register("protection_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.2));
        register("fire_protection_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.25));
        register("blast_protection_def", EffectType.SHIELD, config("shieldAmount", 3.0));
        register("projectile_protection_def", EffectType.SHIELD, config("shieldAmount", 3.0));
        register("feather_falling_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.3));
        register("respiration_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.1));
        register("aqua_affinity_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.1));
        register("depth_strider_def", EffectType.SHIELD, config("shieldAmount", 2.0));
        register("soul_speed_def", EffectType.SPEED, config());
        register("swift_sneak_def", EffectType.DODGE, config("dodgeChance", 0.15));
        register("frost_walker_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.15));
        register("binding_curse_def", EffectType.REFLECT, config("reflectPercent", 0.3));
        register("vanishing_curse_def", EffectType.DAMAGE_REDUCTION, config("damageReduction", 0.1));

        plugin.getLogger().info("✅ 已注册 31 个防御效果");
    }

    /**
     * 注册特殊效果 (5个)
     */
    private void registerSpecialEffects() {
        register("meteor_strike", EffectType.GENERIC, config());
        register("wither_strike", EffectType.DAMAGE_OVER_TIME, config("damagePerSecond", 1.0, "duration", 100));
        register("ender_strike", EffectType.GENERIC, config());
        register("tidal_wave", EffectType.GENERIC, config());
        register("earthquake", EffectType.STUN, config("stunDuration", 60));

        plugin.getLogger().info("✅ 已注册 5 个特殊效果");
    }

    /**
     * 注册单个效果
     */
    private void register(String enchantId, EffectType type, Map<String, Object> config) {
        manager.registerEffect(enchantId, new UniversalEffect(plugin, type, config));
    }

    /**
     * 创建配置
     */
    private Map<String, Object> config(Object... pairs) {
        Map<String, Object> config = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            if (i + 1 < pairs.length) {
                config.put((String) pairs[i], pairs[i + 1]);
            }
        }
        return config;
    }
}
