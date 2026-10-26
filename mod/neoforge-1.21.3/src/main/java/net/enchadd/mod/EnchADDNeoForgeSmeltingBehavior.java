package net.enchadd.mod;

import net.enchadd.mod.common.SmeltingBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

final class EnchADDNeoForgeSmeltingBehavior {
    static void register() { NeoForge.EVENT_BUS.addListener(BlockDropsEvent.class, EnchADDNeoForgeSmeltingBehavior::onDrops); }

    private static void onDrops(BlockDropsEvent event) {
        if (event.getBlockEntity() != null || !(event.getBreaker() instanceof ServerPlayer player)) return;
        ItemStack tool = event.getTool();
        if (tool.isEmpty()) return;
        if (EnchantmentHelper.getItemEnchantmentLevel(
                player.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                        .getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("enchadd", "smelting"))),
                tool) <= 0) return;
        ServerLevel level = event.getLevel();
        for (ItemEntity drop : event.getDrops()) {
            ItemStack smelted = SmeltingBehavior.smelt(level, drop.getItem());
            if (!smelted.isEmpty()) drop.setItem(smelted);
        }
    }
}
