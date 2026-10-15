package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
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

public final class ParryBehavior {

    private static final ResourceKey<Enchantment> PARRY = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("enchadd", "parry")
    );
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Retaliation> WINDOWS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 80;
    private static final int WINDOW_TICKS = 60;
    private static final double BONUS_PER_LEVEL = 1.0;
    private static final double MAX_BONUS = 2.5;

    private ParryBehavior() {
    }

    public static void arm(LivingEntity defender, DamageSource source, float blockedDamage) {
        if (!(defender instanceof ServerPlayer player) || !(defender.level() instanceof ServerLevel level)
                || !Float.isFinite(blockedDamage) || blockedDamage <= 0.0f) return;
        if (!(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) return;
        if (!isShieldBlock(defender)) return;

        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(PARRY);
        int levelValue = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder,
                player.getItemBySlot(EquipmentSlot.OFFHAND)));
        if (levelValue <= 0) return;

        long now = level.getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return;
        COOLDOWNS.put(player.getUUID(), now + COOLDOWN_TICKS);
        WINDOWS.put(player.getUUID(), new Retaliation(attacker.getUUID(), levelValue, now + WINDOW_TICKS));
    }

    public static float adjustCounterattack(LivingEntity victim, DamageSource source, float amount) {
        if (!(victim.level() instanceof ServerLevel level) || !Float.isFinite(amount) || amount <= 0.0f) return amount;
        if (!(source.getEntity() instanceof ServerPlayer attacker) || source.getDirectEntity() != attacker) return amount;
        Retaliation retaliation = WINDOWS.remove(attacker.getUUID());
        if (retaliation == null || level.getGameTime() > retaliation.until()
                || !retaliation.target().equals(victim.getUUID())) return amount;

        double bonus = Math.min(MAX_BONUS, BONUS_PER_LEVEL * retaliation.level());
        double adjusted = amount + bonus;
        return Double.isFinite(adjusted) ? (float) adjusted : amount;
    }

    private static boolean isShieldBlock(LivingEntity entity) {
        ItemStack used = entity.getUseItem();
        ItemStack offhand = entity.getItemBySlot(EquipmentSlot.OFFHAND);
        return entity.isBlocking() && !used.isEmpty() && used.is(Items.SHIELD)
                && !offhand.isEmpty() && offhand.is(Items.SHIELD);
    }

    private record Retaliation(UUID target, int level, long until) {
    }
}
