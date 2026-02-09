package com.enadd.core.enchantment;

import com.enadd.core.enchantment.effects.combat.BleedingEffect;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * 效果注册器 - 注册所有附魔效果
 */
public final class EffectRegistry {

    private final JavaPlugin plugin;
    private final EnchantmentEffectManager manager;

    public EffectRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.manager = EnchantmentEffectManager.getInstance();
    }

    /**
     * 注册所有效果
     */
    public void registerAll() {
        registerCombatEffects();
        registerArmorEffects();
        registerToolEffects();
        registerUtilityEffects();
        registerCurseEffects();
        registerDefenseEffects();
        registerSpecialEffects();

        plugin.getLogger().info("已注册 " + manager.getRegisteredEffectCount() + " 个附魔效果");
    }

    /**
     * 注册战斗效果
     */
    private void registerCombatEffects() {
        // 核心战斗效果
        manager.registerEffect("bleeding", new BleedingEffect(plugin));
        manager.registerEffect("vampirism", new VampirismEffect(plugin));
        manager.registerEffect("critical_strike", new CriticalStrikeEffect(plugin));
        manager.registerEffect("life_steal", new LifeStealEffect(plugin));
        manager.registerEffect("execution", new ExecutionEffect(plugin));
        manager.registerEffect("backstab", new BackstabEffect(plugin));
        manager.registerEffect("disarm", new DisarmEffect(plugin));
        manager.registerEffect("armor_pierce", new ArmorPierceEffect(plugin));
        manager.registerEffect("momentum", new MomentumEffect(plugin));
        manager.registerEffect("stagger", new StaggerEffect(plugin));

        // 元素伤害效果
        manager.registerEffect("flame_blade", new FlameBladeEffect(plugin));
        manager.registerEffect("frost_blade", new FrostBladeEffect(plugin));
        manager.registerEffect("thunder_blade", new ThunderBladeEffect(plugin));
        manager.registerEffect("venom_blade", new VenomBladeEffect(plugin));

        // 特殊攻击效果
        manager.registerEffect("chain_lightning", new ChainLightningEffect(plugin));
        manager.registerEffect("whirlwind", new WhirlwindEffect(plugin));
        manager.registerEffect("shield_bash", new ShieldBashEffect(plugin));

        // 远程攻击效果
        manager.registerEffect("piercing_shot", new PiercingShotEffect(plugin));
        manager.registerEffect("explosive_arrow", new ExplosiveArrowEffect(plugin));
        manager.registerEffect("lightning_arrow", new LightningArrowEffect(plugin));

        plugin.getLogger().info("✅ 已注册 20+ 个战斗效果");
    }

    /**
     * 注册护甲效果
     */
    private void registerArmorEffects() {
        manager.registerEffect("dodge", new DodgeEffect(plugin));
        manager.registerEffect("thorns", new ThornsEffect(plugin));
        manager.registerEffect("stone_skin", new StoneSkinEffect(plugin));
        manager.registerEffect("last_stand", new LastStandEffect(plugin));
        manager.registerEffect("barrier", new BarrierEffect(plugin));
        manager.registerEffect("reinforced_thorns", new ReinforcedThornsEffect(plugin));
        manager.registerEffect("evasive", new EvasiveEffect(plugin));

        plugin.getLogger().info("✅ 已注册 7+ 个护甲效果");
    }

    /**
     * 注册工具效果
     */
    private void registerToolEffects() {
        manager.registerEffect("vein_miner", new VeinMinerEffect(plugin));
        manager.registerEffect("auto_smelt", new AutoSmeltEffect(plugin));
        manager.registerEffect("magnetic", new MagneticEffect(plugin));
        manager.registerEffect("fortune", new FortuneEffect(plugin));
        manager.registerEffect("efficiency", new EfficiencyEffect(plugin));
        manager.registerEffect("silk_touch", new SilkTouchEffect(plugin));
        manager.registerEffect("area_mining", new AreaMiningEffect(plugin));
        manager.registerEffect("instant_mining", new InstantMiningEffect(plugin));

        plugin.getLogger().info("✅ 已注册 8+ 个工具效果");
    }

    /**
     * 注册实用效果
     */
    private void registerUtilityEffects() {
        manager.registerEffect("auto_repair", new AutoRepairEffect(plugin));
        manager.registerEffect("double_drop", new DoubleDropEffect(plugin));
        manager.registerEffect("night_vision", new UniversalEffect(plugin, UniversalEffect.EffectType.NIGHT_VISION, null));
        manager.registerEffect("water_walk", new WaterWalkEffect(plugin));
        manager.registerEffect("glowing", new GlowingEffect(plugin));
        manager.registerEffect("soft_landing", new SoftLandingEffect(plugin));

        plugin.getLogger().info("✅ 已注册 6+ 个实用效果");
    }

    /**
     * 注册诅咒效果
     */
    private void registerCurseEffects() {
        manager.registerEffect("curse_fragile", new CurseFragileEffect(plugin));
        manager.registerEffect("curse_sluggish", new CurseSluggishEffect(plugin));
        manager.registerEffect("curse_weakness", new CurseWeaknessEffect(plugin));
        manager.registerEffect("curse_hunger", new CurseHungerEffect(plugin));

        plugin.getLogger().info("✅ 已注册 4+ 个诅咒效果");
    }

    /**
     * 注册防御效果
     */
    private void registerDefenseEffects() {
        // 防御效果大多重用护甲效果
        plugin.getLogger().info("✅ 防御效果已通过护甲效果实现");
    }

    /**
     * 注册特殊效果
     */
    private void registerSpecialEffects() {
        manager.registerEffect("meteor_strike", new MeteorStrikeEffect(plugin));
        manager.registerEffect("earthquake", new EarthquakeEffect(plugin));

        plugin.getLogger().info("✅ 已注册 2+ 个特殊效果");
    }
}
