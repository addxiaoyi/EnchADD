package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class FirebreakBehavior {
    private static final ResourceKey<Enchantment> FIREBREAK = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "firebreak"));
    private FirebreakBehavior() {}

    public static float adjustDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f || !isFire(source)) return amount;
        Holder<Enchantment> holder = entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FIREBREAK);
        int level = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(holder, entity.getItemBySlot(EquipmentSlot.CHEST)));
        if (level <= 0) return amount;
        double reduction = Math.min(0.24, 0.08 * level);
        double adjusted = amount * (1.0 - reduction);
        return Double.isFinite(adjusted) ? (float) Math.max(0.0, adjusted) : amount;
    }

    private static boolean isFire(DamageSource source) {
        return source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.LAVA);
    }
}
