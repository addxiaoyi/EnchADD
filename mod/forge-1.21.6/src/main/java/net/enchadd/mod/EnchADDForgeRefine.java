package net.enchadd.mod;

import net.enchadd.mod.common.RefineBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

final class EnchADDForgeRefine {
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> REFINE = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "refine"));
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> SILK_TOUCH = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("minecraft", "silk_touch"));
    static void register() { BlockEvent.BreakEvent.BUS.addListener(new EnchADDForgeRefine()::onBreak); }
    @SubscribeEvent public void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getPlayer() instanceof ServerPlayer player) || player.isCreative()) return;
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var tool = player.getMainHandItem();
        if (EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(SILK_TOUCH), tool) > 0) return;
        int enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(REFINE), tool);
        level.getServer().execute(() -> RefineBehavior.apply(level, event.getPos(), player.getRandom(), enchantLevel));
    }
}
