package net.enchadd.mod;

import net.enchadd.mod.common.RefineBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

final class EnchADDNeoForgeRefine {
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> REFINE = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "refine"));
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> SILK_TOUCH = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("minecraft", "silk_touch"));
    static void register() { NeoForge.EVENT_BUS.addListener(BlockDropsEvent.class, EnchADDNeoForgeRefine::onDrops); }
    private static void onDrops(BlockDropsEvent event) {
        if (event.getBlockEntity() != null || !(event.getBreaker() instanceof ServerPlayer player) || player.isCreative()) return;
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var tool = event.getTool();
        if (EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(SILK_TOUCH), tool) > 0) return;
        int enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(REFINE), tool);
        RefineBehavior.apply(event.getLevel(), event.getPos(), player.getRandom(), enchantLevel);
    }
}
