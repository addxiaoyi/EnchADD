package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;

public final class StonewakeBehavior {

    private static final ResourceKey<Enchantment> STONEWAKE = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("enchadd", "stonewake")
    );
    private static final int MAX_LEVEL = 3;
    private static final int SECONDS_PER_LEVEL = 1;
    private static final int MAX_AMPLIFIER = 2;
    private static final int MAX_DURATION_TICKS = 120 * 20;

    private StonewakeBehavior() {
    }

    public static void onBreak(ServerPlayer player, BlockState state, ItemStack tool) {
        if (!isOre(state)) return;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(STONEWAKE);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, tool));
        if (level <= 0) return;

        int duration = Math.min(MAX_DURATION_TICKS, level * SECONDS_PER_LEVEL * 20);
        int amplifier = Math.min(MAX_AMPLIFIER, 0);
        MobEffectInstance current = player.getEffect(MobEffects.HASTE);
        if (current != null && (current.getAmplifier() > amplifier
                || current.getAmplifier() == amplifier && current.getDuration() >= duration)) return;
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, duration, amplifier, false, false, true));
    }

    private static boolean isOre(BlockState state) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.endsWith("_ore") || path.equals("ancient_debris");
    }
}
