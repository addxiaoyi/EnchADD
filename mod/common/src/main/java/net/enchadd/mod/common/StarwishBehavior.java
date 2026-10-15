package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StarwishBehavior {

    private static final ResourceKey<Enchantment> STARWISH = key("starwish");
    private static final int COOLDOWN_TICKS = 1200;
    private static final int NIGHT_VISION_TICKS = 200;
    private static final int LUCK_TICKS = 120;
    private static final double LOOK_UP_PITCH = -60.0;
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private StarwishBehavior() {
    }

    public static void use(ServerPlayer player, ItemStack item) {
        if (item.isEmpty() || !"spyglass".equals(BuiltInRegistries.ITEM.getKey(item.getItem()).getPath())) {
            return;
        }
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(STARWISH);
        if (EnchantmentHelper.getItemEnchantmentLevel(holder, item) <= 0
                || player.level().dimension() != net.minecraft.world.level.Level.OVERWORLD
                || !isNight(worldTime(player) % 24000L)
                || player.getXRot() > LOOK_UP_PITCH) {
            return;
        }
        long now = player.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)) {
            return;
        }
        boolean applied = upgrade(player, MobEffects.NIGHT_VISION, NIGHT_VISION_TICKS)
                | upgrade(player, MobEffects.LUCK, LUCK_TICKS);
        if (applied) {
            COOLDOWNS.put(player.getUUID(), now + COOLDOWN_TICKS);
        }
    }

    private static boolean isNight(long time) {
        return time >= 13000L && time <= 23000L;
    }

    private static long worldTime(ServerPlayer player) {
        try {
            try {
                return ((Number) player.level().getClass().getMethod("getDayTime").invoke(player.level())).longValue();
            } catch (NoSuchMethodException ignored) {
                return ((Number) player.level().getClass().getMethod("getOverworldClockTime").invoke(player.level())).longValue();
            }
        } catch (ReflectiveOperationException exception) {
            return 0L;
        }
    }

    private static boolean upgrade(ServerPlayer player, Holder<net.minecraft.world.effect.MobEffect> effect, int duration) {
        MobEffectInstance current = player.getEffect(effect);
        if (current != null && (current.getAmplifier() > 0 || current.getDuration() >= duration)) {
            return false;
        }
        return player.addEffect(new MobEffectInstance(effect, duration, 0, false, false, true));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }
}
