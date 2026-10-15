package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BulwarkBehavior {

    private static final ResourceKey<Enchantment> BULWARK = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "bulwark"));
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int SECONDS_PER_LEVEL = 2;
    private static final int COOLDOWN_TICKS = 80;
    private static final int MAX_AMPLIFIER = 1;

    private BulwarkBehavior() {
    }

    public static void maybeApply(LivingEntity entity) {
        if (!entity.isBlocking()) return;
        ItemStack shield = entity.getUseItem();
        if (shield.isEmpty() || !shield.is(Items.SHIELD)) return;
        ItemStack offhand = entity.getItemBySlot(EquipmentSlot.OFFHAND);
        if (offhand.isEmpty() || !offhand.is(Items.SHIELD)) return;
        Holder<Enchantment> holder = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(BULWARK);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, offhand));
        if (level <= 0) return;
        long now = entity.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(entity.getUUID(), 0L)) return;
        int duration = level * SECONDS_PER_LEVEL * 20;
        if (entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, duration, MAX_AMPLIFIER, false, false, true))) {
            COOLDOWNS.put(entity.getUUID(), now + COOLDOWN_TICKS);
        }
    }
}
