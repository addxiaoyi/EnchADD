package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
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
    private static final Map<UUID, Long> VULNERABILITY_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> VULNERABILITY_SCALE = new ConcurrentHashMap<>();

    private MobExperienceBehavior() {}

    public static int adjustDeathExperience(LivingEntity victim, Player creditedPlayer, int baseExperience) {
        if (!(victim.level() instanceof ServerLevel level) || baseExperience <= 0 || creditedPlayer == null) return baseExperience;
        Holder<Enchantment> greed = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(GREED);
        Holder<Enchantment> misfortune = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(MISFORTUNE);
        Player actualKiller = victim.getLastHurtByPlayer();
        if (actualKiller == null || !actualKiller.getUUID().equals(creditedPlayer.getUUID())) return baseExperience;
        DamageSource lastDamage = victim.getLastDamageSource();
        if (lastDamage == null || lastDamage.getEntity() != creditedPlayer) return baseExperience;
        boolean directPlayerKill = lastDamage.getDirectEntity() == creditedPlayer;
        int greedLevel = levelOf(creditedPlayer, greed);
        int misfortuneLevel = levelOf(creditedPlayer, misfortune);
        int effectiveGreedLevel = ExperienceRewardRules.greedLevel(greedLevel, directPlayerKill);
        int adjustedExperience = ExperienceRewardRules.adjust(
                baseExperience, greedLevel, misfortuneLevel, true, directPlayerKill);
        if (effectiveGreedLevel > 0) {
            long until = level.getGameTime() + ExperienceRewardRules.vulnerabilityDurationTicks(effectiveGreedLevel);
            double scale = ExperienceRewardRules.vulnerabilityMultiplier(effectiveGreedLevel);
            VULNERABILITY_UNTIL.merge(creditedPlayer.getUUID(), until, Math::max);
            VULNERABILITY_SCALE.merge(creditedPlayer.getUUID(), scale, Math::max);
        }
        return adjustedExperience;
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
        return ExperienceRewardRules.clampLevel(
                EnchantmentHelper.getItemEnchantmentLevel(enchantment, player.getMainHandItem()));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }
}


