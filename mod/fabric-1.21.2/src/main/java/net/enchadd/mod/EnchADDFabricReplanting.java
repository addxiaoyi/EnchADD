package net.enchadd.mod;

import net.enchadd.mod.common.ReplantingBehavior;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

final class EnchADDFabricReplanting {
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> REPLANTING = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "replanting"));
    private EnchADDFabricReplanting() {}
    static void init() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return;
            var registry = serverPlayer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var tool = serverPlayer.getMainHandItem();
            if (EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(REPLANTING), tool) <= 0) return;
            var seed = ReplantingBehavior.seedFor(state);
            if (seed == null) return;
            for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); i++) {
                var stack = serverPlayer.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() == seed && ReplantingBehavior.replant(serverLevel, pos, state, tool, stack)) return;
            }
        });
    }
}
