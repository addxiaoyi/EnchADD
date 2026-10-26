package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SpearDefenseBehavior {
    private static final ResourceKey<Enchantment> COUNTERTHRUST = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "counterthrust"));
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 30;
    private static final double REDUCTION_PER_LEVEL = 0.06;
    private static final double MAX_REDUCTION = 0.18;

    private SpearDefenseBehavior() {}

    public static float adjustDamage(LivingEntity defender, DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f || defender == null || source == null
                || !defender.isShiftKeyDown() || !isDirectMelee(defender, source)) return amount;
        ItemStack weapon = defender.getMainHandItem();
        if (!isSpear(weapon)) return amount;
        Holder<Enchantment> holder = defender.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(COUNTERTHRUST);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, weapon));
        if (level <= 0) return amount;
        long now = defender.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(defender.getUUID(), 0L)) return amount;
        double reduction = Math.min(MAX_REDUCTION, REDUCTION_PER_LEVEL * level);
        double adjusted = amount * (1.0 - reduction);
        if (!Double.isFinite(adjusted) || adjusted >= amount) return amount;
        COOLDOWNS.put(defender.getUUID(), now + COOLDOWN_TICKS);
        return (float) Math.max(0.0, adjusted);
    }

    private static boolean isDirectMelee(LivingEntity defender, DamageSource source) {
        return source.getEntity() != null && source.getEntity() != defender && source.getDirectEntity() == source.getEntity();
    }

    private static boolean isSpear(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_spear") || path.equals("trident");
    }
}
