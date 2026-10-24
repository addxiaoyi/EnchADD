package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class OverwhelmBehavior {
    private static final ResourceKey<Enchantment> OVERWHELM = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "overwhelm"));
    private static final int MAX_LEVEL = 2;
    private static final int MAX_EFFECT_TICKS = 2400;

    private OverwhelmBehavior() {}

    public static void onHit(LivingEntity target, LivingEntity attacker, DamageSource source, float damage) {
        if (!Float.isFinite(damage) || damage <= 0.0f || target == attacker || !target.onGround()
                || source.getEntity() != attacker || source.getDirectEntity() != attacker
                || !attacker.getMainHandItem().is(Items.MACE)) return;
        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(OVERWHELM);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) return;
        int ticks = Math.min(MAX_EFFECT_TICKS, level * 20);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 0, false, false, true));
    }
}
