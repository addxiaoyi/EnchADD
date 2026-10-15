package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WingguardBehavior {
    private static final ResourceKey<Enchantment> WINGGUARD = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "wingguard"));
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private WingguardBehavior() {}

    public static boolean rescue(ServerPlayer player, DamageSource source, float amount) {
        if (!source.is(DamageTypes.FALL) && !source.is(DamageTypes.FLY_INTO_WALL)) return false;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(WINGGUARD);
        int level = Math.min(2, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getItemBySlot(EquipmentSlot.CHEST)));
        if (level <= 0 || player.getHealth() > amount) return false;
        long now = player.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return false;
        if (player.getRandom().nextDouble() >= Math.min(0.9, 0.45 * level)) return false;
        player.setHealth(Math.max(2.0f, player.getMaxHealth() * 0.2f));
        int duration = level * 4 * 20;
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, duration, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, duration, 0, false, false, true));
        COOLDOWNS.put(player.getUUID(), now + 1200);
        return true;
    }
}
