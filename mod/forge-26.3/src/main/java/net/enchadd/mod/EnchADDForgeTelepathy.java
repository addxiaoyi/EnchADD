package net.enchadd.mod;

import net.enchadd.mod.common.TelepathyBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeTelepathy {
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> TELEPATHY = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "telepathy"));
    static void register() { BlockEvent.BreakEvent.BUS.addListener(new EnchADDForgeTelepathy()::onBreak); }
    @SubscribeEvent public void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getPlayer() instanceof ServerPlayer player)) return;
        var holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TELEPATHY);
        if (EnchantmentHelper.getItemEnchantmentLevel(holder, player.getMainHandItem()) <= 0) return;
        level.getServer().execute(() -> level.getServer().execute(
                () -> TelepathyBehavior.collectFreshDrops(level, event.getPos(), player)));
    }
}
