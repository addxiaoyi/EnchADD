package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LastStandBehavior {

    private static final ResourceKey<Enchantment> LAST_STAND = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("enchadd", "last_stand")
    );
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final int MAX_LEVEL = 2;
    private static final int COOLDOWN_TICKS = 2400;
    private static final double CHANCE_PER_LEVEL = 0.15;
    private static final double MAX_CHANCE = 0.35;
    private static final double RESCUE_FRACTION = 0.20;
    private static final double MIN_HEALTH = 2.0;

    private LastStandBehavior() {
    }

    public static boolean rescue(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty()) return false;
        Holder<Enchantment> holder = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(LAST_STAND);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, chest));
        if (level <= 0) return false;
        long now = entity.level().getGameTime();
        long until = COOLDOWNS.getOrDefault(entity.getUUID(), 0L);
        if (now < until) return false;
        double chance = Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * level);
        if (entity.getRandom().nextDouble() >= chance) return false;
        double health = Math.min(entity.getMaxHealth(), Math.max(MIN_HEALTH, entity.getMaxHealth() * RESCUE_FRACTION));
        if (!Double.isFinite(health) || health <= 0.0) return false;
        entity.setHealth((float) health);
        COOLDOWNS.put(entity.getUUID(), now + COOLDOWN_TICKS);
        return true;
    }
}
