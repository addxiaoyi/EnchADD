package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class DamageProtectionBehavior {
    private static final ResourceKey<Enchantment> ARROWGUARD = key("arrowguard");
    private static final ResourceKey<Enchantment> BLASTGUARD = key("blastguard");
    private static final ResourceKey<Enchantment> FIREGUARD = key("fireguard");
    private static final ResourceKey<Enchantment> FROSTGUARD = key("frostguard");
    private static final ResourceKey<Enchantment> FEATHERSTEP = key("featherstep");
    private static final double MAX_REDUCTION = 0.45;

    private DamageProtectionBehavior() {}

    public static float adjustDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity.level() instanceof ServerLevel level) || !Float.isFinite(amount) || amount <= 0.0f
                || source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL)) return amount;

        double reduction = 0.0;
        if (isProjectile(source)) reduction = Math.max(reduction, reduction(entity, level, ARROWGUARD, 0.07, 0.21));
        if (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)) {
            reduction = Math.max(reduction, reduction(entity, level, BLASTGUARD, 0.08, 0.24));
        }
        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.LAVA)) {
            reduction = Math.max(reduction, reduction(entity, level, FIREGUARD, 0.08, 0.24));
        }
        if (source.is(DamageTypes.FREEZE)) reduction = Math.max(reduction, reduction(entity, level, FROSTGUARD, 0.10, 0.30));
        if (source.is(DamageTypes.FALL)) reduction = Math.max(reduction, reduction(entity, level, FEATHERSTEP, 0.12, 0.36));
        if (reduction <= 0.0) return amount;

        double adjusted = amount * (1.0 - reduction);
        return Double.isFinite(adjusted) ? (float) Math.max(0.0, adjusted) : amount;
    }

    private static double reduction(LivingEntity entity, ServerLevel level, ResourceKey<Enchantment> key,
                                    double perLevel, double maximum) {
        Holder<Enchantment> enchantment = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
        int highestLevel = 0;
        EquipmentSlot[] slots = key.equals(FEATHERSTEP)
                ? new EquipmentSlot[]{EquipmentSlot.FEET}
                : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            highestLevel = Math.max(highestLevel, EnchantmentHelper.getItemEnchantmentLevel(enchantment, entity.getItemBySlot(slot)));
        }
        if (highestLevel <= 0) return 0.0;
        return Math.min(MAX_REDUCTION, Math.min(maximum, perLevel * Math.min(3, highestLevel)));
    }

    private static boolean isProjectile(DamageSource source) {
        return source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }
}
