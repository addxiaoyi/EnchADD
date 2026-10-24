package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class AirbagBehavior {

    private static final ResourceKey<Enchantment> AIRBAG = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("enchadd", "airbag")
    );
    private static final int MAX_LEVEL = 4;
    private static final double REDUCTION_PER_LEVEL = 0.20;
    private static final double MAX_REDUCTION = 0.90;

    private AirbagBehavior() {
    }

    public static float adjustDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f || !isCushioned(source)) return amount;
        Holder<Enchantment> holder = entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(AIRBAG);
        int levels = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = entity.getItemBySlot(slot);
            levels = Math.min(Integer.MAX_VALUE, levels + EnchantmentHelper.getItemEnchantmentLevel(holder, armor));
        }
        if (levels <= 0) return amount;
        double reduction = Math.min(MAX_REDUCTION, REDUCTION_PER_LEVEL * Math.min(levels, MAX_LEVEL));
        float adjusted = (float) (amount * (1.0 - reduction));
        return Float.isFinite(adjusted) ? Math.max(0.0f, adjusted) : amount;
    }

    private static boolean isCushioned(DamageSource source) {
        return source.is(DamageTypes.FALL) || source.is(DamageTypes.FLY_INTO_WALL);
    }
}
