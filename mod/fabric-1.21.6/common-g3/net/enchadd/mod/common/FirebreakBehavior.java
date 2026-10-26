package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class FirebreakBehavior {
    private static final ResourceKey<Enchantment> FIREBREAK = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "firebreak"));
    private FirebreakBehavior() {}

    public static float adjustDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f || !isFire(source)) return amount;
        return FirebreakRules.adjustDamage(amount, level(entity));
    }

    public static float adjustIgnitionSeconds(Entity entity, float seconds) {
        if (!(entity instanceof LivingEntity living) || !Float.isFinite(seconds) || seconds <= 0.0f) return seconds;
        int ticks = Math.max(1, (int) Math.floor(seconds * 20.0f));
        return FirebreakRules.adjustBurnTicks(ticks, level(living)) / 20.0f;
    }

    private static int level(LivingEntity entity) {
        Holder<Enchantment> holder = entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FIREBREAK);
        return EnchantmentHelper.getItemEnchantmentLevel(holder, entity.getItemBySlot(EquipmentSlot.CHEST));
    }

    private static boolean isFire(DamageSource source) {
        return source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.LAVA);
    }
}
