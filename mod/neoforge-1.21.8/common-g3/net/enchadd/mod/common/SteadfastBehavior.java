package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SteadfastBehavior {

    private static final ResourceKey<Enchantment> STEADFAST = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("enchadd", "steadfast")
    );
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 80;
    private static final double CHANCE_PER_LEVEL = 0.30;
    private static final double MAX_CHANCE = 0.60;
    private static final double REDUCTION_PER_LEVEL = 0.15;
    private static final double MAX_REDUCTION = 0.60;

    private SteadfastBehavior() {
    }

    public static double reduceStrength(Player player, double strength) {
        if (!Double.isFinite(strength) || strength <= 0.0) return strength;
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(STEADFAST);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, boots));
        if (level <= 0) return strength;
        long now = player.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return strength;
        if (player.getRandom().nextDouble() >= Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * level)) return strength;
        double reduction = Math.min(MAX_REDUCTION, REDUCTION_PER_LEVEL * level);
        COOLDOWNS.put(player.getUUID(), now + COOLDOWN_TICKS);
        return strength * (1.0 - reduction);
    }
}
