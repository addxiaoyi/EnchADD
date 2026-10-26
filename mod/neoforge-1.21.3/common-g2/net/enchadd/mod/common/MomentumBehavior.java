package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class MomentumBehavior {

    private static final ResourceKey<Enchantment> MOMENTUM = key("momentum");
    private static final int MAX_LEVEL = 3;
    private static final int SECONDS_PER_LEVEL = 2;
    private static final int MAX_DURATION_TICKS = 120;
    private static final int MAX_AMPLIFIER = 2;

    private MomentumBehavior() {
    }

    public static void onDeath(LivingEntity victim, DamageSource source) {
        if (victim == null || source == null || source.getDirectEntity() != source.getEntity()) {
            return;
        }
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(MOMENTUM);
        ItemStack weapon = player.getMainHandItem();
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, weapon));
        if (level <= 0) {
            return;
        }
        int duration = Math.min(MAX_DURATION_TICKS, level * SECONDS_PER_LEVEL * 20);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0, false, false, true));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }
}
