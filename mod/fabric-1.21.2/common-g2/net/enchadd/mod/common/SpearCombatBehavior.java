package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class SpearCombatBehavior {
    private static final ResourceKey<Enchantment> SKEWER = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "skewer"));
    private static final int MAX_LEVEL = 3;
    private static final int MAX_DURATION_TICKS = 40;
    private static final double CHANCE_PER_LEVEL = 0.22;
    private static final double MAX_CHANCE = 0.75;
    private static final long COOLDOWN_TICKS = 60L;
    private static final java.util.Map<java.util.UUID, Long> COOLDOWNS = new java.util.concurrent.ConcurrentHashMap<>();

    private SpearCombatBehavior() {}

    public static void onHit(LivingEntity target, LivingEntity attacker, DamageSource source, float damage) {
        if (!Float.isFinite(damage) || damage <= 0.0f || target == attacker || !target.isAlive()
                || source.getEntity() != attacker || source.getDirectEntity() != attacker) return;
        ItemStack weapon = attacker.getMainHandItem();
        if (!isSpear(weapon)) return;
        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SKEWER);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, weapon));
        if (level <= 0) return;
        long now = attacker.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(attacker.getUUID(), 0L)) return;
        double chance = Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * level);
        if (attacker.getRandom().nextDouble() >= chance) return;
        int duration = Math.min(MAX_DURATION_TICKS, level * 10);
        int amplifier = Math.min(1, Math.max(0, level - 1));
        if (target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier, false, false, true))) {
            COOLDOWNS.put(attacker.getUUID(), now + COOLDOWN_TICKS);
        }
    }

    private static boolean isSpear(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_spear") || path.equals("trident");
    }
}
