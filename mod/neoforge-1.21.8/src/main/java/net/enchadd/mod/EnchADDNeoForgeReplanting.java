package net.enchadd.mod;

import net.enchadd.mod.common.ReplantingBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

final class EnchADDNeoForgeReplanting {
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> REPLANTING = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "replanting"));
    static void register() { NeoForge.EVENT_BUS.addListener(BlockDropsEvent.class, EnchADDNeoForgeReplanting::onDrops); }
    private static void onDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)) return;
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var tool = event.getTool();
        if (EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(REPLANTING), tool) <= 0) return;
        var seed = ReplantingBehavior.seedFor(event.getLevel().getBlockState(event.getPos()));
        if (seed == null) return;
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == seed && ReplantingBehavior.replant(event.getLevel(), event.getPos(), event.getLevel().getBlockState(event.getPos()), tool, stack)) return;
        }
    }
}
