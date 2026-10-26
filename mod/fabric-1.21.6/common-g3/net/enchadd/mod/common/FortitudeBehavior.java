package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FortitudeBehavior {

    private static final ResourceKey<Enchantment> FORTITUDE = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "fortitude"));
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int HEARTS_PER_LEVEL = 2;
    private static final int REGEN_TICKS_PER_LEVEL = 60;
    private static final int RESISTANCE_TICKS_PER_LEVEL = 40;
    private static final int COOLDOWN_TICKS = 100;

    private FortitudeBehavior() {
    }

    public static void maybeApply(LivingEntity entity, float damage) {
        if (!Float.isFinite(damage) || damage <= 0.0f) return;
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty()) return;
        Holder<Enchantment> holder = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FORTITUDE);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, chest));
        if (level <= 0) return;
        double remaining = entity.getHealth() - damage;
        double threshold = Math.min(entity.getMaxHealth() * 0.5, level * HEARTS_PER_LEVEL * 2.0);
        if (!(remaining > 0.0 && remaining <= threshold)) return;
        long now = entity.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(entity.getUUID(), 0L)) return;
        boolean applied = entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, level * REGEN_TICKS_PER_LEVEL, 0, false, false, true));
        applied |= entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, level * RESISTANCE_TICKS_PER_LEVEL, 0, false, false, true));
        if (applied) COOLDOWNS.put(entity.getUUID(), now + COOLDOWN_TICKS);
    }
}
