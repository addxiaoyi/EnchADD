package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ShroudBehavior {

    private static final ResourceKey<Enchantment> SHROUD = key("shroud");
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 160;
    private static final double CHANCE_PER_LEVEL = 0.25;
    private static final double MAX_CHANCE = 0.6;
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private ShroudBehavior() {}

    public static boolean shouldBreakTarget(Mob mob, LivingEntity target) {
        if (!(target instanceof ServerPlayer player) || !player.isShiftKeyDown()) {
            return false;
        }
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SHROUD);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getItemBySlot(EquipmentSlot.LEGS)));
        if (level <= 0) {
            return false;
        }
        long now = player.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)
                || player.getRandom().nextDouble() >= Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * level)) {
            return false;
        }
        COOLDOWNS.put(player.getUUID(), now + COOLDOWN_TICKS);
        return true;
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }
}
