package net.enchadd.mod;

import net.enchadd.mod.common.FurrowBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

final class EnchADDForgeFurrow {
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> FURROW = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "furrow"));
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeFurrow()::onBreak); }
    @SubscribeEvent public void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getPlayer() instanceof ServerPlayer player) || player.isShiftKeyDown()) return;
        var holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FURROW);
        var tool = player.getMainHandItem();
        if (EnchantmentHelper.getItemEnchantmentLevel(holder, tool) <= 0) return;
        level.getServer().execute(() -> FurrowBehavior.harvest(level, player, event.getPos(), event.getState(), tool));
    }
}
