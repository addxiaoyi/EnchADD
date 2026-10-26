package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
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

    private static final ResourceKey<Enchantment> BULWARK = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "bulwark"));
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int SECONDS_PER_LEVEL = 2;
    private static final int COOLDOWN_TICKS = 80;
    private static final int RESISTANCE_AMPLIFIER = 0;

    private BulwarkBehavior() {
    }

    public static void maybeApply(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !player.isBlocking()) return;
        ItemStack shield = player.getUseItem();
        if (shield.isEmpty() || !shield.is(Items.SHIELD)) return;
        ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (offhand.isEmpty() || !offhand.is(Items.SHIELD)) return;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(BULWARK);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, offhand));
        if (level <= 0) return;
        long now = player.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return;
        int duration = level * SECONDS_PER_LEVEL * 20;
        if (player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, duration, RESISTANCE_AMPLIFIER, false, false, true))) {
            COOLDOWNS.put(player.getUUID(), now + COOLDOWN_TICKS);
        }
    }
}
