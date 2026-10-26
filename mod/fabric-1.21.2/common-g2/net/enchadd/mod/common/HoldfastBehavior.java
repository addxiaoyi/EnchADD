package net.enchadd.mod.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HoldfastBehavior {

    private static final ResourceKey<Enchantment> HOLDFAST = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("enchadd", "holdfast")
    );
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 0;
    private static final double CHANCE_PER_LEVEL = 0.18;
    private static final double MAX_CHANCE = 0.54;

    private HoldfastBehavior() {
    }

    public static void maybeSuppress(LivingEntity defender, DamageSource source) {
        // 1.21.1 has no BlocksAttacks component; shield-disable suppression is unavailable.
        if (!(defender instanceof ServerPlayer player) || !isAxeAttack(source) || !isShielding(player)) return;
        ItemStack shield = player.getItemBySlot(EquipmentSlot.OFFHAND);
        var holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(HOLDFAST);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, shield));
        if (level <= 0) return;
        long now = player.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return;
        if (player.getRandom().nextDouble() >= Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * level)) return;
        COOLDOWNS.put(player.getUUID(), now + COOLDOWN_TICKS);
    }

    private static boolean isAxeAttack(DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) return false;
        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) return false;
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(weapon.getItem()).getPath();
        return path.endsWith("_axe");
    }

    private static boolean isShielding(ServerPlayer player) {
        ItemStack used = player.getUseItem();
        ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
        return player.isBlocking() && !used.isEmpty() && used.is(Items.SHIELD)
                && !offhand.isEmpty() && offhand.is(Items.SHIELD);
    }
}
