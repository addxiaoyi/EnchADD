package net.enchadd.mod;

import net.enchadd.mod.common.TelepathyBehavior;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

final class EnchADDFabricTelepathy {
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> TELEPATHY = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "telepathy"));
    private EnchADDFabricTelepathy() {}
    static void init() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return;
            var holder = serverPlayer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TELEPATHY);
            if (EnchantmentHelper.getItemEnchantmentLevel(holder, serverPlayer.getMainHandItem()) > 0) {
                serverLevel.getServer().execute(() -> TelepathyBehavior.collectFreshDrops(serverLevel, pos, serverPlayer));
            }
        });
    }
}
