package net.enchadd.mod;

import net.enchadd.mod.common.FurrowBehavior;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

final class EnchADDFabricFurrow {
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> FURROW = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "furrow"));
    private EnchADDFabricFurrow() {}
    static void init() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return;
            if (serverPlayer.isShiftKeyDown()) return;
            var holder = serverPlayer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FURROW);
            if (EnchantmentHelper.getItemEnchantmentLevel(holder, serverPlayer.getMainHandItem()) > 0) FurrowBehavior.harvest(serverLevel, serverPlayer, pos, state, serverPlayer.getMainHandItem());
        });
    }
}
