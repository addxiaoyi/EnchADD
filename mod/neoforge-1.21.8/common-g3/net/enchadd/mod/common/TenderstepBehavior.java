package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;

public final class TenderstepBehavior {
    private static final ResourceKey<Enchantment> TENDERSTEP = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "tenderstep"));
    private TenderstepBehavior() {}

    public static boolean protects(Entity entity, BlockState state) {
        if (!(entity instanceof ServerPlayer player)) return false;
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (!path.equals("farmland") && !path.equals("turtle_egg")) return false;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TENDERSTEP);
        return EnchantmentHelper.getItemEnchantmentLevel(holder, player.getItemBySlot(EquipmentSlot.FEET)) > 0;
    }
}
