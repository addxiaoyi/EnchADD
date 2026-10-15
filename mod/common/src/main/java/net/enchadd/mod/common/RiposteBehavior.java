package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RiposteBehavior {

    private static final ResourceKey<Enchantment> RIPOSTE = key("riposte");
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 100;
    private static final int WEAKNESS_SECONDS_PER_LEVEL = 2;
    private static final double CHANCE_PER_LEVEL = 0.25;
    private static final double MAX_CHANCE = 0.75;
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private RiposteBehavior() {
    }

    public static void blocked(ServerPlayer defender, ItemStack shield, LivingEntity attacker) {
        if (shield == null || shield.isEmpty() || !shield.is(Items.SHIELD) || attacker == null) {
            return;
        }
        Holder<Enchantment> holder = defender.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(RIPOSTE);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, shield));
        if (level <= 0) {
            return;
        }
        long now = defender.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(defender.getUUID(), 0L)
                || defender.getRandom().nextDouble() >= Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * level)) {
            return;
        }
        int duration = WEAKNESS_SECONDS_PER_LEVEL * level * 20;
        if (attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0, false, false, true))) {
            COOLDOWNS.put(defender.getUUID(), now + COOLDOWN_TICKS);
        }
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }
}
