package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EvasionBehavior {

    private static final ResourceKey<Enchantment> EVASION = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("enchadd", "evasion")
    );
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 100;
    private static final double CHANCE_PER_LEVEL = 0.15;
    private static final double MAX_CHANCE = 0.60;

    private EvasionBehavior() {
    }

    public static boolean shouldDodge(ServerPlayer player, DamageSource source) {
        if (!(source.getDirectEntity() instanceof AbstractArrow)) return false;
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EVASION);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, boots));
        if (level <= 0) return false;

        long now = player.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return false;
        if (player.getRandom().nextDouble() >= Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * level)) return false;
        COOLDOWNS.put(player.getUUID(), now + COOLDOWN_TICKS);
        return true;
    }
}
