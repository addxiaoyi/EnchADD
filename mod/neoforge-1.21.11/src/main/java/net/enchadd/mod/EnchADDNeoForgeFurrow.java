package net.enchadd.mod;

import net.enchadd.mod.common.FurrowBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

final class EnchADDNeoForgeFurrow {
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> FURROW = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "furrow"));
    static void register() { NeoForge.EVENT_BUS.addListener(BlockDropsEvent.class, EnchADDNeoForgeFurrow::onDrops); }
    private static void onDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player) || player.isShiftKeyDown()) return;
        var holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FURROW);
        var tool = event.getTool();
        if (EnchantmentHelper.getItemEnchantmentLevel(holder, tool) <= 0) return;
        FurrowBehavior.harvest(event.getLevel(), player, event.getPos(), event.getLevel().getBlockState(event.getPos()), tool);
    }
}
