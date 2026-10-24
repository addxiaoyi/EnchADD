package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PurifyBehavior {

    private static final ResourceKey<Enchantment> PURIFY = key("purify");
    private static final int MAX_LEVEL = 2;
    private static final int COOLDOWN_TICKS = 100;
    private static final double TRIGGER_CHANCE = 0.2;
    private static final List<Holder<MobEffect>> NEGATIVE_EFFECTS = List.of(
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.BLINDNESS,
            MobEffects.HUNGER,
            MobEffects.WEAKNESS,
            MobEffects.CONFUSION
    );
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private PurifyBehavior() {
    }

    public static void afterConsume(ServerPlayer player) {
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(PURIFY);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getItemBySlot(EquipmentSlot.HEAD)));
        if (level <= 0) {
            return;
        }
        long now = player.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)
                || player.getRandom().nextDouble() >= TRIGGER_CHANCE) {
            return;
        }
        int removed = 0;
        for (Holder<MobEffect> effect : NEGATIVE_EFFECTS) {
            if (player.hasEffect(effect) && player.removeEffect(effect)) {
                removed++;
                if (removed >= level) {
                    break;
                }
            }
        }
        if (removed > 0) {
            COOLDOWNS.put(player.getUUID(), now + COOLDOWN_TICKS);
        }
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }
}
