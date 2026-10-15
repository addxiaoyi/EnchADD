package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BarrierBehavior {

    private static final ResourceKey<Enchantment> BARRIER = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("enchadd", "barrier")
    );
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final int MAX_LEVEL = 2;
    private static final int COOLDOWN_TICKS = 200;
    private static final double CHANCE_PER_LEVEL = 0.30;
    private static final double MAX_CHANCE = 0.60;
    private static final double RADIUS_PER_LEVEL = 2.0;
    private static final double MAX_RADIUS = 6.0;
    private static final int MAX_TARGETS = 24;

    private BarrierBehavior() {
    }

    public static void pulse(LivingEntity defender, DamageSource source, float blockedDamage) {
        if (!(defender.level() instanceof ServerLevel level) || !Float.isFinite(blockedDamage) || blockedDamage <= 0.0f) return;
        if (!isShieldBlock(defender)) return;
        ItemStack shield = defender.getItemBySlot(EquipmentSlot.OFFHAND);
        Holder<Enchantment> holder = defender.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(BARRIER);
        int levelValue = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, shield));
        if (levelValue <= 0) return;

        long now = level.getGameTime();
        if (now < COOLDOWNS.getOrDefault(defender.getUUID(), 0L)) return;
        double chance = Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * levelValue);
        if (defender.getRandom().nextDouble() >= chance) return;

        double radius = Math.min(MAX_RADIUS, RADIUS_PER_LEVEL * levelValue);
        Vec3 origin = defender.position();
        int affected = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(origin, origin).inflate(radius), candidate -> candidate != defender && candidate.isAlive())) {
            Vec3 offset = target.position().subtract(origin);
            double horizontal = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
            if (!(horizontal > 0.0001) || !Double.isFinite(horizontal)) continue;
            double horizontalStrength = Math.min(1.4, 0.6 + 0.2 * levelValue);
            double verticalStrength = Math.min(0.8, 0.4 + 0.1 * levelValue);
            Vec3 push = new Vec3(offset.x / horizontal, verticalStrength, offset.z / horizontal)
                    .scale(horizontalStrength);
            target.setDeltaMovement(target.getDeltaMovement().add(push));
            if (++affected >= MAX_TARGETS) break;
        }
        if (affected > 0) COOLDOWNS.put(defender.getUUID(), now + COOLDOWN_TICKS);
    }

    private static boolean isShieldBlock(LivingEntity entity) {
        ItemStack used = entity.getUseItem();
        ItemStack offhand = entity.getItemBySlot(EquipmentSlot.OFFHAND);
        return entity.isBlocking() && !used.isEmpty() && used.is(Items.SHIELD)
                && !offhand.isEmpty() && offhand.is(Items.SHIELD);
    }
}
