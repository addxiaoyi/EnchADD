package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WaysongBehavior {

    private static final ResourceKey<Enchantment> WAYSONG = key("waysong");
    private static final int COOLDOWN_TICKS = 900;
    private static final double RADIUS = 8.0;
    private static final int SPEED_TICKS = 120;
    private static final int SPEED_AMPLIFIER = 0;
    private static final int MAX_TARGETS = 16;
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private WaysongBehavior() {
    }

    public static void use(ServerPlayer caller, ItemStack item) {
        if (item.isEmpty() || !"goat_horn".equals(BuiltInRegistries.ITEM.getKey(item.getItem()).getPath())) {
            return;
        }
        Holder<Enchantment> holder = caller.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(WAYSONG);
        if (EnchantmentHelper.getItemEnchantmentLevel(holder, item) <= 0) {
            return;
        }
        long now = caller.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(caller.getUUID(), 0L)) {
            return;
        }
        boolean applied = upgrade(caller, SPEED_TICKS);
        int affected = 0;
        AABB area = caller.getBoundingBox().inflate(RADIUS);
        for (Player target : caller.level().getEntitiesOfClass(Player.class, area, Player::isAlive)) {
            if (target == caller || !(target instanceof ServerPlayer serverTarget)) {
                continue;
            }
            if (upgrade(serverTarget, SPEED_TICKS)) {
                applied = true;
                if (++affected >= MAX_TARGETS) {
                    break;
                }
            }
        }
        if (applied) {
            COOLDOWNS.put(caller.getUUID(), now + COOLDOWN_TICKS);
        }
    }

    private static boolean upgrade(ServerPlayer player, int duration) {
        MobEffectInstance current = player.getEffect(MobEffects.SPEED);
        if (current != null && (current.getAmplifier() > SPEED_AMPLIFIER
                || current.getAmplifier() == SPEED_AMPLIFIER && current.getDuration() >= duration)) {
            return false;
        }
        return player.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, SPEED_AMPLIFIER, false, false, true));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }
}
