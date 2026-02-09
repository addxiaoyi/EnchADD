package com.enadd.core.visualization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;


/**
 * 综合冲突列表生成器
 * 生成展示原版和扩展附魔关系的冲突列表
 */
public class ComprehensiveConflictListGenerator {

    private final Gson gson;

    public ComprehensiveConflictListGenerator() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * 生成综合冲突列表JSON文件
     */
    public void generateComprehensiveConflictList() {
        try {
            // 生成冲突列表数据
            JsonObject conflictListData = createConflictListData();

            // 写入JSON文件
            String outputPath = Paths.get("docs", "comprehensive_conflict_list.json").toString();
            try (FileWriter writer = new FileWriter(outputPath)) {
                gson.toJson(conflictListData, writer);
            }

            System.out.println("综合冲突列表生成完成！");
            System.out.println("生成了 comprehensive_conflict_list.json 文件");

        } catch (Exception e) {
            System.err.println("生成综合冲突列表时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建冲突列表数据
     */
    private JsonObject createConflictListData() {
        JsonObject data = new JsonObject();

        // 添加说明信息
        data.addProperty("description", "Minecraft 附魔冲突关系综合列表");
        data.addProperty("generated_at", new Date().toString());

        // 添加原版附魔列表
        JsonObject vanillaEnchantments = new JsonObject();
        addVanillaEnchantments(vanillaEnchantments);
        data.add("vanilla_enchantments", vanillaEnchantments);

        // 添加扩展附魔列表
        JsonObject extendedEnchantments = new JsonObject();
        addExtendedEnchantments(extendedEnchantments);
        data.add("extended_enchantments", extendedEnchantments);

        // 添加冲突关系
        JsonObject conflictRelations = new JsonObject();
        addConflictRelations(conflictRelations, vanillaEnchantments, extendedEnchantments);
        data.add("conflict_relations", conflictRelations);

        // 添加兼容关系
        JsonObject compatibleRelations = new JsonObject();
        addCompatibleRelations(compatibleRelations, vanillaEnchantments, extendedEnchantments);
        data.add("compatible_relations", compatibleRelations);

        return data;
    }

    /**
     * 添加原版附魔数据
     */
    private void addVanillaEnchantments(JsonObject vanillaEnchantments) {
        // 原版武器附魔
        JsonObject weaponEnchants = new JsonObject();
        weaponEnchants.addProperty("sharpness", "锋利");
        weaponEnchants.addProperty("smite", "亡灵杀手");
        weaponEnchants.addProperty("bane_of_arthropods", "节肢杀手");
        weaponEnchants.addProperty("knockback", "击退");
        weaponEnchants.addProperty("fire_aspect", "火焰附加");
        weaponEnchants.addProperty("looting", "抢夺");
        weaponEnchants.addProperty("sweeping_edge", "横扫之刃");
        weaponEnchants.addProperty("unbreaking", "耐久");
        weaponEnchants.addProperty("mending", "经验修补");
        weaponEnchants.addProperty("vanishing_curse", "消失诅咒");
        vanillaEnchantments.add("weapon", weaponEnchants);

        // 原版弓附魔
        JsonObject bowEnchants = new JsonObject();
        bowEnchants.addProperty("power", "力量");
        bowEnchants.addProperty("punch", "冲击");
        bowEnchants.addProperty("flame", "火矢");
        bowEnchants.addProperty("infinity", "无限");
        bowEnchants.addProperty("unbreaking", "耐久");
        bowEnchants.addProperty("mending", "经验修补");
        bowEnchants.addProperty("vanishing_curse", "消失诅咒");
        vanillaEnchantments.add("bow", bowEnchants);

        // 原版护甲附魔
        JsonObject armorEnchants = new JsonObject();
        armorEnchants.addProperty("protection", "保护");
        armorEnchants.addProperty("fire_protection", "火焰保护");
        armorEnchants.addProperty("blast_protection", "爆炸保护");
        armorEnchants.addProperty("projectile_protection", "弹射物保护");
        armorEnchants.addProperty("thorns", "荆棘");
        armorEnchants.addProperty("unbreaking", "耐久");
        armorEnchants.addProperty("mending", "经验修补");
        armorEnchants.addProperty("vanishing_curse", "消失诅咒");
        vanillaEnchantments.add("armor", armorEnchants);

        // 原版工具附魔
        JsonObject toolEnchants = new JsonObject();
        toolEnchants.addProperty("efficiency", "效率");
        toolEnchants.addProperty("fortune", "时运");
        toolEnchants.addProperty("silk_touch", "精准采集");
        toolEnchants.addProperty("unbreaking", "耐久");
        toolEnchants.addProperty("mending", "经验修补");
        toolEnchants.addProperty("vanishing_curse", "消失诅咒");
        vanillaEnchantments.add("tool", toolEnchants);

        // 原版钓鱼竿附魔
        JsonObject fishingEnchants = new JsonObject();
        fishingEnchants.addProperty("luck_of_the_sea", "海之眷顾");
        fishingEnchants.addProperty("lure", "饵钓");
        fishingEnchants.addProperty("unbreaking", "耐久");
        fishingEnchants.addProperty("mending", "经验修补");
        fishingEnchants.addProperty("vanishing_curse", "消失诅咒");
        vanillaEnchantments.add("fishing_rod", fishingEnchants);

        // 原版三叉戟附魔
        JsonObject tridentEnchants = new JsonObject();
        tridentEnchants.addProperty("impaling", "穿刺");
        tridentEnchants.addProperty("riptide", "激流");
        tridentEnchants.addProperty("loyalty", "忠诚");
        tridentEnchants.addProperty("channeling", "引雷");
        tridentEnchants.addProperty("unbreaking", "耐久");
        tridentEnchants.addProperty("mending", "经验修补");
        tridentEnchants.addProperty("vanishing_curse", "消失诅咒");
        vanillaEnchantments.add("trident", tridentEnchants);

        // 原版马铠附魔
        JsonObject horseArmorEnchants = new JsonObject();
        horseArmorEnchants.addProperty("protection", "保护");
        horseArmorEnchants.addProperty("unbreaking", "耐久");
        horseArmorEnchants.addProperty("mending", "经验修补");
        horseArmorEnchants.addProperty("vanishing_curse", "消失诅咒");
        vanillaEnchantments.add("horse_armor", horseArmorEnchants);
    }

    /**
     * 添加扩展附魔数据
     */
    private void addExtendedEnchantments(JsonObject extendedEnchantments) {
        // 战斗附魔
        JsonObject combatEnchants = new JsonObject();
        combatEnchants.addProperty("bleeding", "撕裂");
        combatEnchants.addProperty("armor_pierce", "穿甲");
        combatEnchants.addProperty("execution", "处决");
        combatEnchants.addProperty("momentum", "动能");
        combatEnchants.addProperty("disarm", "缴械");
        combatEnchants.addProperty("crippling", "残废");
        combatEnchants.addProperty("reprisal", "复仇");
        combatEnchants.addProperty("hemorrhage", "大失血");
        combatEnchants.addProperty("backstab", "背刺");
        combatEnchants.addProperty("stagger", "击晕");
        combatEnchants.addProperty("rend", "撕裂伤口");
        combatEnchants.addProperty("savage", "野蛮");
        combatEnchants.addProperty("duelist", "决斗者");
        combatEnchants.addProperty("vampirism", "吸血鬼");
        combatEnchants.addProperty("critical_strike", "暴击");
        combatEnchants.addProperty("berserker_rage", "狂战士之怒");
        combatEnchants.addProperty("bloodlust", "血饥渴");
        combatEnchants.addProperty("chain_lightning", "连锁闪电");
        combatEnchants.addProperty("death_mark", "死亡标记");
        combatEnchants.addProperty("dragon_breath", "龙息");
        combatEnchants.addProperty("void_slash", "虚空斩");
        combatEnchants.addProperty("thunder_strike", "雷击");
        combatEnchants.addProperty("shadow_strike", "暗影突袭");
        combatEnchants.addProperty("mana_steal", "法力汲取");
        combatEnchants.addProperty("poison_cloud", "毒云");
        combatEnchants.addProperty("soul_burn", "灵魂灼烧");
        combatEnchants.addProperty("aegis", "守护");
        combatEnchants.addProperty("arrow_rain", "箭雨");
        combatEnchants.addProperty("ice_prison", "寒冰牢笼");
        combatEnchants.addProperty("blade_dance", "刀锋舞");
        combatEnchants.addProperty("blood_pact", "血之契约");
        combatEnchants.addProperty("frenzy", "疯狂");
        combatEnchants.addProperty("grim_harvest", "收割");
        combatEnchants.addProperty("phantom_strike", "幽灵突袭");
        combatEnchants.addProperty("wind_slash", "风斩");
        combatEnchants.addProperty("earth_shatter", "碎裂大地");
        combatEnchants.addProperty("flame_blade", "烈焰之刃");
        combatEnchants.addProperty("frost_blade", "寒冰之刃");
        combatEnchants.addProperty("thunder_blade", "雷霆之刃");
        combatEnchants.addProperty("venom_blade", "毒刃");
        combatEnchants.addProperty("life_steal", "生命偷取");
        combatEnchants.addProperty("mana_burn", "法力燃烧");
        combatEnchants.addProperty("armor_break", "破甲");
        combatEnchants.addProperty("shield_bash", "盾牌猛击");
        combatEnchants.addProperty("whirlwind", "旋风斩");
        combatEnchants.addProperty("piercing_shot", "穿透射击");
        combatEnchants.addProperty("rapid_fire", "快速射击");
        combatEnchants.addProperty("charge_shot", "蓄力射击");
        combatEnchants.addProperty("scatter_shot", "散射");
        combatEnchants.addProperty("toxic_shot", "毒性射击");
        combatEnchants.addProperty("ice_shot", "冰冻射击");
        combatEnchants.addProperty("explosive_arrow", "爆炸箭");
        combatEnchants.addProperty("lightning_arrow", "雷电箭");
        combatEnchants.addProperty("vampire_arrow", "吸血箭");
        combatEnchants.addProperty("tactical_strike", "战术打击");
        combatEnchants.addProperty("relentless", "无情");
        extendedEnchantments.add("combat", combatEnchants);

        // 防具附魔
        JsonObject armorEnchants = new JsonObject();
        armorEnchants.addProperty("stone_skin", "石肤");
        armorEnchants.addProperty("dodge", "闪避");
        armorEnchants.addProperty("reinforced_thorns", "强化荆棘");
        armorEnchants.addProperty("barrier", "屏障");
        armorEnchants.addProperty("adrenaline", "肾上腺素");
        armorEnchants.addProperty("willpower", "意志力");
        armorEnchants.addProperty("grounding", "接地");
        armorEnchants.addProperty("thermostatic", "恒温");
        armorEnchants.addProperty("iron_will", "钢铁意志");
        armorEnchants.addProperty("recoil", "反冲");
        armorEnchants.addProperty("endurance", "耐力");
        armorEnchants.addProperty("last_stand", "最后坚守");
        armorEnchants.addProperty("aegis_armor", "庇护");
        armorEnchants.addProperty("celestial_blessing", "天赐");
        armorEnchants.addProperty("divine_protection", "神圣守护");
        armorEnchants.addProperty("evasive", "规避");
        armorEnchants.addProperty("magma_walker", "岩浆行者");
        armorEnchants.addProperty("frost_walker", "冰霜行者");
        armorEnchants.addProperty("soul_speed", "灵魂疾行");
        armorEnchants.addProperty("depth_strider", "深海探索者");
        armorEnchants.addProperty("feather_falling", "摔落缓冲");
        armorEnchants.addProperty("respiration", "水下呼吸");
        armorEnchants.addProperty("aqua_affinity", "水下速掘");
        armorEnchants.addProperty("thorns", "荆棘");
        armorEnchants.addProperty("protection", "保护");
        armorEnchants.addProperty("fire_protection", "火焰保护");
        armorEnchants.addProperty("blast_protection", "爆炸保护");
        armorEnchants.addProperty("projectile_protection", "弹射物保护");
        armorEnchants.addProperty("swift_sneak", "迅捷潜行");
        armorEnchants.addProperty("soul_fire", "灵魂火");
        armorEnchants.addProperty("warden_bane", "监守者克星");
        armorEnchants.addProperty("fortress", "堡垒");
        armorEnchants.addProperty("second_wind", "二次呼吸");
        extendedEnchantments.add("armor", armorEnchants);

        // 工具附魔
        JsonObject toolEnchants = new JsonObject();
        toolEnchants.addProperty("miner", "矿工");
        toolEnchants.addProperty("prospecting", "勘探");
        toolEnchants.addProperty("auto_smelt", "自动熔炼");
        toolEnchants.addProperty("magnetic", "磁性");
        toolEnchants.addProperty("precision", "精准");
        toolEnchants.addProperty("strong_draw", "强拉");
        toolEnchants.addProperty("catapult", "弹射");
        toolEnchants.addProperty("enhanced_piercing", "强化穿透");
        toolEnchants.addProperty("sniper", "狙击");
        toolEnchants.addProperty("frost_arrow", "冰霜箭");
        toolEnchants.addProperty("signal_arrow", "信号箭");
        toolEnchants.addProperty("silence", "沉默");
        toolEnchants.addProperty("harvest", "丰收");
        toolEnchants.addProperty("titan_strength", "泰坦之力");
        toolEnchants.addProperty("lightning_speed", "闪电速度");
        toolEnchants.addProperty("combo_breaker", "连击终结者");
        toolEnchants.addProperty("arbor_master", "树木大师");
        toolEnchants.addProperty("fortunes_grace", "幸运恩赐");
        toolEnchants.addProperty("smelting_touch", "熔炼触碰");
        toolEnchants.addProperty("collector", "收藏家");
        toolEnchants.addProperty("speed_surge", "速度激增");
        toolEnchants.addProperty("ethereal_step", "虚无步伐");
        toolEnchants.addProperty("heavy_hand", "重击");
        toolEnchants.addProperty("shadow_veil", "阴影面纱");
        toolEnchants.addProperty("climber", "攀爬者");
        toolEnchants.addProperty("intimidation", "威慑");
        toolEnchants.addProperty("ore_sight", "矿石视觉");
        toolEnchants.addProperty("traveler", "旅行者");
        toolEnchants.addProperty("instant_mining", "瞬间挖掘");
        toolEnchants.addProperty("multitool", "多功能工具");
        toolEnchants.addProperty("vacuum", "真空");
        toolEnchants.addProperty("builder", "建造者");
        toolEnchants.addProperty("auto_sort", "自动排序");
        toolEnchants.addProperty("transmutation", "变形");
        toolEnchants.addProperty("area_mining", "区域挖掘");
        toolEnchants.addProperty("explosive_shot", "爆炸射击");
        toolEnchants.addProperty("grappling", "抓钩");
        toolEnchants.addProperty("homing", "追踪");
        toolEnchants.addProperty("triple_shot", "三重射击");
        toolEnchants.addProperty("vein_miner", "矿脉挖掘");
        toolEnchants.addProperty("efficiency", "效率");
        toolEnchants.addProperty("fortune", "时运");
        toolEnchants.addProperty("silk_touch", "精准采集");
        toolEnchants.addProperty("unbreaking", "耐久");
        toolEnchants.addProperty("mending", "经验修补");
        toolEnchants.addProperty("looting", "抢夺");
        toolEnchants.addProperty("sweeping_edge", "横扫之刃");
        toolEnchants.addProperty("knockback", "击退");
        toolEnchants.addProperty("fire_aspect", "火焰附加");
        toolEnchants.addProperty("smite", "亡灵杀手");
        toolEnchants.addProperty("bane_of_arthropods", "节肢杀手");
        toolEnchants.addProperty("sharpness", "锋利");
        toolEnchants.addProperty("power", "力量");
        toolEnchants.addProperty("punch", "冲击");
        toolEnchants.addProperty("flame", "火矢");
        toolEnchants.addProperty("infinity", "无限");
        toolEnchants.addProperty("luck_of_the_sea", "海之眷顾");
        toolEnchants.addProperty("lure", "饵钓");
        toolEnchants.addProperty("impaling", "穿刺");
        toolEnchants.addProperty("riptide", "激流");
        toolEnchants.addProperty("master_craftsman", "工匠大师");
        toolEnchants.addProperty("time_dilation", "时间延展");
        toolEnchants.addProperty("void_reach", "虚空触及");
        toolEnchants.addProperty("duplication", "复制");
        toolEnchants.addProperty("angling_expert", "垂钓专家");
        extendedEnchantments.add("tool", toolEnchants);

        // 诅咒附魔
        JsonObject curseEnchants = new JsonObject();
        curseEnchants.addProperty("curse_fragile", "脆弱诅咒");
        curseEnchants.addProperty("curse_sluggish", "迟缓诅咒");
        curseEnchants.addProperty("curse_noise", "噪音诅咒");
        curseEnchants.addProperty("curse_binding_plus", "绑定诅咒+");
        curseEnchants.addProperty("curse_drain", "衰竭诅咒");
        curseEnchants.addProperty("curse_hunger", "饥饿诅咒");
        curseEnchants.addProperty("curse_weakness", "虚弱诅咒");
        curseEnchants.addProperty("curse_confusion", "混乱诅咒");
        curseEnchants.addProperty("curse_blindness", "失明诅咒");
        curseEnchants.addProperty("curse_decay", "腐朽诅咒");
        curseEnchants.addProperty("curse_echo", "回响诅咒");
        curseEnchants.addProperty("curse_vengeance", "复仇诅咒");
        extendedEnchantments.add("curse", curseEnchants);

        // 实用附魔
        JsonObject utilityEnchants = new JsonObject();
        utilityEnchants.addProperty("invisibility", "隐身");
        utilityEnchants.addProperty("soul_bound", "灵魂绑定");
        utilityEnchants.addProperty("tracker", "追踪");
        utilityEnchants.addProperty("soft_landing", "软着陆");
        utilityEnchants.addProperty("night_vision", "夜视");
        utilityEnchants.addProperty("light_footed", "轻盈步伐");
        utilityEnchants.addProperty("dolphins_grace", "海豚的优雅");
        utilityEnchants.addProperty("light_source", "光源");
        utilityEnchants.addProperty("auto_repair", "自动修复");
        utilityEnchants.addProperty("bad_omen", "不祥之兆");
        utilityEnchants.addProperty("conduit_power", "潮涌能量");
        utilityEnchants.addProperty("double_drop", "双倍掉落");
        utilityEnchants.addProperty("glowing", "发光");
        utilityEnchants.addProperty("hero_of_village", "村庄英雄");
        utilityEnchants.addProperty("levitation", "飘浮");
        utilityEnchants.addProperty("luck", "幸运");
        utilityEnchants.addProperty("quick_hands", "快手");
        utilityEnchants.addProperty("quick_swap", "快速交换");
        utilityEnchants.addProperty("raid_omen", "袭击预兆");
        utilityEnchants.addProperty("scavenger", "清道夫");
        utilityEnchants.addProperty("slow_falling", "缓降");
        utilityEnchants.addProperty("steady_aim", "稳定瞄准");
        utilityEnchants.addProperty("swift_draw", "快速拉弦");
        utilityEnchants.addProperty("unluck", "霉运");
        utilityEnchants.addProperty("water_walk", "水上行走");
        extendedEnchantments.add("utility", utilityEnchants);

        // 防御附魔
        JsonObject defenseEnchants = new JsonObject();
        defenseEnchants.addProperty("reflect", "反射");
        defenseEnchants.addProperty("damage_absorption", "伤害吸收");
        defenseEnchants.addProperty("elemental_resist", "元素抗性");
        defenseEnchants.addProperty("physical_barrier", "物理屏障");
        defenseEnchants.addProperty("magic_barrier", "魔法屏障");
        defenseEnchants.addProperty("immunity", "免疫");
        defenseEnchants.addProperty("energy_shield", "能量护盾");
        defenseEnchants.addProperty("resistance", "抗性");
        defenseEnchants.addProperty("strength", "力量");
        defenseEnchants.addProperty("jump_boost", "跳跃提升");
        defenseEnchants.addProperty("fire_resistance", "抗火");
        defenseEnchants.addProperty("water_breathing", "水下呼吸");
        defenseEnchants.addProperty("haste", "急迫");
        defenseEnchants.addProperty("health_boost", "生命提升");
        defenseEnchants.addProperty("absorption", "吸收");
        defenseEnchants.addProperty("invisibility_aura", "隐身光环");
        defenseEnchants.addProperty("saturation", "饱和");
        defenseEnchants.addProperty("counter_attack", "反击");
        defenseEnchants.addProperty("adaptation", "适应");
        defenseEnchants.addProperty("regeneration", "再生");
        extendedEnchantments.add("defense", defenseEnchants);

        // 特殊附魔
        JsonObject specialEnchants = new JsonObject();
        specialEnchants.addProperty("meteor_strike", "流星打击");
        specialEnchants.addProperty("wither_strike", "凋灵打击");
        specialEnchants.addProperty("ender_strike", "末影打击");
        specialEnchants.addProperty("tidal_wave", "巨浪");
        specialEnchants.addProperty("earthquake", "地震");
        extendedEnchantments.add("special", specialEnchants);
    }

    /**
     * 添加冲突关系
     */
    private void addConflictRelations(JsonObject conflictRelations, JsonObject vanillaEnchantments, JsonObject extendedEnchantments) {
        // 原版武器与扩展附魔冲突
        JsonObject vanillaWeaponConflicts = new JsonObject();
        vanillaWeaponConflicts.add("conflicts_with_extended_combat", gson.toJsonTree(Arrays.asList(
            "sharpness", "smite", "bane_of_arthropods", "looting", "sweeping_edge"
        )));
        vanillaWeaponConflicts.add("conflicts_with_extended_special", gson.toJsonTree(Arrays.asList(
            "sweeping_edge", "looting"
        )));
        conflictRelations.add("vanilla_weapon", vanillaWeaponConflicts);

        // 原版护甲与扩展附魔冲突
        JsonObject vanillaArmorConflicts = new JsonObject();
        vanillaArmorConflicts.add("conflicts_with_extended_curse", gson.toJsonTree(Arrays.asList(
            "protection", "thorns", "unbreaking", "mending"
        )));
        vanillaArmorConflicts.add("conflicts_with_extended_special", gson.toJsonTree(Arrays.asList(
            "thorns", "protection"
        )));
        conflictRelations.add("vanilla_armor", vanillaArmorConflicts);

        // 扩展附魔内部冲突
        JsonObject extendedCombatConflicts = new JsonObject();
        extendedCombatConflicts.add("conflicts_with_extended_combat", gson.toJsonTree(Arrays.asList(
            "sharpness", "smite", "bane_of_arthropods", "looting", "sweeping_edge"
        )));
        extendedCombatConflicts.add("conflicts_with_extended_curse", gson.toJsonTree(Arrays.asList(
            "looting", "sweeping_edge"
        )));
        conflictRelations.add("extended_combat", extendedCombatConflicts);

        JsonObject extendedCurseConflicts = new JsonObject();
        extendedCurseConflicts.add("conflicts_with_all_other_categories", gson.toJsonTree(Arrays.asList(
            "protection", "thorns", "unbreaking", "mending", "sharpness", "efficiency", "fortune"
        )));
        conflictRelations.add("extended_curse", extendedCurseConflicts);
    }

    /**
     * 添加兼容关系
     */
    private void addCompatibleRelations(JsonObject compatibleRelations, JsonObject vanillaEnchantments, JsonObject extendedEnchantments) {
        // 原版工具与扩展工具附魔兼容
        JsonObject vanillaToolCompatibility = new JsonObject();
        vanillaToolCompatibility.add("compatible_with_extended_tool", gson.toJsonTree(Arrays.asList(
            "efficiency", "fortune", "silk_touch", "unbreaking", "mending"
        )));
        compatibleRelations.add("vanilla_tool", vanillaToolCompatibility);

        // 原版弓与扩展弓附魔兼容
        JsonObject vanillaBowCompatibility = new JsonObject();
        vanillaBowCompatibility.add("compatible_with_extended_combat", gson.toJsonTree(Arrays.asList(
            "power", "punch", "flame", "infinity", "unbreaking", "mending"
        )));
        compatibleRelations.add("vanilla_bow", vanillaBowCompatibility);

        // 原版钓鱼竿与扩展工具附魔兼容
        JsonObject vanillaFishingCompatibility = new JsonObject();
        vanillaFishingCompatibility.add("compatible_with_extended_utility", gson.toJsonTree(Arrays.asList(
            "luck_of_the_sea", "lure", "unbreaking", "mending"
        )));
        compatibleRelations.add("vanilla_fishing_rod", vanillaFishingCompatibility);
    }
}
