package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WaterMovementBehavior {
    private static final ResourceKey<Enchantment> TIDESHELL = key("tideshell");
    private static final ResourceKey<Enchantment> TIDE_RUNNER = key("tide_runner");
    private static final Map<UUID, Long> TIDESHELL_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> TIDE_RUNNER_COOLDOWNS = new ConcurrentHashMap<>();

    private WaterMovementBehavior() {}

    public static void useShell(ServerPlayer player, ItemStack item) {
        if (item.isEmpty() || !"nautilus_shell".equals(BuiltInRegistries.ITEM.getKey(item.getItem()).getPath())
                || !touchingWater(player)) return;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TIDESHELL);
        if (EnchantmentHelper.getItemEnchantmentLevel(holder, item) <= 0) return;
        long now = player.level().getGameTime();
        if (now < TIDESHELL_COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return;
        boolean applied = upgrade(player, MobEffects.WATER_BREATHING, 8 * 20)
                | upgrade(player, MobEffects.DOLPHINS_GRACE, 5 * 20);
        if (applied) TIDESHELL_COOLDOWNS.put(player.getUUID(), now + 700);
    }

    public static void tick(ServerPlayer player) {
        if (!player.isSwimming() || !player.isInWater() || player.level().getGameTime() % 5 != 0) return;
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TIDE_RUNNER);
        int level = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(holder, boots));
        if (level <= 0) return;
        long now = player.level().getGameTime();
        if (now < TIDE_RUNNER_COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return;
        int duration = Math.min(2400, Math.max(40, level * 60));
        int amplifier = (int) Math.round(Math.min(1.0, 0.3 * level));
        MobEffectInstance current = player.getEffect(MobEffects.DOLPHINS_GRACE);
        if (current != null && (current.getAmplifier() > amplifier
                || current.getAmplifier() == amplifier && current.getDuration() >= duration)) return;
        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, duration, amplifier, false, false, true));
        TIDE_RUNNER_COOLDOWNS.put(player.getUUID(), now + 200);
    }

    private static boolean upgrade(ServerPlayer player, Holder<net.minecraft.world.effect.MobEffect> effect, int duration) {
        MobEffectInstance current = player.getEffect(effect);
        if (current != null && (current.getAmplifier() > 0 || current.getDuration() >= duration)) return false;
        player.addEffect(new MobEffectInstance(effect, duration, 0, false, false, true));
        return true;
    }

    private static boolean touchingWater(ServerPlayer player) {
        if (player.isInWater()) return true;
        BlockPos feet = player.blockPosition();
        BlockPos eyes = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        return player.level().getFluidState(feet).is(FluidTags.WATER)
                || player.level().getFluidState(eyes).is(FluidTags.WATER);
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }
}
