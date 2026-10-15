package net.enchadd.mod;

import net.enchadd.mod.common.RefineBehavior;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

final class EnchADDFabricRefine {
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> REFINE = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "refine"));
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> SILK_TOUCH = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("minecraft", "silk_touch"));
    private EnchADDFabricRefine() {}
    static void init() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer) || serverPlayer.isCreative()) return;
            var registry = serverPlayer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var tool = serverPlayer.getMainHandItem();
            if (EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(SILK_TOUCH), tool) > 0) return;
            int enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(REFINE), tool);
            RefineBehavior.apply(serverLevel, pos, serverPlayer.getRandom(), enchantLevel);
        });
    }
}
