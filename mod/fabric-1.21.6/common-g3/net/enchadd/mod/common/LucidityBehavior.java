package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class LucidityBehavior {

    private static final ResourceKey<Enchantment> LUCIDITY = key("lucidity");
    private static final int MAX_LEVEL = 3;
    private static final double REDUCTION_PER_LEVEL = 0.25;
    private static final double MAX_REDUCTION = 0.75;

    private LucidityBehavior() {
    }

    public static MobEffectInstance adjust(LivingEntity entity, MobEffectInstance effect) {
        if (entity == null || effect == null || effect.isInfiniteDuration() || !isTarget(effect)) {
            return effect;
        }
        int level = level(entity);
        if (level <= 0 || effect.getDuration() <= 1) {
            return effect;
        }
        double reduction = Math.min(MAX_REDUCTION, REDUCTION_PER_LEVEL * level);
        MobEffectInstance adjusted = new MobEffectInstance(effect.getEffect(), Math.max(1, (int) (effect.getDuration() * (1.0 - reduction))), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
        return adjusted.getDuration() < effect.getDuration() ? adjusted : effect;
    }

    private static boolean isTarget(MobEffectInstance effect) {
        return effect.is(MobEffects.BLINDNESS) || effect.is(MobEffects.DARKNESS) || effect.is(MobEffects.NAUSEA);
    }

    private static int level(LivingEntity entity) {
        Holder<Enchantment> holder = entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(LUCIDITY);
        return Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, entity.getItemBySlot(EquipmentSlot.HEAD)));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }
}
