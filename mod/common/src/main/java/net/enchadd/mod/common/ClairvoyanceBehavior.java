package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClairvoyanceBehavior {

    private static final ResourceKey<Enchantment> CLAIRVOYANCE = key("clairvoyance");
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 100;
    private static final int REVEAL_TICKS_PER_LEVEL = 40;
    private static final int MAX_REVEAL_TICKS = 120;
    private static final double CHANCE_PER_LEVEL = 0.3;
    private static final double MAX_CHANCE = 0.85;
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private ClairvoyanceBehavior() {
    }

    public static void onDamage(LivingEntity victim, DamageSource source) {
        if (!(victim instanceof ServerPlayer player) || source == null) {
            return;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker) || !attacker.hasEffect(MobEffects.INVISIBILITY)) {
            return;
        }
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(CLAIRVOYANCE);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getItemBySlot(EquipmentSlot.HEAD)));
        if (level <= 0) {
            return;
        }
        long now = player.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)) {
            return;
        }
        if (player.getRandom().nextDouble() >= Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * level)) {
            return;
        }
        int duration = Math.min(MAX_REVEAL_TICKS, REVEAL_TICKS_PER_LEVEL * level);
        attacker.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0, false, false, true));
        COOLDOWNS.put(player.getUUID(), now + Math.max(0, COOLDOWN_TICKS));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }
}
