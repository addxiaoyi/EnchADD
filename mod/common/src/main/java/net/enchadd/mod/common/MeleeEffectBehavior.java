package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MeleeEffectBehavior {
    private static final ResourceKey<Enchantment> CINDER = key("cinder");
    private static final ResourceKey<Enchantment> LANCER = key("lancer");
    private static final ResourceKey<Enchantment> REACH = key("reach");
    private static final ResourceKey<Enchantment> UPDRAFT = key("updraft");
    private static final ResourceKey<Enchantment> TREMOR = key("tremor");
    private static final ResourceKey<Enchantment> RALLY = key("rally");
    private static final ResourceKey<Enchantment> INITIATIVE = key("initiative");
    private static final ResourceKey<Enchantment> SHADOWSTRIKE = key("shadowstrike");
    private static final ResourceKey<Enchantment> PURSUIT = key("pursuit");
    private static final ResourceKey<Enchantment> WINGCLIP = key("wingclip");
    private static final ResourceKey<Enchantment> EXECUTIONER = key("executioner");
    private static final ResourceKey<Enchantment> QUELL = key("quell");
    private static final ResourceKey<Enchantment> SKIM = key("skim");
    private static final ResourceKey<Enchantment> SIDESTEP = key("sidestep");
    private static final ResourceKey<Enchantment> BRITTLE = key("brittle");
    private static final ResourceKey<Enchantment> GRAVITATION = key("gravitation");
    private static final ResourceKey<Enchantment> CADENCE = key("cadence");
    private static final ResourceKey<Enchantment> TRACER = key("tracer");
    private static final ResourceKey<Enchantment> UNDERDOG = key("underdog");
    private static final Map<UUID, Long> TREMOR_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, RallyWindow> RALLY_WINDOWS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> SHADOW_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> QUELL_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> SIDESTEP_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> CADENCE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> SPEAR_COOLDOWNS = new ConcurrentHashMap<>();
    private MeleeEffectBehavior() {}

    public static float adjustDamage(LivingEntity victim, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f) return amount;
        if (victim instanceof ServerPlayer brittlePlayer && source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
            Holder<Enchantment> brittleHolder = brittlePlayer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(BRITTLE);
            int brittleLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(brittleHolder, brittlePlayer.getItemBySlot(EquipmentSlot.LEGS)));
            if (brittleLevel > 0) amount *= (float) Math.min(2.0, 1.0 + 0.25 * brittleLevel);
            Holder<Enchantment> gravitationHolder = brittlePlayer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(GRAVITATION);
            int gravitationLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(gravitationHolder, brittlePlayer.getItemBySlot(EquipmentSlot.FEET)));
            if (gravitationLevel > 0) amount *= (float) Math.min(3.0, 1.0 + 0.35 * gravitationLevel);
        }
        if (victim instanceof ServerPlayer sidestepPlayer && source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile
                && sidestepPlayer.isSprinting()) {
            Holder<Enchantment> sidestepHolder = sidestepPlayer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SIDESTEP);
            int sidestepLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(sidestepHolder, sidestepPlayer.getItemBySlot(EquipmentSlot.LEGS)));
            long now = sidestepPlayer.level().getGameTime();
            if (sidestepLevel > 0 && now >= SIDESTEP_COOLDOWNS.getOrDefault(sidestepPlayer.getUUID(), 0L)
                    && sidestepPlayer.getRandom().nextDouble() < Math.min(0.65, 0.25 * sidestepLevel)) {
                amount *= (float) (1.0 - Math.min(0.5, 0.12 * sidestepLevel));
                sidestepPlayer.addEffect(new MobEffectInstance(MobEffects.SPEED, Math.max(40, sidestepLevel * 2 * 20), 0, false, false, true));
                SIDESTEP_COOLDOWNS.put(sidestepPlayer.getUUID(), now + 160);
            }
        }
        if (victim instanceof ServerPlayer skimPlayer && source.is(net.minecraft.world.damagesource.DamageTypes.FLY_INTO_WALL)) {
            Holder<Enchantment> skimHolder = skimPlayer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SKIM);
            int skimLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(skimHolder, skimPlayer.getItemBySlot(EquipmentSlot.CHEST)));
            if (skimLevel > 0) amount = Math.max(0.0f, amount - Math.min(3.0f, skimLevel));
        }
        if (victim instanceof ServerPlayer defender && (source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC) || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER))) {
            Holder<Enchantment> quellHolder = defender.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(QUELL);
            int quellLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(quellHolder, defender.getItemBySlot(EquipmentSlot.CHEST)));
            long now = defender.level().getGameTime();
            if (quellLevel > 0 && now >= QUELL_COOLDOWNS.getOrDefault(defender.getUUID(), 0L)
                    && defender.getRandom().nextDouble() < Math.min(0.6, 0.3 * quellLevel)) {
                QUELL_COOLDOWNS.put(defender.getUUID(), now + 80);
                amount *= (float) (1.0 - Math.min(0.5, 0.2 * quellLevel));
            }
        }
        if (!(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) return amount;
        if (victim instanceof ServerPlayer defender && source.getEntity() instanceof LivingEntity) {
            Holder<Enchantment> rallyHolder = defender.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(RALLY);
            int rallyLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(rallyHolder, defender.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST)));
            if (rallyLevel > 0) RALLY_WINDOWS.put(defender.getUUID(), new RallyWindow(rallyLevel, defender.level().getGameTime() + 60));
        }

        double bonus = 0.0;
        if (attacker instanceof ServerPlayer spearPlayer && isSpear(spearPlayer.getMainHandItem())) {
            long now = spearPlayer.level().getGameTime();
            if (now >= SPEAR_COOLDOWNS.getOrDefault(spearPlayer.getUUID(), 0L)) {
                Holder<Enchantment> lancerHolder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(LANCER);
                int lancerLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(lancerHolder, attacker.getMainHandItem()));
                if (lancerLevel > 0 && spearPlayer.isSprinting()) bonus += amount * Math.min(0.24, 0.08 * lancerLevel);
                Holder<Enchantment> reachHolder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(REACH);
                int reachLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(reachHolder, attacker.getMainHandItem()));
                double distance = attacker.position().distanceTo(victim.position());
                if (reachLevel > 0 && distance > 3.5) {
                    double factor = Math.min(1.0, (Math.min(distance, 7.0) - 3.5) / 3.5);
                    bonus += amount * Math.min(0.18, 0.06 * reachLevel) * factor;
                }
                if (lancerLevel > 0 || reachLevel > 0) SPEAR_COOLDOWNS.put(spearPlayer.getUUID(), now + 24);
            }
        }
        if (attacker instanceof ServerPlayer striker) {
            RallyWindow window = RALLY_WINDOWS.remove(striker.getUUID());
            if (window != null && striker.level().getGameTime() <= window.until()) bonus += Math.min(2.7, 0.9 * window.level());
        }
        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(CINDER);
        int level = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level > 0 && victim.getRemainingFireTicks() >= 20) bonus += Math.min(2.3, 0.7 * level);
        Holder<Enchantment> cadenceHolder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(CADENCE);
        int cadenceLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(cadenceHolder, attacker.getMainHandItem()));
        long cadenceNow = attacker.level().getGameTime();
        if (cadenceLevel > 0 && cadenceNow >= CADENCE_COOLDOWNS.getOrDefault(attacker.getUUID(), 0L)) {
            bonus += Math.min(3.5, 1.0 * cadenceLevel);
            CADENCE_COOLDOWNS.put(attacker.getUUID(), cadenceNow + 60);
        }
        Holder<Enchantment> meteorHolder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key("meteor"));
        int meteorLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(meteorHolder, attacker.getMainHandItem()));
        if (meteorLevel > 0 && attacker.fallDistance >= 1.5f) bonus += Math.min(2.7, 0.9 * meteorLevel);
        Holder<Enchantment> initiativeHolder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(INITIATIVE);
        int initiativeLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(initiativeHolder, attacker.getMainHandItem()));
        double maxHealth = victim.getMaxHealth();
        if (initiativeLevel > 0 && maxHealth > 0.0 && victim.getHealth() / maxHealth >= 0.9) bonus += Math.min(2.1, 0.7 * initiativeLevel);
        Holder<Enchantment> executionerHolder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EXECUTIONER);
        int executionerLevel = Math.min(5, EnchantmentHelper.getItemEnchantmentLevel(executionerHolder, attacker.getMainHandItem()));
        if (executionerLevel > 0 && maxHealth > 0.0 && victim.getHealth() / maxHealth < 0.25) {
            double multiplier = Math.min(2.5, 1.0 + 0.05 * executionerLevel);
            amount *= (float) multiplier;
        }
        Holder<Enchantment> shadowHolder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SHADOWSTRIKE);
        int shadowLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(shadowHolder, attacker.getMainHandItem()));
        if (shadowLevel > 0 && attacker instanceof ServerPlayer shadowPlayer && isBehind(victim, attacker)
                && shadowPlayer.level().getGameTime() >= SHADOW_COOLDOWNS.getOrDefault(shadowPlayer.getUUID(), 0L)) {
            bonus += Math.min(4.0, 2.0 * shadowLevel);
            SHADOW_COOLDOWNS.put(shadowPlayer.getUUID(), shadowPlayer.level().getGameTime() + 80);
        }
        Holder<Enchantment> pursuitHolder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(PURSUIT);
        int pursuitLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(pursuitHolder, attacker.getMainHandItem()));
        if (pursuitLevel > 0 && victim instanceof ServerPlayer sprintingTarget && sprintingTarget.isSprinting()) bonus += Math.min(2.1, 0.7 * pursuitLevel);
        Holder<Enchantment> wingclipHolder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(WINGCLIP);
        int wingclipLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(wingclipHolder, attacker.getMainHandItem()));
        if (wingclipLevel > 0 && !victim.onGround()) bonus += Math.min(2.25, 0.75 * wingclipLevel);
        Holder<Enchantment> tracerHolder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TRACER);
        int tracerLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(tracerHolder, attacker.getMainHandItem()));
        if (tracerLevel > 0 && victim.hasEffect(MobEffects.GLOWING)) bonus += Math.min(2.1, 0.7 * tracerLevel);
        Holder<Enchantment> underdogHolder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(UNDERDOG);
        int underdogLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(underdogHolder, attacker.getMainHandItem()));
        double healthGap = victim.getHealth() - attacker.getHealth();
        if (underdogLevel > 0 && Double.isFinite(healthGap) && healthGap >= 4.0) bonus += Math.min(2.5, 0.8 * underdogLevel);
        if (bonus <= 0.0) return amount;
        double adjusted = amount + bonus;
        return Double.isFinite(adjusted) ? (float) adjusted : amount;
    }

    public static void maybeUpdraft(LivingEntity attacker) {
        if (!(attacker instanceof ServerPlayer player) || player.onGround()) return;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(UPDRAFT);
        int level = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getMainHandItem()));
        if (level > 0) player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, level * 20, 0, false, false, true));
    }

    public static void maybeTremor(LivingEntity attacker, LivingEntity victim) {
        if (!(attacker instanceof ServerPlayer player)) return;
        long now = player.level().getGameTime();
        if (now < TREMOR_COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TREMOR);
        int level = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getMainHandItem()));
        if (level <= 0 || player.getRandom().nextDouble() >= Math.min(0.5, 0.2 * level)) return;
        int duration = level * 2 * 20;
        double radius = Math.min(8.0, Math.max(0.5, 1.5 * level));
        int affected = 0;
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(victim.position(), victim.position()).inflate(radius), entity -> entity != player && entity != victim && entity.isAlive())) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 0, false, false, true));
            if (++affected >= 24) break;
        }
        TREMOR_COOLDOWNS.put(player.getUUID(), now + 80);
    }

    private static boolean isSpear(net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) return false;
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_spear") || path.equals("trident");
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }

    private static boolean isBehind(LivingEntity victim, LivingEntity attacker) {
        Vec3 facing = victim.getLookAngle().normalize();
        Vec3 toAttacker = attacker.position().subtract(victim.position()).normalize();
        return facing.lengthSqr() > 0.0 && toAttacker.lengthSqr() > 0.0 && facing.dot(toAttacker) < -0.7;
    }

    private record RallyWindow(int level, long until) {}
}
