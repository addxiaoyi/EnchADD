package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PivotBehavior {

    private static final ResourceKey<Enchantment> PIVOT = key("pivot");
    private static final int MAX_LEVEL = 2;
    private static final int COOLDOWN_TICKS = 50;
    private static final int SECONDS_PER_LEVEL = 1;

    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private PivotBehavior() {
    }

    public static void blocked(ServerPlayer player, ItemStack shield) {
        if (shield == null || shield.isEmpty() || !shield.is(Items.SHIELD)) {
            return;
        }
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(PIVOT);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, shield));
        if (level <= 0) {
            return;
        }
        long now = player.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)) {
            return;
        }
        int duration = level * SECONDS_PER_LEVEL * 20;
        MobEffectInstance current = player.getEffect(MobEffects.SPEED);
        if (current != null && (current.getAmplifier() > 0
                || current.getAmplifier() == 0 && current.getDuration() >= duration)) {
            return;
        }
        if (player.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, 0, false, false, true))) {
            COOLDOWNS.put(player.getUUID(), now + COOLDOWN_TICKS);
        }
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }
}
