package net.enchadd.mod;

import net.enchadd.mod.common.ReplantingBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

final class EnchADDForgeReplanting {
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> REPLANTING = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "replanting"));
    static void register() { BlockEvent.BreakEvent.BUS.addListener(new EnchADDForgeReplanting()::onBreak); }
    @SubscribeEvent public void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getPlayer() instanceof ServerPlayer player)) return;
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var tool = player.getMainHandItem();
        if (EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(REPLANTING), tool) <= 0) return;
        var state = event.getState();
        var seed = ReplantingBehavior.seedFor(state);
        if (seed == null) return;
        level.getServer().execute(() -> {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                var stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() == seed && ReplantingBehavior.replant(level, event.getPos(), state, tool, stack)) return;
            }
        });
    }
}
