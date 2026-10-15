package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class MobExperienceBehavior {
    private static final ResourceKey<Enchantment> GREED = key("greed");
    private static final ResourceKey<Enchantment> MISFORTUNE = key("misfortune");
    private static final int MAX_LEVEL = 3;
    private static final double GREED_XP_PER_LEVEL = 0.30;
    private static final double GREED_MAX_XP_MULTIPLIER = 3.0;
    private static final double MISFORTUNE_PENALTY_PER_LEVEL = 0.25;
    private static final double MAX_LOSS = 0.90;
    private static final double GREED_VULNERABILITY_PER_LEVEL = 0.25;
    private static final double GREED_MAX_VULNERABILITY = 1.0;
    private static final int GREED_SECONDS_PER_LEVEL = 6;
    private static final Map<UUID, Long> VULNERABILITY_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> VULNERABILITY_SCALE = new ConcurrentHashMap<>();

    private MobExperienceBehavior() {}

    public static int adjustDeathExperience(LivingEntity victim, Player creditedPlayer, int baseExperience) {
        if (!(victim.level() instanceof ServerLevel level) || baseExperience <= 0 || creditedPlayer == null) return baseExperience;
        Holder<Enchantment> greed = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(GREED);
        Holder<Enchantment> misfortune = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(MISFORTUNE);
        Player actualKiller = victim.getLastHurtByPlayer();
        if (actualKiller == null || !actualKiller.getUUID().equals(creditedPlayer.getUUID())) return baseExperience;
        int greedLevel = levelOf(creditedPlayer, greed);
        int misfortuneLevel = levelOf(creditedPlayer, misfortune);
        double maximumMultiplier = Math.max(1.0, Math.min(GREED_MAX_XP_MULTIPLIER, 1.0 + GREED_XP_PER_LEVEL * greedLevel));
        double multiplier = 1.0;
        if (greedLevel > 0) multiplier = Math.min(maximumMultiplier, 1.0 + GREED_XP_PER_LEVEL * greedLevel);
        if (misfortuneLevel > 0) multiplier *= 1.0 - Math.min(MAX_LOSS, MISFORTUNE_PENALTY_PER_LEVEL * misfortuneLevel);
        double adjusted = Math.max(0.0, baseExperience * multiplier);
        if (greedLevel > 0) {
            long until = level.getGameTime() + (long) GREED_SECONDS_PER_LEVEL * Math.min(MAX_LEVEL, greedLevel) * 20L;
            double scale = 1.0 + Math.min(GREED_MAX_VULNERABILITY, GREED_VULNERABILITY_PER_LEVEL * greedLevel);
            VULNERABILITY_UNTIL.merge(creditedPlayer.getUUID(), until, Math::max);
            VULNERABILITY_SCALE.merge(creditedPlayer.getUUID(), Math.min(2.0, scale), Math::max);
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.round(adjusted));
    }

    public static float adjustIncomingDamage(Player player, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f) return amount;
        long until = VULNERABILITY_UNTIL.getOrDefault(player.getUUID(), 0L);
        if (player.level().getGameTime() >= until) {
            VULNERABILITY_UNTIL.remove(player.getUUID(), until);
            VULNERABILITY_SCALE.remove(player.getUUID());
            return amount;
        }
        double scale = VULNERABILITY_SCALE.getOrDefault(player.getUUID(), 1.0);
        double adjusted = amount * Math.min(2.0, scale);
        return Double.isFinite(adjusted) ? (float) adjusted : amount;
    }

    private static int levelOf(Player player, Holder<Enchantment> enchantment) {
        return Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(enchantment, player.getMainHandItem()));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }
}


