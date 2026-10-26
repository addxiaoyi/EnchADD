package net.enchadd.mod;

import net.enchadd.mod.common.TelepathyBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

final class EnchADDNeoForgeTelepathy {
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> TELEPATHY = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "telepathy"));
    static void register() { NeoForge.EVENT_BUS.addListener(BlockDropsEvent.class, EnchADDNeoForgeTelepathy::onDrops); }
    private static void onDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)) return;
        var holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TELEPATHY);
        if (EnchantmentHelper.getItemEnchantmentLevel(holder, event.getTool()) <= 0) return;
        for (var drop : event.getDrops()) TelepathyBehavior.collect(drop, player);
    }
}
