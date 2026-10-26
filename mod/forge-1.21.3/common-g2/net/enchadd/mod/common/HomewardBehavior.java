package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HomewardBehavior {

    private static final ResourceKey<Enchantment> HOMEWARD = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("enchadd", "homeward")
    );
    private static final Map<UUID, Window> WINDOWS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 80;
    private static final int WINDOW_TICKS = 60;
    private static final int SPEED_SECONDS_PER_LEVEL = 1;
    private static final double REDUCTION_PER_LEVEL = 0.20;
    private static final double MAX_REDUCTION = 0.60;

    private HomewardBehavior() {
    }

    public static float adjustDamage(ServerPlayer player, DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f) return amount;
        if (source.is(DamageTypes.FALL)) return reduceFall(player, amount);
        if (!isCombatSource(source) || !player.isSprinting()) return amount;

        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(HOMEWARD);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, leggings));
        if (level <= 0) return amount;

        long now = player.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return amount;
        int speedTicks = level * SPEED_SECONDS_PER_LEVEL * 20;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, speedTicks, 0, false, false, true));
        WINDOWS.put(player.getUUID(), new Window(level, now + WINDOW_TICKS));
        COOLDOWNS.put(player.getUUID(), now + COOLDOWN_TICKS);
        return amount;
    }

    private static float reduceFall(ServerPlayer player, float amount) {
        Window window = WINDOWS.remove(player.getUUID());
        if (window == null || player.level().getGameTime() > window.until()) return amount;
        double reduction = Math.min(MAX_REDUCTION, REDUCTION_PER_LEVEL * window.level());
        float adjusted = (float) (amount * (1.0 - reduction));
        return Float.isFinite(adjusted) ? Math.max(0.0f, adjusted) : amount;
    }

    private static boolean isCombatSource(DamageSource source) {
        return source.getEntity() instanceof LivingEntity || source.getDirectEntity() instanceof Projectile;
    }

    private record Window(int level, long until) {
    }
}
