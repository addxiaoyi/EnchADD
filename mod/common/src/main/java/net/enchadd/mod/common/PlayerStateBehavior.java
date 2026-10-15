package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.LightLayer;
import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerStateBehavior {

    private static final ResourceKey<Enchantment> FLEETFOOT = key("fleetfoot");
    private static final ResourceKey<Enchantment> AFTERGLIDE = key("afterglide");
    private static final ResourceKey<Enchantment> VAMPIRISM = key("vampirism_curse");
    private static final ResourceKey<Enchantment> LETHARGY = key("lethargy");
    private static final Map<UUID, Boolean> WAS_SPRINTING = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> WAS_GLIDING = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> FLEETFOOT_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LETHARGY_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> AFTERGLIDE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int FLEETFOOT_COOLDOWN = 60;
    private static final int AFTERGLIDE_COOLDOWN = 100;

    private PlayerStateBehavior() {
    }

    public static void tick(ServerPlayer player) {
        SoulboundBehavior.restore(player);
        UUID id = player.getUUID();
        boolean sprinting = player.isSprinting();
        boolean wasSprinting = Boolean.TRUE.equals(WAS_SPRINTING.put(id, sprinting));
        if (sprinting && !wasSprinting) { applyFleetfoot(player); applyLethargy(player); }

        boolean gliding = player.isFallFlying();
        boolean wasGliding = Boolean.TRUE.equals(WAS_GLIDING.put(id, gliding));
        if (wasGliding && !gliding && !player.onGround()) applyAfterglide(player);
        refreshVampirism(player);
        ThirstBehavior.tick(player);
        GluttonyBehavior.tick(player);
    }

    private static void applyFleetfoot(ServerPlayer player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FLEETFOOT);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, boots));
        if (level <= 0 || player.level().getGameTime() < FLEETFOOT_COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return;
        if (!canUpgrade(player, MobEffects.SPEED, 0, level * 2 * 20)) return;
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, level * 2 * 20, 0, false, false, true));
        FLEETFOOT_COOLDOWNS.put(player.getUUID(), player.level().getGameTime() + FLEETFOOT_COOLDOWN);
    }

    private static void applyLethargy(ServerPlayer player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(LETHARGY);
        int level = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(holder, boots));
        if (level <= 0 || player.level().getGameTime() < LETHARGY_COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return;
        int duration = level * 2 * 20;
        if (!canUpgrade(player, MobEffects.SLOWNESS, 0, duration)) return;
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 0, false, false, true));
        LETHARGY_COOLDOWNS.put(player.getUUID(), player.level().getGameTime() + 60);
    }

    private static void applyAfterglide(ServerPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(AFTERGLIDE);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, chest));
        if (level <= 0 || player.level().getGameTime() < AFTERGLIDE_COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return;
        int duration = level * 2 * 20;
        if (!canUpgrade(player, MobEffects.SLOW_FALLING, 0, duration)) return;
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, duration, 0, false, false, true));
        AFTERGLIDE_COOLDOWNS.put(player.getUUID(), player.level().getGameTime() + AFTERGLIDE_COOLDOWN);
    }

    private static void refreshVampirism(ServerPlayer player) {
        int level = 0;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(VAMPIRISM);
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            level += EnchantmentHelper.getItemEnchantmentLevel(holder, player.getItemBySlot(slot));
        }
        if (level <= 0) return;
        BlockPos skyPos = player.blockPosition().above(Math.max(1, (int) Math.ceil(player.getEyeHeight()) + 1));
        if (player.level().getBrightness(LightLayer.SKY, skyPos) >= 15) {
            player.setRemainingFireTicks(Math.max(player.getRemainingFireTicks(), 25));
        }
    }

    private static boolean canUpgrade(ServerPlayer player, Holder<net.minecraft.world.effect.MobEffect> effect,
                                      int amplifier, int duration) {
        MobEffectInstance current = player.getEffect(effect);
        return current == null || amplifier > current.getAmplifier() || duration > current.getDuration();
    }

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", path));
    }
}
